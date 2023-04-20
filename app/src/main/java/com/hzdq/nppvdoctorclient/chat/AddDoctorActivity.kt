package com.hzdq.nppvdoctorclient.chat

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.adapter.PersonAdapter
import com.hzdq.nppvdoctorclient.bean.Person
import com.hzdq.nppvdoctorclient.body.BodyDoctorList
import com.hzdq.nppvdoctorclient.body.BodyGroupInvitation
import com.hzdq.nppvdoctorclient.chat.adapter.DoctorListAdapter
import com.hzdq.nppvdoctorclient.databinding.ActivityAddDoctorBinding
import com.hzdq.nppvdoctorclient.util.*
import com.hzdq.nppvdoctorclient.view.SideBar
import kotlinx.coroutines.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
*@desc 添加医生/医助
*@Author Sinory
*@date 2023/3/16 12:46
*/
class AddDoctorActivity : AppCompatActivity() {

    /**
     * 联系人集合
     */
    private var persons: ArrayList<Person>? = null
    private lateinit var binding:ActivityAddDoctorBinding
    private val handler: Handler = Handler() //用于隐藏切换后的字母,在主线程中运行
    private var personAdapter :PersonAdapter? = null
    private var linearLayoutManager:LinearLayoutManager? = null
    private var doctorListAdapter:DoctorListAdapter? = null
    private var pinYinUtils: PinYinUtils? = null
    private var bodyDoctorList:BodyDoctorList? = null
    private lateinit var chatViewModel: ChatViewModel
    var pageNum = 1
    private var tokenDialogUtil:TokenDialogUtil? = null
    private var pinyinComparator:PinyinComparator? = null

    //任务是否完成
    private var isDone = true
    //任务完成量
    private var taskCount = 0
    private var startCount = 0

    private suspend fun doTask(task: BodyGroupInvitation) {
        // 执行单个任务的代码
        isDone = false
        startCount += 1
        Log.d("AddDoctor", "doTask:开始执行第${startCount}次任务 ")
        chatViewModel.groupInvitation(task)

    }


    private fun checkCondition(): Boolean {
        // 判断是否符合条件的代码
        return isDone
    }
    private fun startTaskQueue() {
        // 启动协程执行任务队列
        scope.launch {
            // 遍历任务队列并执行任务
            for (task in taskQueue) {
                task()
                //延迟1秒
                delay(500)
                // 如果符合某个条件，继续执行下一个任务
                if (checkCondition()) {
                    continue
                }
                // 如果不符合条件，中断执行任务队列

                break
            }
            //如果任务完成量等于选取的人员数量则是全部完成
            //否则是部分完成
            Log.d("AddDoctor", "taskCount:$taskCount ")
            Log.d("AddDoctor", "groupInvitationListSize:${chatViewModel.groupInvitationListSize.value} ")

//            if (taskCount == chatViewModel.groupInvitationListSize.value){
//                setResult(RESULT_OK)
//                finish()
//            }else if (taskCount == 0){
//                CoroutineScope(Dispatchers.Main).launch {
//                    ToastUtil.showToast(this@AddDoctorActivity,"未完成任务")
//                }
//            }else {
//                setResult(20)
//                finish()
//            }


//            setResult(RESULT_OK)
//            finish()
        }
    }
    // 在 Activity 的成员变量中定义协程作用域和任务队列
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private val taskQueue = mutableListOf<suspend () -> Unit>()



    override fun onDestroy() {
        scope.cancel()
        ActivityCollector.removeActivity(this)
        tokenDialogUtil?.disMissTokenDialog()
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_doctor)
        tokenDialogUtil = TokenDialogUtil(this)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        pinYinUtils = PinYinUtils()


        binding.confirm.setOnClickListener {
            Log.d("AddDoctor", "拿到列表:${chatViewModel.groupInvitationList.value!!} ")
            binding.confirm.isEnabled = false
            binding.progressbar.visibility = View.VISIBLE
//            chatViewModel.groupInvitationList.value!!.forEach { task ->
//                taskQueue.add {
//                    // 执行单个任务
//                    doTask(task)
//                }
//            }
            // 开始执行任务队列
//            startTaskQueue()
            chatViewModel.groupInvitationPosition.value = 0
        }

        chatViewModel.groupInvitationListSize.observe(this, Observer {
            if (it == 0){
                binding.confirm.isEnabled = false
                binding.confirm.text = "完成"
                binding.confirm.setTextColor(Color.parseColor("#ffb7b7b7"))
            }else {
                binding.confirm.isEnabled = true
                binding.confirm.text = "完成(${it})"
                binding.confirm.setTextColor(Color.parseColor("#ffffff"))
            }
        })


        val bundle = intent.getBundleExtra("bundle")
        chatViewModel.uidList.value  = bundle?.getIntegerArrayList("uidList")
        chatViewModel.groupId.value = bundle?.getInt("groupId",0)


        bodyDoctorList = BodyDoctorList(null,"",1,1000,null)
        chatViewModel.getDoctorList(bodyDoctorList!!)
        pinyinComparator = PinyinComparator()
        initView()

        binding.sideBar.setOnIndexChangeListener(MyOnIndexChangeListener())

        linearLayoutManager = LinearLayoutManager(this)

        doctorListAdapter = DoctorListAdapter(chatViewModel).apply {
            preloadItemCount = 9
            onPreload = {
                // 预加载业务逻辑
                Log.d("TAG", "预加载: ")
                pageNum += 1
                bodyDoctorList = BodyDoctorList(null,chatViewModel.searchName.value,pageNum,1000,null)
                chatViewModel.getDoctorList(bodyDoctorList!!)
            }
        }
//        personAdapter = PersonAdapter()
        binding.recyclerView.apply {
            adapter = doctorListAdapter
            layoutManager = linearLayoutManager
        }

        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


        binding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().equals("")){
                    chatViewModel.searchName.value = ""
                    chatViewModel.doctorList.value!!.clear()
                    bodyDoctorList = BodyDoctorList(null,chatViewModel.searchName.value,1,1000,null)
                    chatViewModel.getDoctorList(bodyDoctorList!!)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        //点击键盘搜索按钮后
        binding.search.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (!binding.search.text.toString().equals("")){
                    chatViewModel.searchName.value = binding.search.text.toString()
                    //Perform Code
                    HideKeyboard.hideKeyboard(v,this)
                    chatViewModel.doctorList.value!!.clear()
                    bodyDoctorList = BodyDoctorList(null,chatViewModel.searchName.value,1,1000,null)
                    chatViewModel.getDoctorList(bodyDoctorList!!)
                }


            }
            false
        }

        chatViewModel.doctorListCode.observe(this, Observer {
            when(it){
                0 -> {
                    binding.progressbar.visibility = View.VISIBLE
                }
                1 -> {
                    doctorListAdapter?.isPreloading = false
                    for (i in 0 until chatViewModel.doctorList.value!!.size){

                        chatViewModel.doctorList.value!![i].pinyin = pinYinUtils!!.getPinYin(chatViewModel.doctorList.value!![i].name)
                        chatViewModel.doctorList.value!![i].position = i
                    }
                    //排序 按照A-Z排序
//                    chatViewModel.doctorList.value!!.sortWith(Comparator { o1, o2 ->
//                        o1!!.pinyin!!.compareTo(o2!!.pinyin!!) //根据拼音排序
//
//                    })
                    // 根据a-z进行排序
                    Collections.sort(chatViewModel.doctorList.value!!, pinyinComparator)
//                    chatViewModel.doctorList.value!!.sortedWith(compareBy({ it.pinyin!!.startsWith("#") }, { it.pinyin }))
//                    Log.d("asdasdasd", "list3:${chatViewModel.doctorList.value} ")
                    doctorListAdapter?.notifyDataSetChanged()
                    doctorListAdapter?.submitList(chatViewModel.doctorList.value)
                    binding.progressbar.visibility = View.GONE
                    bodyDoctorList = null
                }
                11 -> {
                    binding.progressbar.visibility = View.GONE
                    tokenDialogUtil?.showTokenDialog()
                    bodyDoctorList = null
                }
                else -> {
                    binding.progressbar.visibility = View.GONE
                    ToastUtil.showToast(this,chatViewModel.doctorListMsg.value)
                    bodyDoctorList = null
                }
            }
        })

        chatViewModel.groupInvitationPosition.observe(this, Observer {

            if (it >= 0 && it<= chatViewModel.groupInvitationListSize.value!!-1){
                chatViewModel.groupInvitation(chatViewModel.groupInvitationList.value!![it])
            }else if (it == chatViewModel.groupInvitationListSize.value!!){
                if (chatViewModel.isDoneCount.value!! == chatViewModel.groupInvitationListSize.value!!){
                    Log.d("AddDoctor", "任务全部完成了")
                    setResult(RESULT_OK)
                    finish()
                }else {
                    if (chatViewModel.isDoneCount.value!! > 0){
                        setResult(20)
                        finish()
                        Log.d("AddDoctor", "没有全部完成")
                    }else {
                        binding.progressbar.visibility = View.GONE
                    }

                }

            }

        })

        doctorListAdapter?.setOnItemClickListener(object :DoctorListAdapter.OnItemClickListener{
            override fun onItemClick(isCheck:Boolean) {
                if (isCheck){
                    chatViewModel.searchName.value = binding.search.text.toString()
                    //Perform Code
                    HideKeyboard.hideKeyboard(window.decorView,this@AddDoctorActivity)
                    chatViewModel.doctorList.value!!.clear()
                    bodyDoctorList = BodyDoctorList(null,chatViewModel.searchName.value,1,1000,null)
                    chatViewModel.getDoctorList(bodyDoctorList!!)
                }

            }

        })



//        initData()
    }



    private fun initView(){
        binding.head.content.text = "新增人员"
        binding.head.back.setOnClickListener {
            finish()
        }


        chatViewModel.groupInvitationId.observe(this, Observer {
            when(it){
                0 ->{}
                1 ->{
                    //任务完成后任务完成量+1
                    taskCount += 1
                    chatViewModel.isDoneCount.value =  chatViewModel.isDoneCount.value!!+1
                    Log.d("AddDoctor", "任务完成量${chatViewModel.isDoneCount.value} ")
                    chatViewModel.groupInvitationPosition.value = chatViewModel.groupInvitationPosition.value!!+1
                    chatViewModel.groupInvitationId.value = 0
                    isDone = true
                }
                11 ->{
                    tokenDialogUtil?.showTokenDialog()
                    scope.cancel()
                }
                200 ->{
                    chatViewModel.groupInvitationPosition.value = chatViewModel.groupInvitationPosition.value!!+1
                    ToastUtil.showToast(this,chatViewModel.groupInvitationMsg.value)
                    chatViewModel.groupInvitationId.value = 0
                    isDone = true
                }
                else->{
                    chatViewModel.groupInvitationPosition.value = chatViewModel.groupInvitationPosition.value!!+1
                    ToastUtil.showToast(this,chatViewModel.groupInvitationMsg.value)
                    chatViewModel.groupInvitationId.value = 0
                    isDone = true
                }
            }
        })

    }

    inner class MyOnIndexChangeListener :SideBar.OnIndexChangeListener{
        override fun onIndexChange(word: String) {
            updateWord(word)
            updateRecyclerView(word);//A～Z字母

        }

    }

    /**
     * 初始化数据
     */
    private fun initData() {
        persons = ArrayList()
        persons!!.add(Person("张小光","",0)) //将人名添加到集合中
        persons!!.add(Person("杨大雷","",0))
        persons!!.add(Person("胡继开","",0))
        persons!!.add(Person("刘三","",0))
        persons!!.add(Person("钟兴","",0))
        persons!!.add(Person("尹顺","",0))
        persons!!.add(Person("安杰","",0))
        persons!!.add(Person("张骞","",0))
        persons!!.add(Person("温小松","",0))
        persons!!.add(Person("李凤","",0))
        persons!!.add(Person("杜甫","",0))
        persons!!.add(Person("娄志超","",0))
        persons!!.add(Person("张飞","",0))
        persons!!.add(Person("王杰","",0))
        persons!!.add(Person("李三","",0))
        persons!!.add(Person("孙二娘","",0))
        persons!!.add(Person("唐小雷","",0))
        persons!!.add(Person("牛二","",0))
        persons!!.add(Person("姜光刃","",0))
        persons!!.add(Person("刘能","",0))
        persons!!.add(Person("张四","",0))
        persons!!.add(Person("张五","",0))
        persons!!.add(Person("侯大帅","",0))
        persons!!.add(Person("刘洪","",0))
        persons!!.add(Person("乔三","",0))
        persons!!.add(Person("徐达健","",0))
        persons!!.add(Person("吴洪亮","",0))
        persons!!.add(Person("王兆雷","",0))
        persons!!.add(Person("阿四","",0))
        persons!!.add(Person("李洪磊","",0))

        for (i in 0 until persons!!.size){
            persons!![i].pinyin = pinYinUtils!!.getPinYin(persons!![i].name)
            persons!![i].position = i
        }

        //排序 按照A-Z排序
        persons!!.sortWith(Comparator { o1, o2 ->
            o1!!.pinyin!!.compareTo(o2!!.pinyin!!) //根据拼音排序
        })
        personAdapter?.submitList(persons)




    }

    private fun updateRecyclerView(word:String){

        for (i in 0 until chatViewModel.doctorList.value!!.size) {
            val listWord = chatViewModel.doctorList.value!![i].pinyin!!.substring(0, 1) //YANGGUANGFU-(转成)->Y
            if (word.equals(listWord)) {
                //i是ListView中的位置
//                binding.recyclerView.scrollToPosition(i) //定位到recyclerView中的某个位置
                linearLayoutManager?.scrollToPositionWithOffset(i,0)
                return
            }
        }
    }

    /**
     * 显示英文字母
     */
    private fun updateWord(word:String){
        AnimationUtil.fadeIn(binding.word)
        binding.word.text = word
        handler.removeCallbacksAndMessages(null); //先把每次的消息移除
        handler.postDelayed({
            //因为handler在主线程中运行，Runnable方法也是运行在主线程
            //（打日志System.out.println是判断Runnable在哪个线程中运行，出现main证明就是在主线程中运行）
            //                System.out.println(Thread.currentThread().getName() +"-------------------");
//            binding.word.visibility = View.GONE; //1秒后隐藏
               AnimationUtil.fadeOut(binding.word)
        },1000) //1秒后隐藏
    }
}