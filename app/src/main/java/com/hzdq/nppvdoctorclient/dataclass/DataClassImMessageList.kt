package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */
data class DataClassImMessageList(
    var code: String?,
    var `data`: DataImMessageList?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataImMessageList(
    var boolLastPage: Boolean?,
    var list: MutableList<ImMessageList>?,
    var pageNum: Int?,
    var pageSize: Int?,
    var pages: Int?,
    var total: Int?
)

data class ImMessageList(
    var id:Int?,
    var chatType: Int?,
    var formUser: FormUser?,
    var gmtCreate: String?,
    var message: String?,
    var messageType: Int?,
    var msgThirdPartyId: String?,
    var oneself: Boolean?
)

data class FormUser(
    var imUserId: Int?,
    var userId: Int?,
    var userName: String?,
    var userThirdPartyId: String?,
    var userType: Int?
)