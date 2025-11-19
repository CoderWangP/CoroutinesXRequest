package com.wp.coroutinesxrequest.http

import android.widget.Toast
import androidx.annotation.StringRes
import com.google.gson.JsonParseException
import com.wp.coroutinesxrequest.AppModule
import com.wp.coroutinesxrequest.R
import com.wp.coroutinesxrequest.config.GlobalConfig
import com.wp.xrequest.http.exception.ApiException
import com.wp.xrequest.http.exception.IExceptionHandler
import com.wp.xrequest.logD
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.cancellation.CancellationException


class ExceptionHandlerImpl : IExceptionHandler {

    companion object {
        private const val TAG = "ExceptionHandlerImpl"

        private const val UNAUTHORIZED = 401
        private const val FORBIDDEN = 403
        private const val NOT_FOUND = 404
        private const val REQUEST_TIMEOUT = 408
        private const val INTERNAL_SERVER_ERROR = 500
        private const val BAD_GATEWAY = 502
        private const val SERVICE_UNAVAILABLE = 503
        private const val GATEWAY_TIMEOUT = 504

        private const val PARSE_ERROR = -11
        private const val CONNECT_ERROR = -12
        private const val SSL_HANDSHAKE_ERROR = -13
        private const val SOCKET_TIME_OUT = -14
        private const val SOCKET_ERROR = -15
        private const val UNKNOWN_HOST = -16
        private const val UNKNOWN = -17
        private const val CANCEL_REQUEST = -18//请求取消
    }


    override fun handle(error: ApiException, preprocessing: Boolean): ApiException {
        when (error) {
            is ApiException.ResponseException -> {
                val cause = error.exCause
                logD(TAG, "origin error = ${cause?.message}")
                when (cause) {
                    is HttpException -> {
                        val code = cause.code()
                        if (code == UNAUTHORIZED) {
                            //TODO other action to handle UNAUTHORIZED
                            throw CancellationException(cause)
                        }
                        val errorMsg = when (code) {
                            FORBIDDEN, NOT_FOUND, REQUEST_TIMEOUT,
                            INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE,
                            GATEWAY_TIMEOUT -> if (GlobalConfig.TEST) cause.message() else getString(
                                R.string.net_error
                            )

                            else -> if (GlobalConfig.TEST) cause.message() else getString(R.string.net_error)
                        }
                        onResponseException(code, errorMsg, preprocessing)
                    }

                    is JsonParseException, is JSONException -> {
                        logD(TAG, "JsonParseException or JSONException")
                        onResponseException(
                            PARSE_ERROR,
                            getString(R.string.parse_error),
                            preprocessing
                        )
                    }

                    is ConnectException -> onResponseException(
                        CONNECT_ERROR,
                        getString(R.string.connect_error),
                        preprocessing
                    )


                    is SSLHandshakeException -> onResponseException(
                        SSL_HANDSHAKE_ERROR,
                        getString(R.string.ssl_handshake_error),
                        preprocessing
                    )

                    is SocketTimeoutException -> onResponseException(
                        SOCKET_TIME_OUT,
                        getString(R.string.socket_timeout_error),
                        preprocessing
                    )

                    is CancellationException -> throw cause

                    is SocketException ->
                        onResponseException(
                            SOCKET_ERROR,
                            getString(R.string.network_unable_use),
                            preprocessing
                        )

                    is UnknownHostException -> onResponseException(
                        UNKNOWN_HOST,
                        getString(R.string.network_unable_use),
                        preprocessing
                    )

                    else -> onResponseException(
                        UNKNOWN,
                        if (GlobalConfig.TEST) cause?.message
                            ?: "" else getString(R.string.unknown_error),
                        preprocessing
                    )
                }
            }

            is ApiException.BusinessException -> onBusinessException(error, preprocessing)
        }
        return error
    }

    private fun onResponseException(code: Int, message: String, preprocessing: Boolean) {
        if (preprocessing) {
            toast(message)
        }
    }


    private fun onBusinessException(
        error: ApiException.BusinessException,
        preprocessing: Boolean
    ) {
        val response = error.apiResponse
        if (preprocessing) {
            toast(response.message)
        }
    }

    private fun getString(@StringRes resId: Int): String {
        return AppModule.provideApp().getString(resId)
    }


    private fun toast(msg: String?) {
        if (msg.isNullOrEmpty()) {
            return
        }
        Toast.makeText(AppModule.provideApp(), msg, Toast.LENGTH_SHORT).show()
    }

}