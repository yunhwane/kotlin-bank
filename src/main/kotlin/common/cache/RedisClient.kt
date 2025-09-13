package org.example.common.cache

import kotlinx.serialization.KSerializer
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.common.json.JsonUtil
import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.TimeUnit

class RedisClient(
    private val template: RedisTemplate<String, String>,
    private val redissonClient: RedissonClient
) {

    fun get(key: String): String? {
        return template.opsForValue().get(key)
    }

    fun <T> get(key: String, mapper: (String) -> T?): T? {
        val value = get(key)
        return value?.let(mapper)
    }

    fun setIfNotExist(key: String, value: String): Boolean {
        return template.opsForValue().setIfAbsent(key, value) ?: false
    }

    fun <T> invokeWithMutex(key: String, function: () -> T?): T? {
        val lock = redissonClient.getLock(key)
        var lockAcquired = false
        try {
            lockAcquired = lock.tryLock(10, 15, TimeUnit.SECONDS)

            if(!lockAcquired) {
                throw CustomException(ErrorCode.FAILED_TO_GET_LOCK, key)
            }

            return function.invoke()
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_MUTEX_INVOKE, key)
        } finally {
            if(lockAcquired && lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}