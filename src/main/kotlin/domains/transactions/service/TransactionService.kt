package org.example.domains.transcations.service

import org.example.common.cache.RedisClient
import org.example.common.cache.RedisKeyProvider
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.common.json.JsonUtil
import org.example.common.logging.Logging
import org.example.common.message.KafkaProducer
import org.example.common.message.Topics
import org.example.common.transaction.Transactional
import org.example.domains.transactions.model.DepositResponse
import org.example.domains.transactions.model.TransferResponse
import org.example.domains.transactions.repository.TransactionsAccount
import org.example.domains.transactions.repository.TransactionsUser
import org.example.types.dto.Response
import org.example.types.dto.ResponseProvider
import org.example.types.message.TransactionMessage
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionsUser: TransactionsUser,
    private val transactionsAccount: TransactionsAccount,
    private val producer: KafkaProducer,
    private val redisClient: RedisClient,
    private val transactional: Transactional,
    private val logger : Logger = Logging.getLogger(TransactionService::class.java)
) {

    fun deposit(userUlid: String, accountID :String,  value: BigDecimal) : Response<DepositResponse> = Logging.logFor(logger) { it
        it["userUlid"] = userUlid
        it["accountID"] = accountID
        it["value"] = value

        val key = RedisKeyProvider.bankMutexKey(userUlid, accountID)

        return@logFor redisClient.invokeWithMutex(key) {
            return@invokeWithMutex transactional.run {
                val user = transactionsUser.findByUlid(userUlid)

                val account = transactionsAccount.findByUlidAndUser(accountID, user)
                    ?: throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)

                account.balance = account.balance.add(value)
                account.updatedAt = LocalDateTime.now()
                transactionsAccount.save(account)

                val message = JsonUtil.encodeToJson(TransactionMessage(
                    fromUlid = "0x0",
                    fromName = "0x0",
                    fromAccountID =  "0x0",
                    toUlid =  userUlid,
                    toName = user.username,
                    toAccountID = accountID,
                    value = value,
                ), TransactionMessage.serializer())

                producer.sendMessage(Topics.Transactions.topic, message)

                ResponseProvider.success(DepositResponse(afterBalance = account.balance))
            }
        }

    }

    fun transfer(fromUlid: String, fromAccountId: String, toAccountId: String, value : BigDecimal) : Response<TransferResponse> = Logging.logFor(logger) { it
        it["fromUlid"] = fromUlid
        it["fromAccountId"] = fromAccountId
        it["toUlid"] = toAccountId
        it["value"] = value

        val key = RedisKeyProvider.bankMutexKey(fromUlid, fromAccountId)

        return@logFor redisClient.invokeWithMutex(key) {
            return@invokeWithMutex transactional.run {
                val fromAccount = transactionsAccount.findByUlid(fromAccountId)
                    ?: throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)

                if (fromAccount.user.ulid != fromUlid) {
                    throw CustomException(ErrorCode.MISS_MATCH_ACOUNT_ULID_AND_USER_ULID)
                } else if (fromAccount.balance < value) {
                    throw CustomException(ErrorCode.ENOUGH_VALUE)
                } else if (value <= BigDecimal.ZERO) {
                    throw CustomException(ErrorCode.VALUE_MUST_NOT_BE_UNDER_ZERO)
                }

                val toAccount = transactionsAccount.findByUlid(toAccountId)
                    ?: throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)

                fromAccount.balance = fromAccount.balance.subtract(value)
                toAccount.balance = toAccount.balance.add(value)

                transactionsAccount.save(toAccount)
                transactionsAccount.save(fromAccount)

                val message = JsonUtil.encodeToJson(TransactionMessage(
                    fromUlid = fromUlid,
                    fromName = fromAccount.user.username,
                    fromAccountID =  fromAccountId,
                    toUlid =  toAccount.user.ulid,
                    toName = toAccount.user.username,
                    toAccountID = toAccountId,
                    value = value,
                ), TransactionMessage.serializer())

                producer.sendMessage(Topics.Transactions.topic, message)

                ResponseProvider.success(
                    TransferResponse(
                        afterFromBalance = fromAccount.balance,
                        afterToBalance = toAccount.balance
                    )
                )
            }
        }
    }

}