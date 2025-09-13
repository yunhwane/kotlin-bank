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
}