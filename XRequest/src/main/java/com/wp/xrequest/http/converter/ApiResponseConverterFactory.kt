package com.wp.xrequest.http.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.wp.xrequest.http.ApiResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiResponseConverterFactory private constructor(val gson: Gson) : Converter.Factory() {
    companion object {
        fun create(): ApiResponseConverterFactory {
            return ApiResponseConverterFactory(Gson())
        }

        fun create(gson: Gson): ApiResponseConverterFactory {
            return ApiResponseConverterFactory(gson)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return ApiRequestBodyConverter(gson, adapter)
    }

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (getRawType(type) != ApiResponse::class.java) {
            return null
        }
        require(type is ParameterizedType) { "Response type must be parameterized as ApiResponse<Foo> or ApiResponse<? extends Foo>" }
        val dataType = getParameterUpperBound(0, type)
        val dataTypeAdapter: TypeAdapter<*> = gson.getAdapter(TypeToken.get(dataType))
        return ApiResponseBodyConverter(gson, dataTypeAdapter)
    }

}