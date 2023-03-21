package com.hzdq.nppvdoctorclient

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.databinding.ActivityMainBinding
import com.hzdq.nppvdoctorclient.fragment.*
import com.hzdq.nppvdoctorclient.login.LoginActivity
import com.hzdq.nppvdoctorclient.mine.MineViewModel
import com.hzdq.nppvdoctorclient.util.*
import com.hzdq.viewmodelshare.shareViewModels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var serviceFragment: Fragment = ServiceFragment()
    private var patientFragment: Fragment = PatientFragment()
    private var doctorFragment: Fragment = DoctorFragment()
    private var chatFragment: Fragment = ChatFragment()
    private var mineFragment: Fragment = MineFragment()
    private val fragmentList: MutableList<Fragment> = ArrayList()
    private lateinit var shp: Shp
    private lateinit var mineViewModel: MineViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var mainViewModel: MainViewModel
    private val vm: ChatCommonViewModel by shareViewModels("sinory")
    private var destinationMap:Map<Fragment, MotionLayout>? = null
    private var tokenDialogUtil:TokenDialogUtil? = null
    private val TAG = "MainActivity"

    override fun onDestroy() {
        if (!shp.getToken().equals("")){
            vm.unregisterTimeChange()
        }

        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        shp = Shp(this)
        vm.setContext(applicationContext)
        if (shp.getToken().equals("")){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        if (shp.getRoleType() != 2){
            binding.doctorIcon.motion.visibility = View.GONE
        }

        if (savedInstanceState != null){
            serviceFragment = supportFragmentManager.getFragment(savedInstanceState, "ServiceFragment")!!
            patientFragment = supportFragmentManager.getFragment(savedInstanceState, "PatientFragment")!!
            if (shp.getRoleType() == 2){
                doctorFragment = supportFragmentManager.getFragment(savedInstanceState, "DoctorFragment")!!
            }
            chatFragment = supportFragmentManager.getFragment(savedInstanceState, "ChatFragment")!!

            mineFragment = supportFragmentManager.getFragment(savedInstanceState, "MineFragment")!!
            addToList(serviceFragment)
            addToList(patientFragment)

            if (shp.getRoleType() == 2){
                addToList(doctorFragment)

            }
            addToList(chatFragment)
            addToList(mineFragment)
        } else {
            initFragment()
        }

        if (shp.getRoleType() == 2){
            destinationMap = mapOf(
                serviceFragment to binding.serviceIcon.motion,
                patientFragment to binding.patientIcon.motion,
                doctorFragment  to binding.doctorIcon.motion,
                chatFragment to binding.chatIcon.motion,
                mineFragment to binding.mineIcon.motion,
            )
        }else {
            destinationMap = mapOf(
                serviceFragment to binding.serviceIcon.motion,
                patientFragment to binding.patientIcon.motion,
                chatFragment to binding.chatIcon.motion,
                mineFragment to binding.mineIcon.motion,
            )
        }


        initView()

        logOut()
        observer()

        vm.getImAppInfo()
        if (System.currentTimeMillis() - shp.getTokenTimeMillis()!! > 1739000){
            Log.d("TimeChangeReceiver", "ACTION_TIME_TICK:请求一次")
            vm.getImToken()
        }

        vm.registerTimeChange()

    }



    private fun observer(){
        chatViewModel.chatCountTotal.observe(this, Observer {
            Log.d(TAG, "chatCountTotal:$it ")
            if (it>0){
                binding.chatIcon.chatTotalCount.layout.visibility =  View.VISIBLE
                if (it > 99){
                    binding.chatIcon.chatTotalCount.content.text = "99+"
                }else {
                    binding.chatIcon.chatTotalCount.content.text = "$it"
                }
            }else {
                binding.chatIcon.chatTotalCount.layout.visibility =  View.GONE
            }
        })

        mainViewModel.netWorkTimeOut.observe(this, Observer {
            when(it){
                0 -> {binding.networkTimeout.layout.visibility = View.GONE}
                1 ->{binding.networkTimeout.layout.visibility = View.VISIBLE}
            }
        })

        vm.appInfoCode.observe(this, Observer {
            when(it){
                0 -> {}
                1 -> {}
                else -> {
                    ToastUtil.showToast(this,vm.appInfoMsg.value)
                }
            }
        })

        vm.imTokenCode.observe(this, Observer {
            when(it){
                0 -> {}
                1 -> {}
                else -> {
                    ToastUtil.showToast(this,vm.imTokenMsg.value)
                }
            }
        })

        binding.networkTimeout.button.setOnClickListener {
            if (vm.networkTimeout.value != 0){
                when(vm.networkTimeout.value){
                    1 -> {
                        vm.networkTimeout.value = 0
                        vm.getImAppInfo()
                    }
                    2 -> {
                        vm.networkTimeout.value = 0
                        vm.getImToken()
                    }
                }
            }else {
                mainViewModel.netWorkTimeOut.value = 0
                mainViewModel.logOut()
            }

        }

    }

    private fun logOut(){
        binding.backView.setOnClickListener {
            mineViewModel.logOut.value = "close"
        }
        binding.louOut.cancel.setOnClickListener {
            mineViewModel.logOut.value = "close"
        }

        mineViewModel.logOut.observe(this,Observer {
            when(it){
                "show" -> {
                    binding.louOut.motion.visibility = View.VISIBLE
                    binding.backView.visibility = View.VISIBLE
                    BarColor.setBarColor(this,"#1e7964")
                    binding.louOut.motion.transitionToEnd()
                }
                "close" -> {
                    binding.louOut.motion.transitionToStart()
                    lifecycleScope.launch {
                        delay(350)
                        BarColor.setBarColor(this@MainActivity,"#1FBE99")
                        binding.louOut.motion.visibility = View.GONE
                        binding.backView.visibility = View.GONE
                    }
                }

            }
        })


        binding.louOut.confirm.setOnClickListener {
            mainViewModel.logOut()
        }

        mainViewModel.logOutCode.observe(this, Observer {
            when(it){
                0->{}
                1 -> {
                    ToastUtil.showToast(this,"退出登录成功")
                    shp.saveToSp("token","")
                    startActivity(Intent(this,LoginActivity::class.java))
                    ActivityCollector.finishAll()
                }
                else -> {
                    ToastUtil.showToast(this,mainViewModel.logOutMsg.value)
                }
            }
        })


    }

    private fun initView(){
        binding.serviceIcon.motion.setOnClickListener {
            serviceIcon()
            if (serviceFragment == null) {

                serviceFragment = ServiceFragment()
                addFragment(serviceFragment)

            }
            showFragment(serviceFragment)
        }

        binding.patientIcon.motion.setOnClickListener {
            patientIcon()
            if (patientFragment == null) {

                patientFragment = PatientFragment()
                addFragment(patientFragment)

            }
            showFragment(patientFragment)
        }

        binding.doctorIcon.motion.setOnClickListener {
            doctorIcon()
            if (doctorFragment == null) {

                doctorFragment = DoctorFragment()
                addFragment(doctorFragment)

            }
            showFragment(doctorFragment)
        }

        binding.chatIcon.motion.setOnClickListener {
            chatIcon()
            if (chatFragment == null) {

                chatFragment = ChatFragment()
                addFragment(chatFragment)

            }
            showFragment(chatFragment)
        }

        binding.mineIcon.motion.setOnClickListener {
            mineIcon()
            if (mineFragment == null) {

                mineFragment = MineFragment()
                addFragment(mineFragment)

            }
            showFragment(mineFragment)
        }
    }

    private fun initFragment() {
        /* 默认显示home  fragment*/
        serviceIcon()
        serviceFragment = ServiceFragment()
        addFragment(serviceFragment as ServiceFragment)
        showFragment(serviceFragment as ServiceFragment)

        addFragment(patientFragment as PatientFragment)

        if (shp.getRoleType() == 2){
            addFragment(doctorFragment as DoctorFragment)
        }
        addFragment(chatFragment as ChatFragment)
        addFragment(mineFragment as MineFragment)

    }

    private fun serviceIcon(){
        BarColor.setBarColor(this,"#FFFFFF")
        destinationMap?.values?.forEach {
            it.progress = 0f
        }// 循环设置 layout 状态为初始状态
        binding.serviceIcon.motion.transitionToEnd()
    }
    private fun patientIcon(){
        BarColor.setBarColor(this,"#FFFFFF")
        destinationMap?.values?.forEach {
            it.progress = 0f
        }// 循环设置 layout 状态为初始状态
        binding.patientIcon.motion.transitionToEnd()
    }
    private fun doctorIcon(){
        BarColor.setBarColor(this,"#FFFFFF")
        destinationMap?.values?.forEach {
            it.progress = 0f
        }// 循环设置 layout 状态为初始状态
        binding.doctorIcon.motion.transitionToEnd()
    }

    private fun chatIcon(){
        BarColor.setBarColor(this,"#FFFFFF")
        destinationMap?.values?.forEach {
            it.progress = 0f
        }// 循环设置 layout 状态为初始状态
        binding.chatIcon.motion.transitionToEnd()
    }

    private fun mineIcon(){
        BarColor.setBarColor(this,"#1FBE99")
        destinationMap?.values?.forEach {
            it.progress = 0f
        }// 循环设置 layout 状态为初始状态
        binding.mineIcon.motion.transitionToEnd()
    }
    private fun addToList(fragment: Fragment?) {
        if (fragment != null) {
            fragmentList.add(fragment)
        }
    }

    /*显示fragment*/
    private fun showFragment(fragment: Fragment) {
        for (frag in fragmentList) {
            if (frag !== fragment) {
                /*先隐藏其他fragment*/
                supportFragmentManager.beginTransaction().hide(frag).commit()
            }
        }
        supportFragmentManager.beginTransaction().show(fragment).commit()
    }

    /*添加fragment*/
    private fun addFragment(fragment: Fragment) {

        /*判断该fragment是否已经被添加过  如果没有被添加  则添加*/
        if (!fragment.isAdded) {
            supportFragmentManager.beginTransaction().add(R.id.content_layout, fragment).commit()
            /*添加到 fragmentList*/fragmentList.add(fragment)
        }
    }

    /**
     * 安装apk
     */
    private fun installApk(fileSavePath: String) {
        val file = File(fileSavePath)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val data: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判断版本大于等于7.0
            // 通过FileProvider创建一个content类型的Uri
            data =
                FileProvider.getUriForFile(this, "$packageName.fileProvider", file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 给目标应用一个临时授权
        } else {
            data = Uri.fromFile(file)
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive")
        startActivity(intent)
    }

}