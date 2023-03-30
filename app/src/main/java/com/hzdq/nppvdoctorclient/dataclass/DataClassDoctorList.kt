package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/27
 *Author:Sinory
 *Description:
 */
data class DataClassDoctorList(
    var code: String?,
    var `data`: DataDoctorList?,
    var errorDetail: Any?,
    var msg: String?,
    var tid: String?
)

data class DataDoctorList(
    var boolLastPage: Boolean?,
    var list: MutableList<DoctorList>?,
    var pageNum: Int?,
    var pageSize: Int?,
    var pages: Int?,
    var total: Int?
)

data class DoctorList(
    var adaptabilityCount: Int?,
    var doctorDepartment: Any?,
    var doctorPosition: Any?,
    var hospitalName: String?,
    var imUserId: Int?,
    var longTermServiceCount: Int?,
    var name: String?,
    var roleType: Int?,
    var uid: Int?,
    var userType: Int?,
    var position: Int?,
    var pinyin:String?
)