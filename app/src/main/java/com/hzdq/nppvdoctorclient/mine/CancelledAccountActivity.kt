package com.hzdq.nppvdoctorclient.mine

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.ActivityCancelledAccountBinding
import com.hzdq.nppvdoctorclient.login.LoginActivity
import com.hzdq.nppvdoctorclient.mine.dialog.CancellationDialog
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil

class CancelledAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCancelledAccountBinding
    private lateinit var mineViewModel: MineViewModel
    private var tokenDialogUtil: TokenDialogUtil? = null
    private var cancellationDialog: CancellationDialog? = null
    private lateinit var shp: Shp
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cancelled_account)

        shp = Shp(this)
        tokenDialogUtil = TokenDialogUtil(this)


        initView()

        click()

    }

    private fun initView() {
        binding.cancel.setOnClickListener {
            finish()
        }

        binding.back.setOnClickListener {
            finish()
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append("你提交的注销申请生效前，八戒睡眠团队将进行以下验证：\n")
        stringBuilder.append("1、账号处于安全状态；账号没有被盗等风险。\n")
        stringBuilder.append("2、全部设备及用户已解绑；您操作的设备或用户已经全部完成监测。\n")
        stringBuilder.append("3、权限清空及解除；如您的身份为管理员，需提供合作终止证明，其它人员请提供由所属机构管理员出具的注销证明。\n")
        stringBuilder.append("4、其它APP、网站的相关账号解绑；该账号已经解除与其它APP、网站的授权或绑定关系。\n")
        stringBuilder.append("5、不能存在待处理的财务问题；如设备损坏、丢失及其他费用等。")
        binding.content.text = stringBuilder
    }

    private fun click() {
        binding.confirm.setOnClickListener {
//            mineViewModel.getCancellation()

                    if (cancellationDialog == null){
                        cancellationDialog = CancellationDialog(this,R.style.CustomDialog)
                    }
                    cancellationDialog?.show()
                    cancellationDialog?.setCanceledOnTouchOutside(false)
                    cancellationDialog?.setConfirm(object :CancellationDialog.ConfirmAction{
                        override fun onRightClick() {
                            shp.saveToSp("token", "")
                            shp.saveToSp("uid", "")
                            startActivity(
                                Intent(this@CancelledAccountActivity,
                                    LoginActivity::class.java)
                            )
                            ActivityCollector.finishAll()
                        }

                    })

        }
//
//        mineViewModel.cancellationCode.observe(this, Observer {
//            when(it){
//                0 -> {}
//                1 -> {
//                    shp.saveToSp("token", "")
//                    shp.saveToSp("uid", "")
//                    if (cancellationDialog == null){
//                        cancellationDialog = CancellationDialog(this,R.style.CustomDialog)
//                    }
//                    cancellationDialog?.show()
//                    cancellationDialog?.setCanceledOnTouchOutside(false)
//                    cancellationDialog?.setConfirm(object :CancellationDialog.ConfirmAction{
//                        override fun onRightClick() {
//                            startActivity(
//                                Intent(this@CancelledAccountActivity,
//                                    LoginActivity::class.java)
//                            )
//                            ActivityCollector.finishAll()
//                        }
//
//                    })
//                }
//                11 -> {
//                    tokenDialogUtil?.showTokenDialog()
//                }
//                404 -> {
//                    binding.networkTimeout.layout.visibility = View.VISIBLE
//                }
//                else -> {
//                    ToastUtil.showToast(this,mineViewModel.cancellationMsg.value)
//                }
//            }
//        })
//
//        binding.networkTimeout.layout.setOnClickListener {  }
//
//        binding.networkTimeout.button.setOnClickListener {
//            binding.networkTimeout.layout.visibility = View.GONE
//            mineViewModel.getCancellation()
//        }
    }

    override fun onDestroy() {
        ActivityCollector.removeActivity(this)
        tokenDialogUtil?.disMissTokenDialog()
        cancellationDialog?.dismiss()
        super.onDestroy()
    }
}