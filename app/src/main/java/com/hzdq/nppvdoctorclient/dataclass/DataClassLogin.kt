package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/16
 *Author:Sinory
 *Description:
 */
 data class DataClassLogin(
    var code: String?,
    var `data`: DataLogin?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataLogin(
    var doctorDepartment: String?,
    var doctorPosition: String?,
    var hospitalId: Int?,
    var hospitalName: String?,
    var mobile: String?,
    var name: String?,
    var roleType: Int?,
    var token: String?,
    var uid: Int?,
    var userType: Int?,
    var weakPassword: Boolean?
)