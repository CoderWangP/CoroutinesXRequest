package com.wp.coroutinesxrequest.sample.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.wp.coroutinesxrequest.http.HttpRequestManager
import com.wp.coroutinesxrequest.sample.model.Banner
import com.wp.coroutinesxrequest.sample.model.Contributor
import com.wp.coroutinesxrequest.sample.model.TestModel
import com.wp.xrequest.http.ApiResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

val API by lazy {
    HttpRequestManager.createApi(TestApi::class.java)
}

interface TestApi {

    /**
     * @param owner : square
     * @param repo : retrofit
     */
    /*    @GET("/repos/{owner}/{repo}/contributors")
        suspend fun contributors(
            @Path("owner") owner: String?,
            @Path("repo") repo: String?
        ): MutableList<JsonObject>*/


    /**
     * @param owner : square
     * @param repo : retrofit
     */
    /*    @GET("/repos/{owner}/{repo}/contributors")
        fun contributors2(
            @Path("owner") owner: String?,
            @Path("repo") repo: String?
        ): Call<MutableList<JsonObject>>*/


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


    @GET("/banner/json")
    suspend fun banners4JsonParseException(): ApiResponse<Banner>




    @GET("/wenda/comments/1/json")
    suspend fun question4(): TestModel

    @GET("/repos/{owner}/{repo}/contributors")
    suspend fun contributors(
        @Path("owner") owner1: String,
        @Path("repo") repo1: String
    ): List<Contributor>


    /**
     * 登录
     */
    @POST("/user/login")
    fun login4Flow(@Body jsonObject: JsonObject): Flow<ApiResponse<JsonElement>>

}