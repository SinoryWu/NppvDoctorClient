package com.hzdq.nppvdoctorclient.dataclass

data class DataClassVersion(
    var code: String?,
    var `data`: DataVersion?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataVersion(
    var downloadAddress: String?,
    var gmtCreate: String?,
    var gmtModify: String?,
    var id: Int?,
    var market: Int?,
    var mustUpdated: Int?,
    var systemType: Int?,
    var versionDescription: String?,
    var versionNo: String?
)