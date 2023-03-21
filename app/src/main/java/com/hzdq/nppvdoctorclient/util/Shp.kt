package com.hzdq.nppvdoctorclient.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class Shp(val context: Context) {
    fun saveToSp(key:String,value:String){
        context.getSharedPreferences("sp", AppCompatActivity.MODE_PRIVATE).apply {
            this.edit().putString(key,value).apply()
        }
    }

    fun saveToSpInt(key:String,value:Int){
        context.getSharedPreferences("sp", AppCompatActivity.MODE_PRIVATE).apply {
            this.edit().putInt(key,value).apply()
        }
    }

    fun saveToSpBoolean(key:String,value:Boolean){
        context.getSharedPreferences("sp", AppCompatActivity.MODE_PRIVATE).apply {
            this.edit().putBoolean(key,value).apply()
        }
    }

    fun saveToSpLone(key:String,value:Long){
        context.getSharedPreferences("sp", AppCompatActivity.MODE_PRIVATE).apply {
            this.edit().putLong(key,value).apply()
        }
    }

    fun getToken():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("token","")
    }



    fun getFirstLogin(): Boolean {
        val sp = context.getSharedPreferences("sp", Context.MODE_PRIVATE)
        return sp.getBoolean("firstLogin", true)
    }

    fun getPhone():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("phone","")
    }

    fun getAppKey():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("appKey","")
    }

    fun getClientId():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("clientId","")
    }

    fun getClientSecret():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("clientSecret","")
    }

    fun getImToken():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("imToken","")
    }

    fun getImUserName():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("imUserName","")
    }

    fun getTokenTimeMillis():Long?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getLong("tokenTimeMillis",0)
    }

    fun getConversationSearchName():String?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getString("ConversationSearchName","")
    }

    fun getRoleType():Int{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getInt("roleType",2)
    }

    fun getMessageIndex():Int?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getInt("MessageIndex",0)
    }

    fun getGroupId():Int?{
        val sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE)
        return sp.getInt("groupId",0)
    }


}