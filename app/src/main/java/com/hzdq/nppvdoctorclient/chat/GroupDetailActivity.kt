package com.hzdq.nppvdoctorclient.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.ActivityGroupDetailBinding
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil
/**
*@desc 群设置（详情）
*@Author Sinory
*@date 2023/3/16 12:46
*/
class GroupDetailActivity : AppCompatActivity() {
    private var tokenDialogUtil: TokenDialogUtil? = null
    private lateinit var binding:ActivityGroupDetailBinding
    private val ADD_DOCTOR_REQUEST_CODE = 0x000014
    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_group_detail)

        ActivityCollector.addActivity(this)
        initView()
    }

    private fun initView(){
        binding.head.content.text = "群设置"
        binding.head.back.setOnClickListener {
            finish()
        }

        binding.head.add.visibility = View.VISIBLE

        binding.head.add.setOnClickListener {
            val intent = Intent(this,AddDoctorActivity::class.java)
            startActivityForResult(intent,ADD_DOCTOR_REQUEST_CODE)
        }
    }
}