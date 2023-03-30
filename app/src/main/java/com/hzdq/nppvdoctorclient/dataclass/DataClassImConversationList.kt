package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */
data class DataClassImConversationList(
    var code: String?,
    var `data`: DataImConversationList?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataImConversationList(
    var boolLastPage: Boolean?,
    var list: List<ImConversationList>?,
    var pageNum: Int?,
    var pageSize: Int?,
    var pages: Int?,
    var total: Int?
)

data class ImConversationList(
    var gmtCreate: String?,
    var groupId: Int?,
    var groupName: String?,
    var groupThirdPartyId: String?,
    var lastMessage: String?,
    var lastMessageType: Int?,
    var lastMsgTime: String?,
    var joinState:Int?,
    var exitTime:String?,
    var numberOfUnreadMessages: Int?,
    var packageId: Int?
)