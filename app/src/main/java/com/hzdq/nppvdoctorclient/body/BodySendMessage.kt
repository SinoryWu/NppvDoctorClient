package com.hzdq.nppvdoctorclient.body

/**
 *Time:2023/3/22
 *Author:Sinory
 *Description:
 */
data class BodySendMessage(
    var chatType: Int?,
    var message: String?,
    var messageType: Int?,
    var toId: Int?
)