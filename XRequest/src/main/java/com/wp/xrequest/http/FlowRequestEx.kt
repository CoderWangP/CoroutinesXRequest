package com.wp.xrequest.http

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wp.xrequest.Config
import com.wp.xrequest.http.exception.ApiException
import com.wp.xrequest.logD
import com.wp.xrequest.logE
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.cancellation.CancellationException

/**
 *
 *
 * flow extension util
 *
 * https://developer.android.com/kotlin/flow/stateflow-and-sharedflow?hl=zh-cn
 *
 */
inline fun LifecycleOwner.request4Flow(
    tag: String = "",
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    val key = tag.ifEmpty {
        getJobDefaultTag()
    }
    if (jobStore.contains(key)) {
        val previousJob = jobStore.remove(key)
        if (previousJob?.isActive == true) {
            kotlin.runCatching {
                previousJob.cancel("cancel previous same request,tag is :$key")
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
    val job = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            kotlin.runCatching {
                block()
            }.onFailure {
                if (isActive) {
                    logE(TAG, "onFailure :${it.message}")
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
    }
    jobStore[key] = job
    job.invokeOnCompletion {
        jobStore.remove(key)
        if (Config.enableLog) {
            logD(TAG, "createJob -> LifecycleOwner.request4Flow:$jobStore")
        }
    }
}

suspend fun <T> Flow<ApiResponse<T>>.collectSuccessDataOrNull(
    ifErrorCancelScope: Boolean = false,
    callback: (T?, ApiException?) -> Unit
) {
    this
        .flowOn(Dispatchers.IO)
        .catch { e ->
            logE(TAG,"origin error = ${e.message}")
            val targetError = ApiException.ResponseException(e)
            if (ifErrorCancelScope) {
                throw CancellationException(targetError)
            } else {
                Config.errorHandler?.handle(targetError)
            }
        }
        .collect {
            if (it is ApiResponse.Success) {
                if (it.code == CodeConstant.RESULT_OK) {
                    callback(it.data, null)
                } else {
                    callback(null, ApiException.BusinessException(it))
                }
            } else {
                val e = it as ApiResponse.Failure
                if (ifErrorCancelScope) {
                    throw CancellationException(e.cause)
                } else {
                    val targetError = ApiException.ResponseException(e.cause)
                    Config.errorHandler?.handle(targetError)
                    callback(null, targetError)
                }
            }
        }
}


suspend fun <T> Flow<ApiResponse<T>>.collectResponse(
    ifErrorCancelScope: Boolean = false,
    callback: (ApiResponse.Success<*>?, ApiException?) -> Unit
) {
    this
        .flowOn(Dispatchers.IO)
        .catch { e ->
            logE(TAG,"origin error = ${e.message}")
            val targetError = ApiException.ResponseException(e)
            if (ifErrorCancelScope) {
                throw CancellationException(targetError)
            } else {
                Config.errorHandler?.handle(targetError)
            }
        }
        .collect {
            when (it) {
                is ApiResponse.Success -> callback(it, null)
                is ApiResponse.Failure -> {
                    val targetError = ApiException.ResponseException(it.cause)
                    Config.errorHandler?.handle(targetError)
                    callback(null, targetError)
                }
            }
        }
}