package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/16
 *Author:Sinory
 *Description:
 */
data class DataClassUserImToken(
    var code: String?,
    var `data`: DataUserImToken?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataUserImToken(
    var imToken: String?,
    var imUserName: String?
)