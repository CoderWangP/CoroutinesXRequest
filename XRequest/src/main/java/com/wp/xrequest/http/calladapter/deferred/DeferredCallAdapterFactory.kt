package com.wp.request.http.calladapter.deferred

import com.wp.xrequest.http.calladapter.deferred.DeferredCallAdapter
import kotlinx.coroutines.Deferred
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class DeferredCallAdapterFactory private constructor():CallAdapter.Factory() {
    companion object{
        fun create(): DeferredCallAdapterFactory {
            return DeferredCallAdapterFactory()
        }
    }
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //Deferred<Any>
        if (getRawType(returnType) != Deferred::class.java) {
            return null
        }
        require(returnType is ParameterizedType) {
            "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>"
        }
        //Deferred<Any>
        val responseType = getParameterUpperBound(0, returnType)
        return DeferredCallAdapter<Any>(responseType)
    }
}