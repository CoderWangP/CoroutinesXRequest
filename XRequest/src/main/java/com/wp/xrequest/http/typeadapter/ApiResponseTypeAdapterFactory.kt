package com.wp.xrequest.http.typeadapter

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.wp.xrequest.Config
import com.wp.xrequest.http.ApiResponse
import com.wp.xrequest.logD
import com.wp.xrequest.logE
import com.wp.xrequest.toJson
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType


class ApiResponseTypeAdapterFactory : TypeAdapterFactory {
    companion object {
        private const val TAG = "ApiResponseTypeAdapterFactory"
    }

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val returnType = type.rawType
        if (returnType != ApiResponse::class.java) {
            return null
        }
        val underlyingType = type.type
        require(underlyingType is ParameterizedType) { "ApiResponse return type must be parameterized as ApiResponse<Foo> or ApiResponse<? extends Foo>" }
        val dataType = getParameterUpperBound(0, underlyingType)
        val dataTypeToken = TypeToken.get(dataType)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter?, value: T) {}

            @Suppress("UNCHECKED_CAST")
            override fun read(reader: JsonReader): T {
                logD(TAG, "read start")
                val responseData: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                if (Config.enableLog) {
                    logD(TAG, "responseData = ${responseData.toJson()}")
                }
                val codeMemberName = Config.serializedName?.code ?: "code"
                val messageMemberName = Config.serializedName?.message ?: "message"
                val dataMemberName = Config.serializedName?.data ?: "data"
                if (!responseData.has(codeMemberName)) {
                    return ApiResponse.Failure(IllegalStateException("Json data must has code serialized member.")) as T
                }
                if (!responseData.has(dataMemberName)) {
                    return ApiResponse.Failure(IllegalStateException("Json data must has data serialized member.")) as T
                }
                return try {
                    val code = responseData.get(codeMemberName).asInt
                    val message = responseData.get(messageMemberName).asString ?: ""
                    val dataElement = responseData.get(dataMemberName)
                    val data = gson.fromJson(dataElement, dataTypeToken)
                    logD(TAG, "read success")
                    ApiResponse.Success(data, message, code) as T
                } catch (e: Throwable) {
                    val targetError =
                        if (e is JsonParseException) e else JsonSyntaxException(e.cause)
                    logE(TAG, "read failed:${e.message}")
                    ApiResponse.Failure(targetError) as T
                }
            }
        }.nullSafe()
    }


    private fun getParameterUpperBound(index: Int, type: ParameterizedType): Type? {
        val types = type.actualTypeArguments
        require(!(index < 0 || index >= types.size)) { "Index " + index + " not in range [0," + types.size + ") for " + type }
        val paramType = types[index]
        return if (paramType is WildcardType) {
            paramType.upperBounds[0]
        } else paramType
    }
}