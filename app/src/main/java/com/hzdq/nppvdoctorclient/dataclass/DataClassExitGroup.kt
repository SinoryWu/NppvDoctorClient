package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/26
 *Author:Sinory
 *Description:
 */
data class DataClassExitGroup(
    var code: String?,
    var `data`: DataExitGroup?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataExitGroup(
    var imToken: String?,
    var imUser: ImUserExitGroup?,
    var imUserName: String?
)

data class ImUserExitGroup(
    var deleted: Int?,
    var gmtCreate: String?,
    var gmtModify: String?,
    var id: Int?,
    var passWord: String?,
    var userId: Int?,
    var userName: String?,
    var userThirdPartyId: String?,
    var userType: Int?
)