package com.tg.unitylibrary.Module

/**
 * APP通用数据
 */
interface UnityAppInfoModule {
    /**
     * 获取bundleId
     * 如果和包名一样，可以直接返回null
     */
    val bundleId: String?

    /**
     * 获取登录返回的accessToken
     */
    val accessToken: String?

    /**
     * 获取设备ID（数美ID）
     */
    val deviceId: String?

    /**
     * 获取网络类型  wifi,2G,3G,4G,5G
     */
    val netType: String?

    /**
     * 获取app的市场渠道
     */
    val appChannel: String?

    /**
     * 客户端api环境
     */
    val apiEnvironment: String?

    /**
     * 获取用户uid
     * @return
     */
    val uid: String?

    /**
     * 获取Udid
     */
    val uDID: String?
}