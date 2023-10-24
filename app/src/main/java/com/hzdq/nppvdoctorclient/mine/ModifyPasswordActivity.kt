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
    private var password = ""
    private var confirmPassword = ""
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

    override fun onBackPressed() {
        if (shp.getWeakPassword()){

            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }else {
            finish()
        }
    }

    private fun initView(){
        binding.head.content.text = "修改密码"
        binding.head.back.setOnClickListener {
           onBackPressed()
        }
        binding.phone.text = shp.getPhone()


        binding.newPassword.content.text = "新密码"
        binding.repeatPassword.content.text = "重复密码"

        binding.newPassword.edit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                password = binding.newPassword.edit.text.toString()
                if (PasswordRegularUtil.getResult(password)){
                    binding.newPassword.layout.background = getDrawable(R.drawable.modify_password_edit_bg_true)
                }else {
                    binding.newPassword.layout.background = getDrawable(R.drawable.modify_password_edit_bg_false)
                    ToastUtil.showToast(this,"请输入8位以上含3种字符的密码")
                }

            }
        }

        binding.repeatPassword.edit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus){
                confirmPassword = binding.repeatPassword.edit.text.toString()
                if (PasswordRegularUtil.getResult(confirmPassword)){
                    binding.repeatPassword.layout.background = getDrawable(R.drawable.modify_password_edit_bg_true)


                }else {
                    binding.repeatPassword.layout.background = getDrawable(R.drawable.modify_password_edit_bg_false)
                    ToastUtil.showToast(this,"请输入8位以上含3种字符的密码")
                }



            }
        }





        binding.confirm.setOnClickListener {
            binding.newPassword.edit.clearFocus()
            binding.repeatPassword.edit.clearFocus()
            mineViewModel.newPassword.value = AESEncrypt.encryptAES(binding.newPassword.edit.text.toString(),"respirator_10131")
            mineViewModel.repeatPassword.value = AESEncrypt.encryptAES(binding.repeatPassword.edit.text.toString(),"respirator_10131")

            if (binding.code.text.toString().equals("")){
                ToastUtil.showToast(this,"请输入验证码")
                return@setOnClickListener
            }

            if (!mineViewModel.newPassword.value.equals(mineViewModel.repeatPassword.value)){
                ToastUtil.showToast(this,"两次密码不一致")
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