package com.wp.coroutinesxrequest.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class TestInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
/*        if(true){
            throw NonIOException("IOException")
        }*/
        return chain.proceed(chain.request())
    }

    inner class NonIOException(message:String):IOException(message)
}