package com.tg.unitylibrary

import com.tg.baselogin.user.model.UpdateModel
import com.ypp.net.annotations.Host
import com.ypp.net.bean.ResponseResult
import io.reactivex.Flowable
import retrofit2.http.POST

@Host("https://gateway.hibixin.com")
interface VersionUpdateApi {
    @POST("/mt/user/appVersion/checkAppVersion")
    fun requestUpdateInfo(): Flowable<ResponseResult<UpdateModel?>>
}
