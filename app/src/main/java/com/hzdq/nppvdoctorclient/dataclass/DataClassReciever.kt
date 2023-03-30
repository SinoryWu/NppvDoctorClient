package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/3/24
 *Author:Sinory
 *Description:
 */
data class DataClassReceiver(
    var fromUserId: Int?,
    var fromUserName: String?,
    var fromUserType: Int?,
    var messageContent: String?,
    var messageType: Int?,
    var conversationId:String?
)