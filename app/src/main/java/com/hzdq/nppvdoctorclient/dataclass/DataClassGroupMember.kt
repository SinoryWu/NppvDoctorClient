package com.hzdq.nppvdoctorclient.dataclass

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 *Time:2023/3/24
 *Author:Sinory
 *Description:
 */
@Parcelize
data class DataClassGroupMember(
    @SerializedName("code")
    var code: String?,
    @SerializedName("data")
    var `data`: MutableList<DataGroupMember?>?,
    @SerializedName("errorDetail")
    var errorDetail: String?,
    @SerializedName("msg")
    var msg: String?,
    @SerializedName("tid")
    var tid: String?
): Parcelable

@Parcelize
data class DataGroupMember(
    @SerializedName("imUserId")
    var imUserId: Int?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("uid")
    var uid: Int?,
    @SerializedName("userType")
    var userType: Int?
): Parcelable