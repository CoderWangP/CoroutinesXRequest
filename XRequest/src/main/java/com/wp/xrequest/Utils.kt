package com.wp.xrequest

import android.util.Log
import android.widget.Toast
import com.google.gson.Gson

/**
 *
 * extension utils
 *
 */
fun Any.logD(msg: Any?) {
    logD(this.javaClass.simpleName, msg)
}

fun Any.logE(msg: Any?) {
    logE(this.javaClass.simpleName, msg)
}

fun logD(tag: String, msg: Any?) {
    if (Config.enableLog) {
        Log.d(tag, "thread = ${Thread.currentThread().name} -> $msg")
    }
}

fun logE(tag: String, msg: Any?) {
    if (Config.enableLog) {
        Log.e(tag, "thread = ${Thread.currentThread().name} -> $msg")
    }
}

fun Any.toJson(): String {
    return Gson().toJson(this)
}

