package com.hzdq.nppvdoctorclient.dataclass

/**
 *Time:2023/4/6
 *Author:Sinory
 *Description:
 */
data class DataClassDoctorLoad(
    var code: String?,
    var `data`: DataDoctorLoad?,
    var errorDetail: String?,
    var msg: String?,
    var tid: String?
)

data class DataDoctorLoad(
    var adaptabilityCount: Int?,
    var doctorDepartment: String?,
    var doctorPosition: String?,
    var hospitalName: String?,
    var imUserId: Int?,
    var longTermServiceCount: Int?,
    var mobile: String?,
    var name: String?,
    var packageList: List<PackageDoctorLoad?>?,
    var roleType: Int?,
    var uid: Int?,
    var userType: Int?
)

data class PackageDoctorLoad(
    var adaptabilityId: Int?,
    var ahi: String?,
    var avgSpo2: String?,
    var createUserId: Int?,
    var doctorAssistantId: Int?,
    var doctorAssistantName: String?,
    var doctorId: Int?,
    var doctorName: String?,
    var doctorUserId: Int?,
    var gmtCreate: String?,
    var gmtModify: String?,
    var groupId: Int?,
    var guidanceEndTime: String?,
    var guidanceStartTime: String?,
    var hospitalId: Int?,
    var hospitalName: String?,
    var id: Int?,
    var lowSpo2: String?,
    var maskType: String?,
    var medicalHistoryId: Int?,
    var odi: String?,
    var packageNumber: String?,
    var packageState: Int?,
    var packageType: Int?,
    var patientId: Int?,
    var patientName: String?,
    var pipeModel: String?,
    var pressureTitrationId: Int?,
    var recordContent: String?,
    var refundRemarks: String?,
    var remarks: String?,
    var respiratorSn: String?,
    var returnCourierNumber: String?,
    var ringSn: String?,
    var sendCourierNumber: String?,
    var sleepSn: String?
)