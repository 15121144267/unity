package com.tg.unitylibrary

import com.alibaba.fastjson.JSONObject
import com.tg.core.base.ApiConstant
import com.ypp.net.annotations.Host
import io.reactivex.Flowable
import retrofit2.http.*

/**
 * @author Created by helei
 * @data 18.3.22
 * Email:helei19910210@163.com
 * Description:
 */
@Host(ApiConstant.URL_RELEASE_GATEWAY)
interface UnityGateWayApiService {
    @POST
    fun postRequest(
        @Url url: String?,
        @Body body: Map<String?,@JvmSuppressWildcards Any?>?,
        @HeaderMap headers: Map<String?, String?>?
    ):@JvmSuppressWildcards Flowable<JSONObject?>?

    @POST
    fun postRequest(@Url url: String?, @Body body: Map<String?,@JvmSuppressWildcards Any?>?):@JvmSuppressWildcards Flowable<JSONObject?>?

    @GET
    fun getRequest(@Url url: String?, @HeaderMap headers: Map<String?, String?>?):@JvmSuppressWildcards Flowable<JSONObject?>?

    @GET
    fun getRequest(@Url url: String?):@JvmSuppressWildcards Flowable<JSONObject?>?
}