package com.wp.coroutinesxrequest.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.wp.coroutinesxrequest.components.BaseRequestViewModel
import com.wp.coroutinesxrequest.components.Status
import com.wp.coroutinesxrequest.sample.api.API
import com.wp.coroutinesxrequest.sample.api.TestApi
import com.wp.coroutinesxrequest.sample.model.Banner
import com.wp.coroutinesxrequest.toast
import com.wp.xrequest.http.asyncRequest
import com.wp.xrequest.http.checkResponse
import com.wp.xrequest.http.exceptionOrNull
import com.wp.xrequest.http.getOrElse
import com.wp.xrequest.http.getOrNull
import com.wp.xrequest.http.getOrThrow
import com.wp.xrequest.http.launchRequest
import com.wp.xrequest.http.jobScope
import com.wp.xrequest.http.zip
import com.wp.xrequest.logD
import com.wp.xrequest.logE
import com.wp.xrequest.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response


class MainViewModelPlan : BaseRequestViewModel() {
    private val _response4CustomCoroutineScope = MutableLiveData<String>()
    val response4CustomCoroutineScope: LiveData<String> = _response4CustomCoroutineScope


    private val _singleResponse = MutableLiveData<String>()
    val singleResponse: LiveData<String> = _singleResponse

    private val _response1 = MutableLiveData<String>()
    val response1: LiveData<String> = _response1

    private val _response2 = MutableLiveData<String>()
    val response2: LiveData<String> = _response2

    private val _zipResponse1 = MutableLiveData<String>()
    val zipResponse1: LiveData<String> = _zipResponse1

    private val _zipResponse2 = MutableLiveData<String>()
    val zipResponse2: LiveData<String> = _zipResponse2


    private val _exceptionResponse1 = MutableLiveData<String>()
    val exceptionResponse1: LiveData<String> = _exceptionResponse1

    private val _exceptionResponse2 = MutableLiveData<String>()
    val exceptionResponse2: LiveData<String> = _exceptionResponse2


    private val loginBody = JsonObject().apply {
        addProperty("username", "111")
        addProperty("password", "")
    }

    companion object {
        private const val TAG = "MainViewModelPlan"
    }

    fun request4CustomCoroutineScope() {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.jobScope {
            val result = launchRequest {
                API.question2()
            }.getOrElse {
                toast(it.message ?: "")
            }
            result.checkAndUpdateStatus<JsonObject> {
                _response4CustomCoroutineScope.value = it.toJson()
            }
        }
    }

    /**
     * 单个请求
     */
    fun requestSingle() {
        /*        val method = TestApi::class.java.getDeclaredMethod("question5")
                val type = method.genericParameterTypes*/
        /*runBlocking {
            val result = API.question3()
            _singleResponse.value = result.toJson()
        }*/

        jobScope {
            onRequestStatusUpdate(Status.START)
            /*val result1 = launchRequest { API.banners4JsonParseException() }
            val exception = result1.exceptionOrNull()
            exception?.run {
                logE(TAG, exception.errorMsg)
                _singleResponse.value = this.errorMsg ?: ""
            }
            result1.getOrNull()?.run {
                _singleResponse.value = this.toJson()
            }*/

            val result2 = launchRequest { API.banners2() }.getOrThrow()
            result2.checkAndUpdateStatus<List<Banner>> {
                _singleResponse.value = it.size.toString() + it.toJson()
            }

            /*val result1 = API.question3()
            result1.run {
                _singleResponse.value = this.toJson()
            }*/

            /*val result2 = launchRequest {
                API.banners4JsonParseException()
            }.getOrElse {
                toast(it.message ?: "")
            }
            result2.checkAndUpdateStatus {
                _singleResponse.value = this.toJson()
            }

            val result3 = launchRequest {
                API.banners4JsonParseException()
            }.getOrElse {
                toast(it.message ?: "")
            }
            result3.checkAndUpdateStatus<Banner> {
                _singleResponse.value = it.toJson()
            }*/

        }
    }

    fun requestMultiple() {
        jobScope {
            onRequestStatusUpdate(Status.START)
            val deferred1 = asyncRequest {
                // delay(5000)
                throw IllegalStateException("test error intercept http request.")
                API.question2()
            }
            /*       val deferred1 = asyncRequest {
                       API.question2()
                   }*/
            val deferred2 = asyncRequest {
                // delay(5000)
                API.banners2()
            }
            val result1 = deferred1.getOrNull(true)
            result1?.run {
                _response1.value = toJson()
            } ?: kotlin.run {
                val exception1 = deferred1.exceptionOrNull()
                _response1.value = exception1?.errorMsg ?: ""
            }

            val result2 = deferred2.getOrNull()
            result2.checkAndUpdateStatus<MutableList<JsonObject>> {
                _response2.value = it.toJson()
            }
        }
    }


    fun requestMultipleZip() {
        jobScope {

            onRequestStatusUpdate(Status.START)
            /*         val deferred1 = asyncRequest {
                         API.banners4JsonParseException()
                     }*/
            val deferred1 = asyncRequest {
                API.banners2()
            }
            val deferred2 = asyncRequest {
                API.question2()
            }
            zip(deferred1, deferred2) { t1, t2, e ->
                e?.run {
                    _zipResponse1.value = errorMsg ?: ""
                } ?: kotlin.run {
                    _zipResponse1.value = t1?.toJson()
                    _zipResponse2.value = t2?.toJson()
                }
            }
        }
    }

    fun requestException() {
        jobScope {
            logD(TAG, "requestException start")
            val await1 = asyncRequest { API.question2() }
            val await2 = asyncRequest { API.banners4JsonParseException() }
            val result1 = await1.getOrNull()
            logD(TAG, "requestException result1 end")
            result1.checkResponse<JsonObject> {
                _exceptionResponse1.value = it.toJson()
            }
            try {
                logD(TAG, "requestException result2 start")
                val result2 = await2.await()
                logD(TAG, "requestException result2 end")
                _exceptionResponse2.value = result2.toJson()
            } catch (e: Throwable) {
                logE(TAG, e.message + "error ")
            }

            /*val result2 = await2.getOrNull()
            logE(TAG, "result2 = ${result2?.toJson()}")
            val exception = await2.exceptionOrNull()
            logE(TAG, "error = ${exception?.errorMsg}")*/
            /*val result2 = await2
                .onFailure {
                    logE(TAG, it.errorMsg)
                    toast(it.message ?: "")
                }*/
            //val result22 = result2.getOrNull(preprocessing = false)
            /*result2.checkAndUpdateStatus<Banner> {

            }*/
        }
    }


    private fun createUnAuthException(): HttpException {

        return HttpException(
            Response.error<JsonObject>(
                401, okhttp3.Response.Builder() //
                    .body("".toResponseBody())
                    .code(401)
                    .message("请登录")
                    .protocol(Protocol.HTTP_1_1)
                    .request(Request.Builder().url("http://localhost/").build())
                    .build().body
            )
        )
    }
}