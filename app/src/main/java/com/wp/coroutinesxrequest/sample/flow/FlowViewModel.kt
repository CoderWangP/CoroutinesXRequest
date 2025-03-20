package com.wp.coroutinesxrequest.sample.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.wp.coroutinesxrequest.components.BaseRequestViewModel
import com.wp.coroutinesxrequest.sample.api.API
import com.wp.coroutinesxrequest.toast
import com.wp.xrequest.http.collectSuccessDataOrNull
import com.wp.xrequest.http.exception.ApiException
import com.wp.xrequest.http.request
import com.wp.xrequest.toJson


class FlowViewModel : BaseRequestViewModel() {
    private val _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    val response1 = MutableLiveData<String>()

    val response2 = MutableLiveData<String>()

    companion object {
        private const val TAG = "FlowViewModel"
    }

    fun requestData() {
        request {
/*            API.banners4Flow().collectOrThrow {
                mSingleResponse.value = it.toJson()
            }*/
            /*API.login4Flow(JsonObject().apply {
                addProperty("username", "111")
                addProperty("password", "")
            }).collectOrThrow {
                mSingleResponse.value = it.toJson()
            }*/
            API.login4Flow(JsonObject().apply {
                addProperty("username", "111")
                addProperty("password", "")
            }).collectSuccessDataOrNull { data, e ->
                e?.run {
                    if (e is ApiException.BusinessException) {
                        toast(e.errorMsg ?: "")
                    }
                    _response.value = when (e) {
                        is ApiException.BusinessException -> e.apiResponse.toJson()
                        is ApiException.ResponseException -> e.errorMsg
                    }
                }
                data?.run {
                    _response.value = toJson()
                }
            }
        }
    }
}