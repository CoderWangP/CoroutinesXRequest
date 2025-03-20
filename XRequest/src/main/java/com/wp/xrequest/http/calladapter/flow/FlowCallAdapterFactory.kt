package com.wp.xrequest.http.calladapter.flow

import kotlinx.coroutines.flow.*
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 *
 * refer to [retrofit2.DefaultCallAdapterFactory]
 *
 * [HttpServiceMethod]
 * [KotlinExtensions.await]
 *
 *
 *
 */
class FlowCallAdapterFactory private constructor() : CallAdapter.Factory() {
    companion object {
        fun create(): FlowCallAdapterFactory {
            return FlowCallAdapterFactory()
        }
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //Flow<HttpResponse>
        if (getRawType(returnType) != Flow::class.java) {
            return null
        }
        require(returnType is ParameterizedType) {
            "Flow return type must be parameterized as Flow<Foo> or Flow<out Foo>"
        }
        //Flow<Any>
        val responseType = getParameterUpperBound(0, returnType)
        return FlowCallAdapter<Any>(responseType)
    }
}