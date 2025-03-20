package com.wp.xrequest.http.calladapter.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.*
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 *
 * handle flow CallAdapter
 *
 */
class FlowCallAdapter<T>(private val dataType: Type) : CallAdapter<T, Flow<T>> {
    override fun responseType(): Type {
        return dataType
    }

    override fun adapt(call: Call<T>): Flow<T> {
        return flow {
            emit(suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    call.cancel()
                }
                call.enqueue(object : Callback<T> {
                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body == null) {
                                val invocation = call.request().tag(Invocation::class.java)
                                val method = invocation?.method()
                                val e = KotlinNullPointerException("Response from " +
                                        method?.declaringClass?.name +
                                        '.' +
                                        method?.name +
                                        " was null but response body type was declared as non-null")
                                continuation.resumeWithException(e)
                            } else {
                                continuation.resume(body)
                            }
                        } else {
                            continuation.resumeWithException(HttpException(response))
                        }
                    }

                    override fun onFailure(call: Call<T>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
            })
        }
    }
}