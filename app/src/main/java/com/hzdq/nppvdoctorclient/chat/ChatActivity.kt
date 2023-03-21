package com.hzdq.nppvdoctorclient.chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.chat.adapter.MessageListAdapter
import com.hzdq.nppvdoctorclient.databinding.ActivityChatBinding
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private var messageListAdapter:MessageListAdapter? = null
    private var linearLayoutManager:LinearLayoutManager? = null
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



        binding.head.content.text = intent.getStringExtra("groupName")

        binding.head.back.setOnClickListener {
            finish()
        }

        linearLayoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = true
        }
        messageListAdapter = MessageListAdapter(chatViewModel)

        binding.recyclerView.apply {
            adapter = messageListAdapter
            layoutManager = linearLayoutManager
        }

        chatViewModel.messageListLiveData.observe(this, Observer {

            lifecycleScope.launch {
                delay(50)
                messageListAdapter?.notifyDataSetChanged()
                messageListAdapter?.submitList(it)
                linearLayoutManager?.scrollToPositionWithOffset(0,0)
                if (chatViewModel.needToScrollToTopConversationList){
                    delay(50)
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
                }
                chatViewModel.needToScrollToTopConversationList = false
            }
        })

    }






}