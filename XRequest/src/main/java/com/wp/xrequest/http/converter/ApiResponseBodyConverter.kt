package com.wp.xrequest.http.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.wp.xrequest.Config
import com.wp.xrequest.http.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

class ApiResponseBodyConverter<T>(
    private var gson: Gson,
    private var dataAdapter: TypeAdapter<T>
) : Converter<ResponseBody, ApiResponse<T>> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): ApiResponse<T> {
        var code = 0
        var message = ""
        var data: T? = null
        return try {
            val jsonReader = gson.newJsonReader(value.charStream())
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val nextMemberName = jsonReader.nextName()
                when (nextMemberName) {
                    Config.serializedName?.code ?: "code" -> code = jsonReader.nextInt()
                    Config.serializedName?.message ?: "message" -> message = jsonReader.nextString()
                    Config.serializedName?.data ?: "data" -> data = dataAdapter.read(jsonReader)
                }
            }
            jsonReader.endObject()
            ApiResponse.Success(data!!, message, code)
        } catch (e: Throwable) {
            ApiResponse.Failure(e)
        } finally {
            value.close()
        }
    }
}