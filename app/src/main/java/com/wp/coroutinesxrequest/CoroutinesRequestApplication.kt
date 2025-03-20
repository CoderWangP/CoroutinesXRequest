package com.wp.coroutinesxrequest

import android.app.Application
import com.facebook.stetho.Stetho
import com.wp.coroutinesxrequest.config.GlobalConfig
import com.wp.coroutinesxrequest.http.ExceptionHandlerImpl
import com.wp.xrequest.Config


class CoroutinesRequestApplication : Application() {

    companion object{
        private const val TAG = "CoroutinesRequestApplication"
    }

    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
        if(GlobalConfig.TEST){
            Stetho.initializeWithDefaults(this)
        }
        initRequest()

        testGlobalRequest()
    }

    private fun testGlobalRequest() {
/*        request {
            val result = myLaunch2 { API.question2() }.getORThrow()
            result?.run {
                logD(TAG,"result = ${toJson()}")
            }
        }*/
    }

    private fun initRequest() {
        Config.build {
            enableLog(GlobalConfig.TEST)
            setExceptionHandler(ExceptionHandlerImpl())
            setSerializedName("errorCode","errorMsg")
        }
    }
}