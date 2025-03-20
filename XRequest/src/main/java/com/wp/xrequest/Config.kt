package com.wp.xrequest

import com.wp.xrequest.http.exception.IExceptionHandler


/**
 *
 * config
 *
 */
object Config {

    private lateinit var config: Builder
    val errorHandler: IExceptionHandler?
        get() = config.errorHandler
    val enableLog
        get() = config.enableLog
    val serializedName
        get() = config.serializedName

    class Builder {
        var errorHandler: IExceptionHandler? = null
        var enableLog: Boolean = false
        var serializedName: SerializedName? = null
        fun enableLog(enableLog: Boolean) = apply {
            this.enableLog = enableLog
        }

        /**
         * if server data format is not same as define,
         * this property must be set
         */
        fun setSerializedName(
            code: String = "code",
            message: String = "message",
            data: String = "data"
        ) = apply {
            this.serializedName = SerializedName(code, message, data)
        }

        fun setExceptionHandler(errorHandler: IExceptionHandler) = apply {
            this.errorHandler = errorHandler
        }

        /**
         * server data format
         */
        data class SerializedName(
            val code: String = "code",
            val message: String = "message",
            val data: String = "data"
        )
    }

    @JvmStatic
    fun build(block: Builder.() -> Unit) {
        val builder = Builder().apply(block)
        config = builder
    }
}