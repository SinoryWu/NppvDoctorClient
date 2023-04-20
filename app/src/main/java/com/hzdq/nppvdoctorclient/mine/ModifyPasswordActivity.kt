package com.hzdq.nppvdoctorclient.mine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodyModifyPassword
import com.hzdq.nppvdoctorclient.body.BodySendMsg
import com.hzdq.nppvdoctorclient.databinding.ActivityModifyPasswordBinding
import com.hzdq.nppvdoctorclient.login.LoginActivity
import com.hzdq.nppvdoctorclient.util.*
import kotlin.math.min

class ModifyPasswordActivity : AppCompatActivity() {
    private lateinit var mineViewModel: MineViewModel
    private lateinit var binding:ActivityModifyPasswordBinding
    private var tokenDialogUtil:TokenDialogUtil? = null
    private lateinit var shp: Shp
    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        tokenDialogUtil = TokenDialogUtil(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_modify_password)
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        shp = Shp(this)
        initView()
        getCode()
    }

    private fun initView(){
        binding.head.content.text = "修改密码"
        binding.head.back.setOnClickListener {
            finish()
        }
        binding.phone.text = shp.getPhone()


        binding.newPassword.content.text = "新密码"
        binding.repeatPassword.content.text = "重复密码"
        val dataID = "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890";
        binding.newPassword.edit.setKeyListener(object : DigitsKeyListener(){
            override fun getInputType(): Int {
                return EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
            }

            override fun getAcceptedChars(): CharArray {
                val  data =dataID.toCharArray();
                return data
            }
        })

        binding.repeatPassword.edit.setKeyListener(object : DigitsKeyListener(){
            override fun getInputType(): Int {
                return EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
            }

            override fun getAcceptedChars(): CharArray {
                val  data =dataID.toCharArray();
                return data
            }
        })
        binding.newPassword.edit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               mineViewModel.newPassword.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.repeatPassword.edit.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mineViewModel.repeatPassword.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })



        binding.confirm.setOnClickListener {
            if (binding.code.text.toString().equals("")){
                ToastUtil.showToast(this,"请输入验证码")
                return@setOnClickListener
            }
            if (mineViewModel.newPassword.value.equals("")){
                ToastUtil.showToast(this,"请输入新密码")
                return@setOnClickListener
            }
            if (!mineViewModel.newPassword.value.equals(mineViewModel.repeatPassword.value)){
                ToastUtil.showToast(this,"两次密码不一致")
                return@setOnClickListener
            }

            if (mineViewModel.newPassword.value!!.length < 6 || mineViewModel.newPassword.value!!.length > 20){
                ToastUtil.showToast(this,"密码长度6～20个字符，不能为纯数字或纯字母")
                return@setOnClickListener
            }

            if (PasswordUtil.isAllLetters(mineViewModel.newPassword.value!!)){
                ToastUtil.showToast(this,"密码不能为纯字母")
                return@setOnClickListener
            }
            if (PasswordUtil.isNumeric(mineViewModel.newPassword.value!!)){
                ToastUtil.showToast(this,"密码不能为纯数字")
                return@setOnClickListener
            }
            val bodyModifyPassword = BodyModifyPassword(mineViewModel.repeatPassword.value,mineViewModel.newPassword.value,shp.getPhone(),binding.code.text.toString())
//            val bodyModifyPassword = BodyModifyPassword(mineViewModel.repeatPassword.value,mineViewModel.newPassword.value,"15355090637",binding.code.text.toString())
            mineViewModel.changePassword(bodyModifyPassword)
        }

        mineViewModel.changeCode.observe(this, Observer {
            when(it){
                0->{}
                1->{
                    ToastUtil.showToast(this,"修改密码成功，请重新登录")
                    shp.saveToSp("token", "")
                    shp.saveToSp("uid", "")

                    startActivity(
                        Intent(applicationContext,
                            LoginActivity::class.java)
                    )
                    ActivityCollector.finishAll()
                }
                11->{tokenDialogUtil?.showTokenDialog()}
                else->{ToastUtil.showToast(this,mineViewModel.changeMsg.value)}
            }
        })
    }


    /**
     * 获取验证码
     */
    private fun getCode(){
        binding.getCode.setOnClickListener {
            val bodySendMsg = BodySendMsg(6,shp.getPhone())
//            val bodySendMsg = BodySendMsg(6,"15355090637")
            mineViewModel.countTime.start()
            mineViewModel.sendMsg(bodySendMsg)

        }

        mineViewModel.timeCount.observe(this, Observer {
            if (it > 0 && it <= 60){
                binding.getCode.text = "${it}秒"
                binding.getCode.isClickable = false
                binding.getCode.setBackgroundResource(R.drawable.login_get_code_bg_false)
            }else if ( it == 0) {
                binding.getCode.isClickable = true
                binding.getCode.setBackgroundResource(R.drawable.login_get_code_bg)
                binding.getCode.text = "重新获取验证码"
            }else if ( it == -1) {
                binding.getCode.isClickable = true
                binding.getCode.setBackgroundResource(R.drawable.login_get_code_bg)
                binding.getCode.text = "获取验证码"
            }
        })

        mineViewModel.sendMsgCode.observe(this, Observer {
            when(it){
                0->{}
                1->{ToastUtil.showToast(this,"验证码发送成功")}
                11->{tokenDialogUtil?.showTokenDialog()}
                else->{ToastUtil.showToast(this,mineViewModel.sendMsgMsg.value)}
            }
        })
    }
}