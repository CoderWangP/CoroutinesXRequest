package com.wp.xrequest.http.typeadapter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.wp.xrequest.Config
import com.wp.xrequest.http.ApiResponse
import com.wp.xrequest.logD


class ApiResponseTypeAdapter : TypeAdapter<ApiResponse<*>>() {
    companion object{
        private const val TAG = "ApiResponseTypeAdapter"
    }

    override fun write(out: JsonWriter?, value: ApiResponse<*>?) {

    }

    override fun read(reader: JsonReader): ApiResponse<*> {
        reader.beginObject()
        logD(TAG,"read start")
        var code = 0
        var message = ""
        var data: Any? = null
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "code", Config.serializedName?.code -> code = reader.nextInt()
                "message", Config.serializedName?.message -> message = reader.nextString()
                "data", Config.serializedName?.data -> data =
                    Gson().fromJson<Any>(reader, Any::class.java)
            }
        }
        reader.endObject()
        logD(TAG,"read end")
        return ApiResponse.Success(data as Any, message, code)
    }
}