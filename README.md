# CoroutinesXRequest

### Retrofit + Coroutines 快速简洁的处理请求工具

##### API 写法

    ```kotlin
    /**
     * 查看banner
     */
    @GET("/banner/json")
    suspend fun banners2(): ApiResponse<List<Banner>>

    /**
     * 问答
     */
    @GET("/wenda/comments/1/json")
    suspend fun question2(): ApiResponse<JsonObject>
    ```

##### 1.单个请求

    ```kotlin
    jobScope {
        val result = launchRequest { API.banners2() }.getOrThrow()
    }
    ```

##### 2.多个请求

    ```kotlin
    jobScope {
        val deferred1 = asyncRequest { API.question2() }
        val deferred2 = asyncRequest { API.banners2() }
        val result1 = deferred1.getOrNull()
        val result2 = deferred2.getOrNull()
    }
    ```

##### 3.多个请求合并

    ```kotlin
    jobScope {
        val deferred1 = asyncRequest { API.banners2() }
        val deferred2 = asyncRequest { API.question2() }
        zip(deferred1, deferred2) { t1, t2, e ->
            e?.run {
                _zipResponse1.value = errorMsg ?: ""
            } ?: kotlin.run {
                _zipResponse1.value = t1?.toJson()
                _zipResponse2.value = t2?.toJson()
            }
        }
    }
    ```
