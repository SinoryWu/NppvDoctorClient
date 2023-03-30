package com.hzdq.nppvdoctorclient.body

/**
 *Time:2023/3/27
 *Author:Sinory
 *Description:
 */
data class BodyDoctorList(
    var hospitalId: Int?,
    var name: String?,
    var pageNum: Int?,
    var pageSize: Int?,
    var roleType: Int?
)