@file:JvmName("RequestEx")

package com.wp.xrequest.http

import android.app.Application
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.wp.xrequest.Config
import com.wp.xrequest.http.exception.ApiException
import com.wp.xrequest.logD
import com.wp.xrequest.logE
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 *
 *
 * request extension util
 *
 */
const val TAG = "RequestEx"

val jobStore: ConcurrentHashMap<String, Job> = ConcurrentHashMap()

inline fun ViewModel.request(
    tag: String = "",
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    createJob(tag, viewModelScope, block)
}

inline fun LifecycleOwner.request(
    tag: String = "",
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    createJob(tag, lifecycleScope, block)
}

inline fun Application.request(
    tag: String = "",
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    createJob(tag, globalScope, block)
}


val globalScope = object : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext
}

inline fun createJob(
    tag: String = "",
    scope: CoroutineScope,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    Result
    val key = tag.ifEmpty {
        getJobDefaultTag()
    }
    if (Config.enableLog) {
        logD(TAG, "key = $key")
    }
    if (jobStore.contains(key)) {
        val previousJob = jobStore.remove(key)
        if (previousJob?.isActive == true) {
            runCatching {
                previousJob.cancel("cancel previous same request,tag is :$key")
            }.onFailure {
                logE(TAG, "${it.message}")
            }
        }
    }
    val job = scope.launch(Dispatchers.Main.immediate) {
        kotlin.runCatching {
            block()
        }.onFailure {
            logE(TAG, "isActive = ${isActive}, onFailure :${it.message}")
            if (isActive) {
                //all exception package into CancellationException cause
                if (it is CancellationException) {
                    val cause = it.cause
                    if (cause is ApiException) {
                        Config.errorHandler?.handle(cause)
                    }
                }
                throw it
            }
        }
    }
    jobStore[key] = job
    job.invokeOnCompletion {
        jobStore.remove(key)
        if (Config.enableLog) {
            val jobStores = if (scope is LifecycleCoroutineScope) {
                "LifecycleOwner.request: $jobStore"
            } else if (scope.coroutineContext is EmptyCoroutineContext) {
                "Application.request: $jobStore"
            } else {
                "ViewModel.request:$jobStore"
            }
            logD(
                TAG,
                "invokeOnCompletion -> jobStores = $jobStores , error = ${it?.message}"
            )
        }
    }
}

fun getJobDefaultTag(): String {
    runCatching {
        val stackTraces = Thread.currentThread().stackTrace
        val index = stackTraces.indexOfFirst { it.methodName == "getJobDefaultTag" }
        if (index == -1) {
            return ""
        }
        val next = (index + 1)
        if (next in stackTraces.indices) {
            val nextStackTrace = stackTraces[next]
            return nextStackTrace.className + "." + nextStackTrace.methodName
        }
    }.onFailure {
        logE(TAG, it.message)
    }
    return ""
}


suspend inline fun <T> launchRequest(
    crossinline block: suspend () -> ApiResponse<T>
): ApiResponse<T> {
    return withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: Throwable) {
            ApiResponse.Failure(e)
        }
    }
}

/**
 * request for async,suitable for multiple request
 */
inline fun <T> CoroutineScope.asyncRequest(
    crossinline block: suspend () -> ApiResponse<T>
): Deferred<ApiResponse<T>> {
    return this.async(SupervisorJob(this.coroutineContext[Job]), CoroutineStart.DEFAULT) {
        block()
    }
}


/**
 * 获取ApiResponse<T>中的T，如果有异常(包含业务异常)，返回null
 * [Config.errorHandler] to handle exception
 */
fun <T> ApiResponse<T>.getOrNull(preprocessing: Boolean = true): T? {
    return try {
        when (this) {
            is ApiResponse.Success -> {
                logD(TAG, "Success")
                if (this.code == CodeConstant.RESULT_OK) {
                    data
                } else {
                    throw ApiException.BusinessException(this)
                }
            }

            is ApiResponse.Failure -> throw ApiException.ResponseException(this.cause)
        }
    } catch (e: Throwable) {
        logD(TAG, "failure -> ${e.message}")
        handleThrowCancelException(e, preprocessing)
        null
    }
}

/**
 * if has exception, will cancel top CoroutineScope. Use carefully!!!
 */
fun <T> ApiResponse<T>.getOrThrow(preprocessing: Boolean = true): T {
    return try {
        when (this) {
            is ApiResponse.Success -> {
                logD(TAG, "Success")
                if (this.code == CodeConstant.RESULT_OK) {
                    data
                } else {
                    throw ApiException.BusinessException(this)
                }
            }

            is ApiResponse.Failure -> throw ApiException.ResponseException(this.cause)
        }
    } catch (e: Throwable) {
        logD(TAG, "failure -> ${e.message}")
        val cancellationException = createCancellationException(e)
        val cause = cancellationException.cause
        if (cause is ApiException) {
            Config.errorHandler?.handle(cause, preprocessing)
        }
        throw cancellationException
    }
}

fun <T, R : T> ApiResponse<T>.getOrElse(onFailure: (Throwable) -> R): T {
    return try {
        when (this) {
            is ApiResponse.Success -> {
                logD(TAG, "Success")
                if (this.code == CodeConstant.RESULT_OK) {
                    data
                } else {
                    throw ApiException.BusinessException(this)
                }
            }

            is ApiResponse.Failure -> throw ApiException.ResponseException(this.cause)
        }
    } catch (e: Throwable) {
        logD(TAG, "failure -> ${e.message}")
        val cancellationException = createCancellationException(e)
        val cause = cancellationException.cause
        if (cause is ApiException) {
            onFailure(cause)
        } else {
            throw cancellationException
        }
    }
}

fun <T> ApiResponse<T>.exceptionOrNull(): ApiException? {
    if (this is ApiResponse.Success && code != CodeConstant.RESULT_OK) {
        return ApiException.BusinessException(this)
    }
    if (this is ApiResponse.Failure) {
        return ApiException.ResponseException(this.cause)
    }
    return null
}

fun <T> ApiResponse<T>.businessExceptionOrNull(): ApiException.BusinessException? {
    if (this is ApiResponse.Success && code != CodeConstant.RESULT_OK) {
        return ApiException.BusinessException(this)
    }
    return null
}

private fun handleThrowCancelException(
    e: Throwable,
    preprocessing: Boolean
) {
    val cancellationException = createCancellationException(e)
    //not cancel top coroutine scope must not throw exception
    val cause = cancellationException.cause
    if (cause is ApiException) {
        Config.errorHandler?.handle(cause, preprocessing)
    } else {
        throw cancellationException
    }
}

private fun createCancellationException(e: Throwable): CancellationException {
    return when (e) {
        is CancellationException -> e
        is ApiException -> CancellationException(e)
        else -> CancellationException(ApiException.ResponseException(e))
    }
}

/**
 * 获取deferred结果ApiResponse<T>
 */
suspend fun <T> Deferred<ApiResponse<T>>.getResponse(): ApiResponse<T> {
    return try {
        await()
    } catch (e: Throwable) {
        ApiResponse.Failure(e)
    }
}

/**
 * 获取ApiResponse<T>中的T，如果有异常，返回null
 */
suspend fun <T> Deferred<ApiResponse<T>>.getOrNull(preprocessing: Boolean = true): T? {
    return try {
        when (val response = await()) {
            is ApiResponse.Success -> {
                if (response.code == CodeConstant.RESULT_OK) {
                    response.data
                } else {
                    throw ApiException.BusinessException(response)
                }
            }

            is ApiResponse.Failure -> throw ApiException.ResponseException(response.cause)
        }
    } catch (e: Throwable) {
        handleThrowCancelException(e, preprocessing)
        logE(TAG, "Deferred<ApiResponse<T>>.getORThrow -> error = ${e.message}")
        null
    }
}


suspend fun <T> Deferred<ApiResponse<T>>.exceptionOrNull(): ApiException? {
    try {
        val response = await()
        if (response is ApiResponse.Success && response.code != CodeConstant.RESULT_OK) {
            return ApiException.BusinessException(response)
        }
        if (response is ApiResponse.Failure) {
            return ApiException.ResponseException(response.cause)
        }
        return null
    } catch (e: Throwable) {
        return ApiException.ResponseException(e)
    }
}

/**
 * 多个请求并行合并
 */
@Suppress("UNCHECKED_CAST")
suspend fun <T1, T2> zip(
    deferred1: Deferred<ApiResponse<T1>>,
    deferred2: Deferred<ApiResponse<T2>>,
    block: (T1?, T2?, ApiException?) -> Unit
) {
    try {
        val responses = awaitAll(deferred1, deferred2)
        if (responses.all { it is ApiResponse.Success && it.code == CodeConstant.RESULT_OK }) {
            val result = responses.map { (it as ApiResponse.Success).data }
            block(result[0] as T1, result[1] as T2, null)
        } else {
            val e = findFirstFailureException(responses)
            block(null, null, e)
            throw CancellationException(e)
        }
    } catch (e: Throwable) {
        val exCancel = createCancellationException(e)
        val cause = exCancel.cause
        if (cause is ApiException) {
            block(null, null, cause)
        }
        throw exCancel
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <T1, T2, T3> zip(
    deferred1: Deferred<ApiResponse<T1>>,
    deferred2: Deferred<ApiResponse<T2>>,
    deferred3: Deferred<ApiResponse<T3>>,
    block: (T1?, T2?, T3?, ApiException?) -> Unit
) {
    try {
        val responses = awaitAll(deferred1, deferred2, deferred3)
        if (responses.all { it is ApiResponse.Success && it.code == CodeConstant.RESULT_OK }) {
            val result = responses.map { (it as ApiResponse.Success).data }
            block(result[0] as T1, result[1] as T2, result[2] as T3, null)
        } else {
            val e = findFirstFailureException(responses)
            block(null, null, null, e)
            throw CancellationException(e)
        }
    } catch (e: Throwable) {
        val exCancel = createCancellationException(e)
        val cause = exCancel.cause
        if (cause is ApiException) {
            block(null, null, null, cause)
        }
        throw exCancel
    }
}

private fun findFirstFailureException(responses: List<ApiResponse<Any?>>): ApiException {
    val firstFailureResponse =
        responses.first { it is ApiResponse.Success && it.code != CodeConstant.RESULT_OK || it is ApiResponse.Failure }
    val e = if (firstFailureResponse is ApiResponse.Success) {
        ApiException.BusinessException(firstFailureResponse)
    } else {
        ApiException.ResponseException((firstFailureResponse as ApiResponse.Failure).cause)
    }
    return e
}