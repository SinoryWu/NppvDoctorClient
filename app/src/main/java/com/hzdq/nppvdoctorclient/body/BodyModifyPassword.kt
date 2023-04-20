package com.hzdq.nppvdoctorclient.body

/**
 *Time:2023/4/11
 *Author:Sinory
 *Description:
 */
data class BodyModifyPassword(
    var duplicatePassword: String?,
    var newPassword: String?,
    var phone: String?,
    var verificationCode: String?
)