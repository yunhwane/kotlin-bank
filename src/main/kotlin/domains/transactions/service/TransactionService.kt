package org.example.domains.transactions.service

import org.example.common.cache.RedisClient
import org.example.common.cache.RedisKeyProvider
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.common.logging.Logging
import org.example.common.transaction.Transactional
import org.example.domains.transactions.model.DepositResponse
import org.example.domains.transactions.model.TransferResponse
import org.example.domains.transactions.repository.TransactionsAccount
import org.example.domains.transactions.repository.TransactionsUser
import org.example.types.dto.Response
import org.example.types.dto.ResponseProvider
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransactionService(
    private val transactionUser: TransactionsUser,
    private val transactionAccount: TransactionsAccount,
    private val redisClient: RedisClient,
    private val transactional: Transactional,
    private val logger: Logger = Logging.getLogger(TransactionService::class.java),
) {

    fun deposit(userUlid: String, accountID: String, value: BigDecimal): Response<DepositResponse> = Logging.logFor(logger) { log ->
        log["user_ulid"] = userUlid
        log["account_id"] = accountID
        log["value"] = value

        val key = RedisKeyProvider.bankMutexKey(userUlid, accountID)

        return@logFor redisClient.invokeWithMutex(key) {
            return@invokeWithMutex transactional.run {
                val user = transactionUser.findByUlid(userUlid)

                val account = transactionAccount.findByUlidAndUser(accountID, user)
                    ?: throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)

                account.balance = account.balance.add(value)
                account.updatedAt = LocalDateTime.now()
                transactionAccount.save(account)

                ResponseProvider.success(DepositResponse(afterBalance = account.balance))
            }
        }
    }

    fun transfer(fromUlid: String, fromAccountId: String, toAccountId: String, value: BigDecimal): Response<TransferResponse> = Logging.logFor(logger) { log ->
        log["from_user_ulid"] = fromUlid
        log["from_account_id"] = fromAccountId
        log["to_account_id"] = toAccountId
        log["value"] = value

        val key = RedisKeyProvider.bankMutexKey(fromUlid, fromAccountId)

        return@logFor redisClient.invokeWithMutex(key) {
            return@invokeWithMutex transactional.run {
                val fromAccount = transactionAccount.findByUlid(fromAccountId)
                    ?: throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)

                if(fromAccount.user.ulid != fromUlid) {
                    throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)
                } else if(fromAccount.balance < value) {
                } else if(value <= BigDecimal.ZERO) {
                }

                val toAccount = transactionAccount.findByUlid(toAccountId)
                    ?: throw CustomException(ErrorCode.FAILED_TO_FIND_ACCOUNT)

                fromAccount.balance = fromAccount.balance.subtract(value)
                fromAccount.updatedAt = LocalDateTime.now()

                toAccount.balance = toAccount.balance.add(value)
                toAccount.updatedAt = LocalDateTime.now()

                transactionAccount.save(toAccount)
                transactionAccount.save(fromAccount)

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