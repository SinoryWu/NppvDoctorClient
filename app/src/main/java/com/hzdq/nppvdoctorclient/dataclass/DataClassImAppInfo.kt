package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/16
 *Author:Sinory
 *Description:
 */
data class DataClassImAppInfo(
    var code: String?,
    var `data`: DataImAppInfo?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataImAppInfo(
    var appKey: String?,
    var clientId: String?,
    var clientSecret: String?
)