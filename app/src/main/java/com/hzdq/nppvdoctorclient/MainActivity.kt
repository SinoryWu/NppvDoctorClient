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
import androidx.work.*
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCustomMessageBody
import com.hzdq.nppvdoctorclient.body.BodyVersion
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.databinding.ActivityMainBinding
import com.hzdq.nppvdoctorclient.dataclass.DataClassReceiver
import com.hzdq.nppvdoctorclient.dataclass.FromUser
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.fragment.*
import com.hzdq.nppvdoctorclient.login.LoginActivity
import com.hzdq.nppvdoctorclient.mine.MineViewModel
import com.hzdq.nppvdoctorclient.mine.dialog.UpdateDialog
import com.hzdq.nppvdoctorclient.mine.dialog.VersionUpdateDialog
import com.hzdq.nppvdoctorclient.util.*
import com.hzdq.viewmodelshare.shareViewModels
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


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
    private var updateDialog: UpdateDialog? = null
    private var versionUpdateDialog: VersionUpdateDialog? = null

    override fun onStart() {

        Log.d(TAG, "onStart: ")
        if (shp.getFirstLoginIm()==false){
            vm.registerListener()
//            EMClient.getInstance().chatManager().addMessageListener(msgListener);

        }
        super.onStart()
    }


    override fun onDestroy() {
        if (!shp.getToken().equals("")){
            vm.unregisterTimeChange()
        }
        if (shp.getFirstLoginIm()==false){
            vm.unregisterListener()
//            EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        }
        tokenDialogUtil?.disMissTokenDialog()
        updateDialog?.dismiss()
        versionUpdateDialog?.dismiss()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        ActivityCollector.addActivity(this)
        tokenDialogUtil = TokenDialogUtil(this)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        shp = Shp(this)
        vm.setContext(applicationContext)


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // 网络状态
            .setRequiresBatteryNotLow(true)                 // 不在电量不足时执行
            .setRequiresCharging(true)                      // 在充电时执行
            .setRequiresStorageNotLow(true)                 // 不在存储容量不足时执行
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraints.setRequiresDeviceIdle(true) // 在待机状态下执行，需要 API 23
        }else{ }


        val workRequest =  PeriodicWorkRequest.Builder(MyWorker::class.java,1, TimeUnit.SECONDS)
            .setConstraints(constraints.build())//
            .build();
        WorkManager.getInstance().enqueue(workRequest);//这串代码是加入任务队列的意思

        if (shp.getFirstLoginIm() == false){
            vm.initIm()
            vm.loginIm()



        }


        vm.registerActivityLifecycleCallbacks(application)
        vm.registerTimeChange()
        val intent =intent

        if (shp.getToken().equals("")){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        if (shp.getRoleType() != 3){
            binding.doctorIcon.motion.visibility = View.GONE
        }



        val bodyVersion = BodyVersion(5, 2, 2)
        mineViewModel.postVersion(bodyVersion)

        mineViewModel.versionCode.observe(this, Observer {
            when (it) {
                0 -> {
                }
                1 -> {
                    if (!mineViewModel.version.value!!.equals("")) {
                        if (!mineViewModel.version.value!!.equals(
                                mineViewModel.getVerName(this)
                            )
                        ) {
                            if (updateDialog == null) {
                                updateDialog = UpdateDialog(
                                    mineViewModel.version.value!!,
                                    this,
                                    R.style.CustomDialog
                                )
                                updateDialog?.show()
                                updateDialog?.setCanceledOnTouchOutside(false)
                                updateDialog?.setConfirm(object : UpdateDialog.ConfirmAction {
                                    override fun onRightClick() {


                                        updateDialog?.dismiss()
                                        updateDialog = null


                                        if (!mineViewModel.version.value.equals(
                                                mineViewModel.getVerName(
                                                    this@MainActivity
                                                )
                                            )
                                        ) {
                                            DownLoadVersionAppFile(
                                                mineViewModel.downLoadAddress.value!!,
                                                "${cacheDir}/NPPV管理端.apk"
                                            )

                                        }
                                    }

                                })
                            }
                        }
                    }


                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                404 -> {
                    binding.networkTimeout.layout.visibility = View.VISIBLE
                    ToastUtil.showToast(this, mineViewModel.versionMsg.value)
                    mineViewModel.versionCode.value = 0
                }
            }
        })

        if (savedInstanceState != null){
            serviceFragment = supportFragmentManager.getFragment(savedInstanceState, "ServiceFragment")!!
            patientFragment = supportFragmentManager.getFragment(savedInstanceState, "PatientFragment")!!
            if (shp.getRoleType() == 3){
                doctorFragment = supportFragmentManager.getFragment(savedInstanceState, "DoctorFragment")!!
            }
            chatFragment = supportFragmentManager.getFragment(savedInstanceState, "ChatFragment")!!

            mineFragment = supportFragmentManager.getFragment(savedInstanceState, "MineFragment")!!
            addToList(serviceFragment)
            addToList(patientFragment)

            if (shp.getRoleType() == 3){
                addToList(doctorFragment)

            }
            addToList(chatFragment)
            addToList(mineFragment)
        } else {
            initFragment()
            if (intent.getIntExtra("notification",0) == 1){
                binding.chatIcon.motion.performClick()
            }

        }

        if (shp.getRoleType() == 3){
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

        mainViewModel.getUserInfo()

        initView()

        logOut()
        observer()

        vm.getImAppInfo()
        vm.getImToken()








    }



    private fun observer(){

        chatViewModel.chatCountTotal.observe(this, Observer {
            if (chatViewModel.isChat.value == false){
                //判断是否在聊天界面
                if (it > 0){
                    binding.chatIcon.chatTotalCount.visibility =  View.VISIBLE
//                if (it > 99){
//                    binding.chatIcon.chatTotalCount.content.text = "99+"
//                }else {
//                    binding.chatIcon.chatTotalCount.content.text = "$it"
//                }
                }else {
                    binding.chatIcon.chatTotalCount.visibility =  View.GONE
                }
            }else {
                binding.chatIcon.chatTotalCount.visibility =  View.GONE
            }

        })

        mainViewModel.netWorkTimeOut.observe(this, Observer {
            when(it){
                0 -> {binding.networkTimeout.layout.visibility = View.GONE}
                1 ->{binding.networkTimeout.layout.visibility = View.VISIBLE}
                2 -> {binding.networkTimeout.layout.visibility = View.VISIBLE}
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
            }else  {
                if (mainViewModel.netWorkTimeOut.value == 1){
                    mainViewModel.netWorkTimeOut.value = 0
                    mainViewModel.logOut()
                }else if (mainViewModel.netWorkTimeOut.value == 2){
                    mainViewModel.netWorkTimeOut.value = 0
                    mainViewModel.getUserInfo()
                }

            }

        }

        mainViewModel.userInfoCode.observe(this, Observer {
            when(it){
                0 ->{}
                1 ->{}
                11->{
                    tokenDialogUtil?.showTokenDialog()
                }
                else->{
                    ToastUtil.showToast(this,mainViewModel.userInfoMsg.value)
                }
            }
        })



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
                11->{
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this,mainViewModel.logOutMsg.value)
                }
            }
        })



    }

    private fun initView(){
        binding.serviceIcon.motion.setOnClickListener {
            chatViewModel.isChat.value = false
            serviceIcon()
            if (serviceFragment == null) {

                serviceFragment = ServiceFragment()
                addFragment(serviceFragment)

            }
            showFragment(serviceFragment)
        }

        binding.patientIcon.motion.setOnClickListener {
            chatViewModel.isChat.value = false
            patientIcon()
            if (patientFragment == null) {

                patientFragment = PatientFragment()
                addFragment(patientFragment)

            }
            showFragment(patientFragment)
        }

        binding.doctorIcon.motion.setOnClickListener {
            chatViewModel.isChat.value = false
            doctorIcon()
            if (doctorFragment == null) {

                doctorFragment = DoctorFragment()
                addFragment(doctorFragment)

            }
            showFragment(doctorFragment)
        }

        // 获取华为 HMS 推送 token
        HMSPushHelper.getInstance().getHMSToken(this)
        binding.chatIcon.motion.setOnClickListener {
            chatViewModel.isChat.value = true
            binding.chatIcon.chatTotalCount.visibility =  View.GONE
            chatIcon()
            //登录环信
            if (shp.getFirstLoginIm()==true){
                if (!shp.getAppKey().equals("")){
                    vm.initIm()

                    vm.loginIm()
                    vm.registerListener()
                }

            }

            if (chatFragment == null) {

                chatFragment = ChatFragment()
                addFragment(chatFragment)

            }
            showFragment(chatFragment)
        }

        binding.mineIcon.motion.setOnClickListener {
            chatViewModel.isChat.value = false
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

        if (shp.getRoleType() == 3){
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


    override fun onSaveInstanceState(outState: Bundle) {

        /*fragment不为空时 保存*/
        if (serviceFragment != null) {
            supportFragmentManager.putFragment(outState!!, "ServiceFragment", serviceFragment)
        }
        if (patientFragment != null) {
            supportFragmentManager.putFragment(outState!!, "PatientFragment", patientFragment)
        }
        if (shp.getRoleType() == 3){
            if (doctorFragment != null) {
                supportFragmentManager.putFragment(outState!!, "DoctorFragment", doctorFragment)
            }
        }
        if (chatFragment != null) {
            supportFragmentManager.putFragment(outState!!, "ChatFragment", chatFragment)
        }
        if (mineFragment != null) {
            supportFragmentManager.putFragment(outState!!, "MineFragment", mineFragment)
        }
        super.onSaveInstanceState(outState)
    }
    private var exitTime: Long = 0
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


    /**
     * 下载更新包到本地
     * @param url
     * @param path
     */
    private fun DownLoadVersionAppFile(url: String, path: String) {
        versionUpdateDialog = VersionUpdateDialog(this, mineViewModel, this)
        versionUpdateDialog?.show()
        versionUpdateDialog?.setCanceledOnTouchOutside(false)
        FileDownloader.getImpl().create(url).setPath(path)
            .setListener(object : FileDownloadListener() {
                override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {

                }

                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {

                    mineViewModel.updateProgress.value =
                        ((soFarBytes.toDouble() / totalBytes.toDouble()).toDouble() * 100).toInt()

                }

                override fun completed(task: BaseDownloadTask) {
                    mineViewModel.updateProgress.value = 100

                    if (fileIsExists(path)) {
                        installApk(path)
                        ActivityCollector.finishAll()
                    }
                }

                override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {

                }

                override fun error(task: BaseDownloadTask, e: Throwable) {
                    mineViewModel.updateProgress.value = 100
                    ToastUtil.showToast(this@MainActivity, "更新包下载失败")
                }

                override fun warn(task: BaseDownloadTask) {

                }
            }).start()
    }

    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
    private fun fileIsExists(filePath: String): Boolean {
        try {
            val f = File(filePath)
            if (!f.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }



}