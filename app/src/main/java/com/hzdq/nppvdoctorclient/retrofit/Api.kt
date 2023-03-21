package com.hzdq.nppvdoctorclient.retrofit

import com.hzdq.nppvdoctorclient.body.BodyImConversationList
import com.hzdq.nppvdoctorclient.body.BodyImMessageList
import com.hzdq.nppvdoctorclient.body.BodyLoginPassword
import com.hzdq.nppvdoctorclient.body.BodyLoginVerificationCode
import com.hzdq.nppvdoctorclient.dataclass.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {
    //用户密码登录
    @POST("/hx_app/doctor/app/user/auth/login")
    fun loginPassword(@Body bodyLoginPassword: BodyLoginPassword):Call<DataClassLogin>
    //手机号验证码登录
    @POST("/hx_app/doctor/app/user/auth/loginPhone")
    fun loginVerificationCode(@Body bodyLoginVerificationCode: BodyLoginVerificationCode):Call<DataClassLogin>
    //登出
    @GET("/hx_app/doctor/app/user/logout")
    fun logOut():Call<DataClassGeneralBoolean>
    //获取im app详细信息
    @GET("/hx_app/doctor/app/im/loadAppInfo")
    fun getImAppInfo():Call<DataClassImAppInfo>
    //获取当前用户回话token
    @GET("/hx_app/doctor/app/im/loadUserImToken")
    fun getUserImToken():Call<DataClassUserImToken>
    //获取对话列表
    @POST("/hx_app/doctor/app/im/imConversationList")
    fun getImConversationList(@Body bodyImConversationList: BodyImConversationList):Call<DataClassImConversationList>
    //获取消息列表
    @POST("/hx_app/doctor/app/im/imMessageList")
    fun getImMessageList(@Body bodyImMessageList: BodyImMessageList):Call<DataClassImMessageList>
}