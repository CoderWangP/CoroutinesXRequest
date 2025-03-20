package com.wp.coroutinesxrequest.http

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.wp.coroutinesxrequest.config.GlobalConfig
import com.wp.request.http.calladapter.deferred.DeferredCallAdapterFactory
import com.wp.xrequest.http.calladapter.SafeCallAdapterFactory
import com.wp.xrequest.http.calladapter.flow.FlowCallAdapterFactory
import com.wp.xrequest.http.typeadapter.ApiResponseTypeAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


object HttpRequestManager {

    private const val TAG = "HttpRequestManager"

    private val sApiCache: ConcurrentHashMap<Class<*>, WeakReference<*>> by lazy { ConcurrentHashMap<Class<*>, WeakReference<*>>() }
    private val sRetrofit by lazy {
        val timeout = 30L
        val builder: OkHttpClient.Builder = OkHttpClient
            .Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)

        if (GlobalConfig.TEST) {
            builder
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addNetworkInterceptor(StethoInterceptor())
        }

        val rfBuilder = Retrofit
            .Builder()
            .client(builder.build())
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .addCallAdapterFactory(DeferredCallAdapterFactory.create())
            .addCallAdapterFactory(SafeCallAdapterFactory.create())
            .baseUrl("https://www.wanandroid.com")
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(ApiResponseTypeAdapterFactory())
            .setLenient()
            .create()

        rfBuilder.addConverterFactory(GsonConverterFactory.create(gson))

        rfBuilder.build()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createApi(tClass: Class<T>): T {
        val weakReference: WeakReference<*>? = sApiCache[tClass]
        val api = weakReference?.get()
        if (api != null) {
            return api as T
        } else {
            sApiCache.remove(tClass)
        }
        val t = sRetrofit.create(tClass) ?: throw RuntimeException("create api is null.")
        val cacheT = WeakReference(t)
        sApiCache[tClass] = cacheT
        return t
    }

}