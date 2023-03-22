package com.hzdq.nppvdoctorclient.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodyImMessageList
import com.hzdq.nppvdoctorclient.body.BodyReadAllMsg
import com.hzdq.nppvdoctorclient.body.BodySendMessage
import com.hzdq.nppvdoctorclient.chat.adapter.MessageListAdapter2
import com.hzdq.nppvdoctorclient.databinding.ActivityChatBinding
import com.hzdq.nppvdoctorclient.dataclass.FromUser
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.util.*


/**
*@desc 聊天界面
*@Author Sinory
*@date 2023/3/16 12:46
*/
class ChatActivity : AppCompatActivity() {
    private var tokenDialogUtil: TokenDialogUtil? = null
    private lateinit var binding:ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private val TAG = "ChatActivity"
    private lateinit var shp: Shp
    private var messageListAdapter2: MessageListAdapter2? = null
    private var linearLayoutManager:LinearLayoutManager? = null
    private var bodySendMessage:BodySendMessage? = null
    private var imMessageList:ImMessageList? = null
    private var fromUser:FromUser? = null
    private var bodyImMessageList:BodyImMessageList? = null
    private var bodyReadAllMsg:BodyReadAllMsg? = null
    var editHeight = 0
    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        chatViewModel.groupId.value = intent.getIntExtra("groupId",0)
        shp = Shp(this)
        ActivityCollector.addActivity(this)
        tokenDialogUtil = TokenDialogUtil(this)
        bodySendMessage = BodySendMessage(2,"",0,chatViewModel.groupId.value)
        fromUser = FromUser(0,0,"","",shp.getRoleType())
        imMessageList = ImMessageList(0,2,fromUser,"","",0,"",true)


        initView()

        binding.sendMsg.setOnClickListener {
            if (binding.edit.text.toString().equals("")){
                ToastUtil.showToast(this,"聊天内容不能为空")
            }else {
                bodySendMessage?.messageType = 1
                bodySendMessage?.message = binding.edit.text.toString()

                imMessageList?.gmtCreate = DateUtil.stamp2Date(System.currentTimeMillis())

                imMessageList?.messageType = 1
                imMessageList?.message = binding.edit.text.toString()

                chatViewModel.sendMessage(bodySendMessage!!)
            }
        }



        observer()

    }


    private fun observer(){
        chatViewModel.sendMessageCode.observe(this, Observer {
            when(it){
                0->{}
                1->{
                    binding.recyclerView.setItemAnimator(DefaultItemAnimator())
                    chatViewModel.messageList.value?.add(0,imMessageList!!)
                    messageListAdapter2!!.notifyItemInserted(0)
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
                    val intent = Intent()
                    intent.putExtra("lastMsgTime",DateUtil.stamp2Date(System.currentTimeMillis()))
                    intent.putExtra("lastMessage",binding.edit.text.toString())
                    intent.putExtra("lastMessageType",1)
                    setResult(RESULT_OK,intent)
                    binding.edit.setText("")
                }
                11->{
                    tokenDialogUtil?.showTokenDialog()
                }
                else->{
                    ToastUtil.showToast(this,chatViewModel.sendMessageMsg.value)
                }
            }
        })

        chatViewModel.readMsgCode.observe(this, Observer {
            when(it){
                0 -> {}
                1 -> {
                    setResult(RESULT_OK)
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this,chatViewModel.readMsgMsg.value)
                }
            }
        })


    }

    override fun onBackPressed() {
        HideKeyboard.hideKeyboard(binding.root,this)
        finish()
    }

    private fun initView(){
        binding.head.content.text = intent.getStringExtra("groupName")

        binding.head.back.setOnClickListener {
            onBackPressed()
        }
        bodyReadAllMsg = BodyReadAllMsg(chatViewModel.groupId.value)
        bodyImMessageList = BodyImMessageList(0,0,0,null)
        bodyImMessageList?.groupId = chatViewModel.groupId.value
//        bodyImMessageList?.groupId = chatViewModel.groupId.value
        bodyImMessageList?.index = null
        bodyImMessageList?.pageNum = 1
        bodyImMessageList?.pageSize = 20
        chatViewModel.getMessageList(bodyImMessageList!!)
        chatViewModel.readAllMsg(bodyReadAllMsg!!)

        linearLayoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = true
        }
        messageListAdapter2 = MessageListAdapter2(this).apply {
            preloadItemCount = 9
            onPreload = {
                // 预加载业务逻辑
                Log.d(TAG, "预加载:已经到倒数第2个item了去加载下一页 ")
                bodyImMessageList?.pageSize = 10
                Log.d(TAG, "预加载 index:${chatViewModel.index.value} ")
                bodyImMessageList?.index = chatViewModel.index.value
                chatViewModel.getMessageList(bodyImMessageList!!)
            }
        }


        binding.recyclerView.apply {

            layoutManager = linearLayoutManager
            adapter = messageListAdapter2


        }

        chatViewModel.messageCode.observe(this, Observer {
            when(it){
                0 ->{}
                1 -> {
                    messageListAdapter2?.notifyDataSetChanged()
                    messageListAdapter2?.submitList(chatViewModel.messageList.value)
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
                }
                20 -> {
                    messageListAdapter2?.isPreloading = false
                    binding.recyclerView.setItemAnimator(null);
                    messageListAdapter2?.notifyItemRangeChanged(chatViewModel.positionStart.value!!,chatViewModel.loaderItemCount.value!!)
//                    messageListAdapter2?.notifyDataSetChanged()
//                    binding.recyclerView.setItemAnimator(DefaultItemAnimator())
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this,chatViewModel.messageMsg.value)
                }
            }
        })

        val observer = binding.linearLayout3.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 获取View的新高度
                val height = binding.linearLayout3.height
                if (height != editHeight){
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
                    editHeight = height
                }
                Log.d(TAG, "onGlobalLayout:$height")
                // TODO: 处理高度变化

                // 移除监听器，避免重复调用
//                observer.removeOnGlobalLayoutListener(this)
            }
        })


//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val lastVisibleItemPosition = linearLayoutManager?.findLastVisibleItemPosition()
//                val itemCount = linearLayoutManager?.getItemCount()
//                if (itemCount!! > 0 && lastVisibleItemPosition!! >= itemCount - 5) {
//                    // 滚动到了倒数第5个item
//                    // TODO: 处理滚动事件
//                    Log.d(TAG, "onScrolled: 滚动到了倒数第五个")
//
//
//                    // 移除监听器，避免重复调用
//                    binding.recyclerView.removeOnScrollListener(this)
//                }
//            }
//        })

//        chatViewModel.messageListLiveData.observe(this, Observer {
//            lifecycleScope.launch {
//                delay(50)
////                messageListAdapter?.notifyDataSetChanged()
////                messageListAdapter?.submitList(it)
//                linearLayoutManager?.scrollToPositionWithOffset(0,0)
//                if (chatViewModel.needToScrollToTopConversationList){
//                    delay(50)
//                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
//                }
//                chatViewModel.needToScrollToTopConversationList = false
//            }
//        })

        EPSoftKeyBoardListener.setListener(this,object :EPSoftKeyBoardListener.OnSoftKeyBoardChangeListener{
            override fun keyBoardShow(height: Int) {
                linearLayoutManager?.scrollToPositionWithOffset(0,0)
            }

            override fun keyBoardHide(height: Int) {

            }

        })


    }





}