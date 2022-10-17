package com.tg.unitylibrary

import UnityApiService
import com.alibaba.fastjson.JSONObject
import com.ypp.net.ApiServiceManager
import com.ypp.net.lift.RxSchedulers
import io.reactivex.Flowable

/**
 * @author Created by helei
 * @data 18.3.22
 * Email:helei19910210@163.com
 * Description:
 */
object UnityApi {
     fun getRequest(requestUrl: String?, headers: Map<String?, String?>?): Flowable<Any>? {
        return if (headers == null) ApiServiceManager.getInstance().obtainService(
            UnityApiService::class.java
        )
            .getRequest(requestUrl)!!
            .compose(RxSchedulers.ioToMain()) else ApiServiceManager.getInstance().obtainService(
            UnityApiService::class.java
        )
            .getRequest(requestUrl, headers)!!
            .compose(RxSchedulers.ioToMain())
    }

     fun postRequest(
        requestUrl: String?,
        params: Map<String?, Any?>?,
        headers: Map<String?, String?>?
    ): Flowable<Any>? {
        return if (headers == null) ApiServiceManager.getInstance().obtainService(
            UnityApiService::class.java
        )
            .postRequest(requestUrl, params)!!
            .compose(RxSchedulers.ioToMain()) else ApiServiceManager.getInstance().obtainService(
            UnityApiService::class.java
        )
            .postRequest(requestUrl, params, headers)!!
            .compose(RxSchedulers.ioToMain())
    }

    fun getGateWayRequest(requestUrl: String?, headers: Map<String?, String?>?): Flowable<JSONObject>? {
        return if (headers == null) ApiServiceManager.getInstance().obtainService(
            UnityGateWayApiService::class.java
        )
            .getRequest(requestUrl)!!
            .compose(RxSchedulers.ioToMain()) else ApiServiceManager.getInstance().obtainService(
            UnityGateWayApiService::class.java
        )
            .getRequest(requestUrl, headers)!!
            .compose(RxSchedulers.ioToMain())
    }

    fun postGateWayRequest(
        requestUrl: String?,
        params: HashMap<String?, Any?>?,
        headers: Map<String?, String?>?
    ): Flowable<JSONObject>? {
        return if (headers == null) ApiServiceManager.getInstance().obtainService(
            UnityGateWayApiService::class.java
        )
            .postRequest(requestUrl, params)!!
            .compose(RxSchedulers.ioToMain()) else ApiServiceManager.getInstance().obtainService(
            UnityGateWayApiService::class.java
        )
            .postRequest(requestUrl, params, headers)!!
            .compose(RxSchedulers.ioToMain())
    }
}