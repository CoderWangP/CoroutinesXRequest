package com.wp.coroutinesxrequest



object AppModule {

    private lateinit var mApplication:CoroutinesRequestApplication

    fun init(application: CoroutinesRequestApplication){
        mApplication = application
    }

    fun provideApp():CoroutinesRequestApplication{
        return mApplication
    }
}