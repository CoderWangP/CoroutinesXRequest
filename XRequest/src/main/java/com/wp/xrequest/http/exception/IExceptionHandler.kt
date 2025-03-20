package com.wp.xrequest.http.exception

/**
 *
 * error handler
 *
 */
interface IExceptionHandler {
    /**
     * @param preprocessing 预处理异常
     */
    fun handle(error: ApiException, preprocessing: Boolean = true): ApiException
}