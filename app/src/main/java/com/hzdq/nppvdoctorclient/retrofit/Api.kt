package com.hzdq.nppvdoctorclient.retrofit

import com.hzdq.nppvdoctorclient.body.*
import com.hzdq.nppvdoctorclient.dataclass.*
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface Api {
    //用户密码登录
    @POST("/hx_app/doctor/app/user/auth/v2/login")
    fun loginPassword(@Body bodyLoginPassword: BodyLoginPassword):Call<DataClassLogin>
    //手机号验证码登录
    @POST("/hx_app/doctor/app/user/auth/loginPhone")
    fun loginVerificationCode(@Body bodyLoginVerificationCode: BodyLoginVerificationCode):Call<DataClassLogin>
    //发送短信验证码
    @POST("/hx_app/base/sms/seed")
    fun sendMsg(@Body bodySendMsg: BodySendMsg):Call<DataClassNoData>
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
    //发送消息
    @POST("/hx_app/doctor/app/im/sendMessage")
    fun sendMessage(@Body bodySendMessage: BodySendMessage):Call<DataClassGeneralBoolean>
    //已读消息
    @POST("/hx_app/doctor/app/im/readAllMsg")
    fun readAllMsg(@Body bodyReadAllMsg: BodyReadAllMsg):Call<DataClassNoData>
    //上传文件
    @Multipart
    @POST("/hx_app/base/aly/oss/upload")
    fun uploadFile(@Part file: MultipartBody.Part):Call<DataClassFile>
    //获取用户个人信息
    @GET("/hx_app/doctor/app/user/load")
    fun getUserInfo():Call<DataClassUserInfo>
    //获取群成员
    @GET("/hx_app/doctor/app/im/listGroupMembers")
    fun getGroupMembers(@Query("groupId") groupId:Int):Call<DataClassGroupMember>
    //退群
    @GET("/hx_app/doctor/app/im/groupRemove")
    fun getGroupRemove(@Query("groupId") groupId:Int):Call<DataClassExitGroup>
    //获取所有医生/医助列表
    @POST("/hx_app/doctor/app/doctor/user/listPage")
    fun getDoctorList(@Body bodyDoctorList: BodyDoctorList):Call<DataClassDoctorList>
    //拉人入群
    @POST("/hx_app/doctor/app/im/groupInvitation")
    fun groupInvitation(@Body bodyGroupInvitation: BodyGroupInvitation):Call<DataClassNoData>
    //获取医生详情
    @GET("/hx_app/doctor/app/doctor/user/load")
    fun getDoctorLoad(@Query("uid") uid:Int):Call<DataClassDoctorLoad>
    //修改密码
    @POST("/hx_app/doctor/app/user/v2/changePassword")
    fun changePassword(@Body bodyModifyPassword: BodyModifyPassword):Call<DataClassGeneralBoolean>
    //获取最新版本
    @POST("/hx_app/app/version/getTheLatestVersion")
    fun postLatestVersion(@Body bodyVersion: BodyVersion):Call<DataClassVersion>
}