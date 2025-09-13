package org.example.types.dto

import org.springframework.http.HttpStatus

object ResponseProvider {
    fun<T> success(result: T? = null): Response<T> {
        return Response(
            code = HttpStatus.OK.value(),
            message = "SUCCESS",
            result = result
        )
    }

    fun<T> failed(code: HttpStatus,message: String,result: T? = null): Response<T> {
        return Response(
            code = code.value(),
            message = message,
            result = result
        )
    }
}


data class Response<T>(
    val code: Int,
    val message: String,
    val result: T? = null
)