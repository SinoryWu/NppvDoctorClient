package com.hzdq.nppvdoctorclient.retrofit

import android.content.Context
import com.hzdq.nppvdoctorclient.util.Shp

class URLCollection {


    companion object{
//        val H5_BASE_URL = "http://192.168.0.154:5174"


//        val HYBRID_URL  = "http://test-pap-patient-h5.bajiesleep.com?token="
        val HYBRID_URL  = "https://pap-patient-h5.bajiesleep.com?token="
//        val H5_BASE_URL = "http://test-nppv-assistant-h5.bajiesleep.cn"
        val H5_BASE_URL = "https://nppv-assistant-h5.bajiesleep.cn"
//        val BASE_URL = "http://test-hx.bajiesleep.com"
        val BASE_URL = "https://pap.bajiesleep.com"


        val HELP_URL = "https://bajiesleep.com/help/nppv_app_list.html"
        val USER_AGREEMENT = "https://www.bajiesleep.com/yonghuxieyi.html"
        val PRIVACY_AGREEMENT = "https://www.bajiesleep.com/yinsixieyi.html"

        val COMPRESS_PICTURES = "?x-oss-process=image/resize,h_150,m_lfit"

        //服务列表
        fun getServiceList(shp: Shp):String{
            //服务列表
            val SERVICE_LIST = "${H5_BASE_URL}/server?token=${shp.getToken()}&isDoctor=${if (shp.getRoleType() == 2) 1 else 0}"
            return SERVICE_LIST
        }

        //患者列表
        fun getPatientList(shp: Shp):String{
            val PATIENT_LIST = "${H5_BASE_URL}/patient?token=${shp.getToken()}&isDoctor=${if (shp.getRoleType() == 2) 1 else 0}"
            return PATIENT_LIST
        }
        //患者详情
        fun getPatientDetail(shp: Shp,id:Int):String{
            val PATIENT_DETAIL = "/patient/detail?token=${shp.getToken()}&id=$id"
            return PATIENT_DETAIL
        }

        //医生列表
        fun getDoctorList(shp:Shp):String{
            val DOCTOR_LIST = "${H5_BASE_URL}/doctor?token=${shp.getToken()}"
            return DOCTOR_LIST
        }
        //医生详情
        fun getDoctorDetail(shp:Shp,id:Int):String{
            val DOCTOR_DETAIL = "/doctor/detail?token=${shp.getToken()}&id=$id"
            return DOCTOR_DETAIL
        }


        fun newServers(shp: Shp):String{
            val NEW_SERVERS = "${H5_BASE_URL}/server/new?token=${shp.getToken()}"
            return NEW_SERVERS

        }

        fun getExistingPatientsList(shp: Shp):String{
            val EXISTING_PATIENT_LIST = "${H5_BASE_URL}/server/edit?token=${shp.getToken()}"
            return EXISTING_PATIENT_LIST

        }
    }


}