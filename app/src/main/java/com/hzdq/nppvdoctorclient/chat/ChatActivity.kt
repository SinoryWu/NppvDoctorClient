package com.hzdq.nppvdoctorclient.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.bean.Person
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
*@desc 聊天界面
*@Author Sinory
*@date 2023/3/16 12:46
*/
class ChatActivity : AppCompatActivity() {
    private var tokenDialogUtil: TokenDialogUtil? = null


    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        ActivityCollector.addActivity(this)



    }




}