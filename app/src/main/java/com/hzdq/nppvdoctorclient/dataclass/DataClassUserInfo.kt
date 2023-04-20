package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/24
 *Author:Sinory
 *Description:
 */
data class DataClassUserInfo(
    var code: String?,
    var `data`: DataUserInfo?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataUserInfo(
    var hospitalName: String?,
    var name: String?,
    var mobile: String?,
    var roleType: Int?,
    var token: String?,
    var uid: Int?,
    var doctorDepartment:String?,
    var doctorPosition:String
)