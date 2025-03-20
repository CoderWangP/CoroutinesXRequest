package com.wp.xrequest.http.exception

import com.wp.xrequest.http.ApiResponse
import com.wp.xrequest.http.CodeConstant


/**
 *
 * api exception
 */
sealed class ApiException(val errorMsg: String?) : Throwable(errorMsg) {
    /**
     * Using retrofit to execute http request errors
     */
    data class ResponseException(val exCause: Throwable?) : ApiException(exCause?.message)

    /**
     * business exception when code is not [CodeConstant.RESULT_OK]
     */
    data class BusinessException(val apiResponse: ApiResponse.Success<*>) :
        ApiException(apiResponse.message)

}

val ApiException.isBusinessException: Boolean
    get() = this is ApiException.BusinessException

fun ApiException.toBusinessException(): ApiException.BusinessException {
    return this as ApiException.BusinessException
}