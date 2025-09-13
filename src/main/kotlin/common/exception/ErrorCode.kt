package org.example.common.exception


interface CodeInterface {
    val code: Int
    val message: String
}

enum class ErrorCode(
    override val code: Int,
    override val message: String
) : CodeInterface {
    AUTH_CONFIG_NOT_FOUND(-100, "oauth config not found"),
    FAILED_TO_CALL_CLIENT(-100, "failed to call client"),
    CALL_RESULT_BODY_IS_NULL(-100, "call result body is null"),
    PROVIDER_NOT_FOUND(-100, "provider not found"),
    TOKEN_IS_INVALID(-104,"token is invalid"),
    TOKEN_EXPIRED(-105,"token is expired"),
    FAILED_TO_INVOKE_IN_LOGGER(-200, "failed to invoke in logger"),
    FAILED_TO_SAVE_DATA(-300, "failed to save data"),
    FAILED_TO_FIND_ACCOUNT(-301, "failed to find account"),
    FAILED_MATCH_ACCOUNT_ULID_AND_USER_ULID(-302, "failed match account ulid and user ulid"),
    ACCOUNT_BALANCE_IS_NOT_ZERO(-303, "account balance is not zero"),
    FAILED_MUTEX_INVOKE(111, "failed mutex invoke"),
    FAILED_TO_GET_LOCK(112, "failed to get lock"),
    INSUFFICIENT_BALANCE(400, "insufficient balance"),
    INVALID_TRANSACTION_AMOUNT(401, "invalid transaction amount"),
}