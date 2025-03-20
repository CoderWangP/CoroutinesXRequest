package com.wp.xrequest.http.calladapter.deferred

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.Type

class DeferredCallAdapter<T>(private val dataType: Type) : CallAdapter<T, Deferred<T>> {
    override fun responseType(): Type {
        return dataType
    }

    override fun adapt(call: Call<T>): Deferred<T> {
        val deferred = CompletableDeferred<T>()
        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        val invocation = call.request().tag(Invocation::class.java)
                        val method = invocation?.method()
                        val e = KotlinNullPointerException(
                            "Response from " +
                                    method?.declaringClass?.name +
                                    '.' +
                                    method?.name +
                                    " was null but response body type was declared as non-null"
                        )
                        deferred.completeExceptionally(e)
                    } else {
                        deferred.complete(body)
                    }
                } else {
                    deferred.completeExceptionally(HttpException(response))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                deferred.completeExceptionally(t)
            }
        })
        return deferred
    }
}