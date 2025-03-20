package com.wp.xrequest.http.calladapter

import com.wp.xrequest.http.ApiResponse
import com.wp.xrequest.logD
import com.wp.xrequest.logE
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Objects.requireNonNull

class SafeCallAdapterFactory : CallAdapter.Factory() {

    companion object {
        private const val TAG = "SafeCallAdapterFactory"
        @JvmStatic
        fun create(): SafeCallAdapterFactory {
            return SafeCallAdapterFactory()
        }
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //Call<ApiResponse<T>>
        if (getRawType(returnType) != Call::class.java) {
            return null
        }
        require(returnType is ParameterizedType) { "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>" }
        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != ApiResponse::class.java) {
            return null
        }
        //对应于ApiResponse<out T> 中的T
        return object : CallAdapter<Any, Call<Any>> {
            override fun responseType(): Type {
                return responseType
            }

            override fun adapt(call: Call<Any>): Call<Any> {
                return SafeCall(call)
            }
        }
    }

    class SafeCall(private val delegate: Call<Any>) : Call<Any> {

        override fun enqueue(callback: Callback<Any>) {
            requireNonNull(callback, "callback == null")
            delegate.enqueue(object : Callback<Any> {

                override fun onResponse(
                    call: Call<Any>,
                    response: Response<Any>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body == null) {
                            val invocation = call.request().tag(Invocation::class.java)!!
                            val method = invocation.method()
                            val e = KotlinNullPointerException(
                                "Response from " +
                                        method.declaringClass.name +
                                        '.' +
                                        method.name +
                                        " was null but response body type was declared as non-null"
                            )
                            logE(TAG, "onResponse,body == null")
                            callback.onResponse(
                                this@SafeCall,
                                Response.success(ApiResponse.Failure(e))
                            )
                        } else {
                            logD(TAG, "onResponse,body success = $body")
                            callback.onResponse(this@SafeCall, Response.success(body))
                        }
                    } else {
                        val e = HttpException(response)
                        logE(TAG, "onResponse,body success = ${e.message}")
                        callback.onResponse(
                            this@SafeCall,
                            Response.success(ApiResponse.Failure(e))
                        )
                    }
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    logE(TAG, "onFailure,${t.message}")
                    callback.onResponse(
                        this@SafeCall,
                        Response.success(ApiResponse.Failure(t))
                    )
                }
            })
        }


        override fun clone(): Call<Any> {
            return delegate.clone()
        }

        override fun execute(): Response<Any> {
            return delegate.execute()
        }



        override fun isExecuted(): Boolean {
            return delegate.isExecuted
        }

        override fun cancel() {
            delegate.cancel()
        }

        override fun isCanceled(): Boolean {
            return delegate.isCanceled
        }

        override fun request(): Request {
            return delegate.request()
        }

        override fun timeout(): Timeout {
            return delegate.timeout()
        }

    }

}