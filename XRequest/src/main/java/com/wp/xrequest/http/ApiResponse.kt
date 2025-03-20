package com.wp.xrequest.http

sealed class ApiResponse<out T> {
    data class Success<T>(
        val data: T,
        val message: String,
        val code: Int = CodeConstant.RESULT_OK
    ) : ApiResponse<T>()

    data class Failure(val cause: Throwable) : ApiResponse<Nothing>()
}

val ApiResponse<*>.isSuccess: Boolean get() = this is ApiResponse.Success<*>

val ApiResponse<*>.isFailure: Boolean get() = !isSuccess



