package com.hzdq.nppvdoctorclient.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.hzdq.nppvdoctorclient.MainActivity
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodyLoginPassword
import com.hzdq.nppvdoctorclient.body.BodyLoginVerificationCode
import com.hzdq.nppvdoctorclient.databinding.ActivityLoginBinding
import com.hzdq.nppvdoctorclient.login.dialog.PrivateDialog
import com.hzdq.nppvdoctorclient.mine.PrivacyAgreementActivity
import com.hzdq.nppvdoctorclient.util.*
import com.hzdq.nppvdoctorclient.util.ViewClickDelay.clickDelay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding:ActivityLoginBinding
    private lateinit var navController: NavController
    private var exitTime: Long = 0
    private var privateDialog : PrivateDialog? = null
    private lateinit var shp:Shp
    private var bodyLoginPassword:BodyLoginPassword? = null
    private var bodyLoginVerificationCode:BodyLoginVerificationCode? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        bodyLoginPassword = BodyLoginPassword(loginViewModel.user.value,loginViewModel.password.value)
        bodyLoginVerificationCode = BodyLoginVerificationCode(loginViewModel.phone.value,loginViewModel.verificationCode.value)
        initView()
        initPrivateDialog()
        switchFragment()
        observer()
        click()

    }

    private fun initView(){
        shp = Shp(this)
        navController = findNavController(R.id.login_fragment)
        BarColor.setBarColor(this,"#1FC49E")
        binding.version.text = "V${loginViewModel.getVerName(this)}"

    }

    private fun initPrivateDialog(){
        if (shp.getFirstLogin()){
            if (null == privateDialog){
                privateDialog = PrivateDialog(this,R.style.CustomDialog)
                privateDialog!!.setConfirm("同意",object :PrivateDialog.IOnConfirmListener{
                    override fun onConfirm(dialog: PrivateDialog?) {

                    }

                })
                privateDialog?.setCancel("不同意并退出App",object :PrivateDialog.IOnCancelListener{
                    override fun onCancel(dialog: PrivateDialog?) {
                        shp.saveToSpBoolean("firstLogin",true)
                        val intent = Intent(Intent.ACTION_MAIN)

                        intent.addCategory(Intent.CATEGORY_HOME)

                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

                        startActivity(intent)
//
//
                        lifecycleScope.launch {
                            delay(500)
                            System.exit(0)
                        }
                    }

                })

                privateDialog?.setUser {
                    val intent = Intent(this, PrivacyAgreementActivity::class.java)
                    intent.putExtra("type","user")
                    startActivity(intent)
                }

                privateDialog?.setPrivate {
                    val intent = Intent(this, PrivacyAgreementActivity::class.java)
                    intent.putExtra("type","privacy")
                    startActivity(intent)
                }
                privateDialog?.show()
                privateDialog?.setCanceledOnTouchOutside(false)
            }
        }



    }

    /**
     * 登录
     */
    private fun login(){
        if (!binding.checkBox.isChecked){
            ToastUtil.showToast(this,"请勾选已阅读并同意《隐私声明》与《用户协议》")
            return
        }

        if (isVerificationCodeFragment()){
            if (loginViewModel.phone.value.equals("")){
                ToastUtil.showToast(this,"手机号不能为空")
                return
            }
            if (!PhoneFormatCheckUtils.isPhoneLegal(loginViewModel.phone.value)){
                ToastUtil.showToast(this,"请输入正确的手机号码")
                return
            }
            if (loginViewModel.verificationCode.value.equals("")){
                ToastUtil.showToast(this,"请输入验证码")
                return
            }
        }else {
            if (loginViewModel.user.value.equals("")){
                ToastUtil.showToast(this,"账号不能为空")
                return
            }

            if (loginViewModel.password.value.equals("")){
                ToastUtil.showToast(this,"请输入密码")
                return
            }
        }
        if (isPassWordFragment()){
            //当前处于密码登录

            bodyLoginPassword?.mobile  = loginViewModel.user.value
            bodyLoginPassword?.passWord  = AESEncrypt.encryptAES(loginViewModel.password.value,"respirator_10131")
            loginViewModel.loginPassword(bodyLoginPassword!!)
        }else {
            //当前处于验证码登录
            bodyLoginVerificationCode?.phone  = loginViewModel.phone.value
            bodyLoginVerificationCode?.verificationCode  = loginViewModel.verificationCode.value
            loginViewModel.loginVerificationCode(bodyLoginVerificationCode!!)
        }

    }

    private fun observer(){
        loginViewModel.loginCode.observe(this, Observer {
            when(it){
                0 -> {}
                1 -> {

                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                }
                else -> {
                    ToastUtil.showToast(this,loginViewModel.loginMsg.value)
                }
            }
        })

//        loginViewModel.netWorkTimeOut.observe(this, Observer {
//            when(it){
//                0 -> {binding.networkTimeout.layout.visibility = View.GONE}
//                1 ->{binding.networkTimeout.layout.visibility = View.VISIBLE}
//                2 ->{binding.networkTimeout.layout.visibility = View.VISIBLE}
//            }
//        })


        loginViewModel.sendMsgCode.observe(this, Observer {
            when(it){
                0->{}
                1->{ToastUtil.showToast(this,"验证码发送成功")}
                else->{ToastUtil.showToast(this,loginViewModel.sendMsgMsg.value)}
            }
        })

    }

    /**
     * 当前为密码登录
     */
    private fun isPassWordFragment():Boolean{
        return navController.currentDestination?.id == R.id.passWordFragment
    }

    /**
     * 当前为验证码登录
     */
    private fun isVerificationCodeFragment():Boolean{
        return navController.currentDestination?.id == R.id.verificationCodeFragment
    }
    /**
     * 切换验证码或密码登录
     */
    private fun switchFragment(){
        binding.passwordOrCode.setOnClickListener {
            if (binding.passwordOrCode.text.equals("使用密码登录")){
                binding.passwordOrCode.text = "使用验证码登录"
                navController.navigate(R.id.action_verificationCodeFragment_to_passWordFragment)
            }else {
                binding.passwordOrCode.text = "使用密码登录"
                navController.navigate(R.id.action_passWordFragment_to_verificationCodeFragment)
            }
        }
    }

    private fun click(){
        binding.userAgreement.clickDelay {
            val intent = Intent(this, PrivacyAgreementActivity::class.java)
            intent.putExtra("type","user")
            startActivity(intent)
        }

        binding.privacyStatement.clickDelay {
            val intent = Intent(this, PrivacyAgreementActivity::class.java)
            intent.putExtra("type","privacy")
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            login()
        }

//        binding.networkTimeout.button.setOnClickListener {
//            when(loginViewModel.netWorkTimeOut.value){
//                1 -> {
//                    loginViewModel.netWorkTimeOut.value = 0
//                    loginViewModel.loginPassword(bodyLoginPassword!!)
//                }
//                2 -> {
//                    loginViewModel.netWorkTimeOut.value = 0
//                    loginViewModel.loginVerificationCode(bodyLoginVerificationCode!!)
//                }
//            }
//        }
    }


    override fun onBackPressed() {
        exit()
    }
    /**
     * 按两次退出程序
     */
    private fun exit() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtil.showToast(this,"再按一次退出程序")
            exitTime = System.currentTimeMillis()
        } else {
            finish()
            System.exit(0)
        }
    }

    override fun onDestroy() {
        privateDialog?.dismiss()
        super.onDestroy()
    }
}