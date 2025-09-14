package org.example.domains.history.service

import kotlinx.serialization.builtins.ListSerializer
import org.example.common.cache.RedisClient
import org.example.common.cache.RedisKeyProvider
import org.example.common.json.JsonUtil
import org.example.common.logging.Logging
import org.example.domains.history.repository.HistoryMongoRepository
import org.example.types.dto.History
import org.example.types.dto.Response
import org.example.types.dto.ResponseProvider
import org.slf4j.Logger
import org.springframework.stereotype.Service

@Service
class HistoryService(
    private val historyMongoRepository: HistoryMongoRepository,
    private val redisClient: RedisClient,
    private val logger : Logger = Logging.getLogger(HistoryService::class.java),
) {

    fun history(ulid: String): Response<List<History>> = Logging.logFor(logger) { log ->
        log["ulid"] = ulid

        val key = RedisKeyProvider.historyCacheKey(ulid)
        val cached = redisClient.get(key)

        val data = if (cached == null) {
            val result = historyMongoRepository.findLatestTransactionHistory(ulid)
            redisClient.setIfNotExist(key, JsonUtil.encodeToJson(result, ListSerializer(History.serializer())))
            result
        } else {
            JsonUtil.decodeFromJson(cached, ListSerializer(History.serializer()))
        }

        ResponseProvider.success(data)
    }
}

