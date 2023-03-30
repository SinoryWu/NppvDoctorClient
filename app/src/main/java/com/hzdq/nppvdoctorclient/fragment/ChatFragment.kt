package com.hzdq.nppvdoctorclient.fragment

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.hzdq.nppvdoctorclient.ChatCommonViewModel
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodyReadAllMsg
import com.hzdq.nppvdoctorclient.chat.ChatActivity
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.chat.adapter.ConversationListAdapter
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListNetWorkStatus
import com.hzdq.nppvdoctorclient.databinding.FragmentChatBinding
import com.hzdq.nppvdoctorclient.util.*
import com.hzdq.viewmodelshare.shareViewModels
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    private lateinit var binding:FragmentChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private  var shp:Shp? = null
    private val CHAT_REQUEST_CODE = 0x000015
    private var  conversationListAdapter:ConversationListAdapter? = null
    private val vm: ChatCommonViewModel by shareViewModels("sinory")
    private var tokenDialogUtil :TokenDialogUtil? = null
    private var bodyReadAllMsg:BodyReadAllMsg? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chat, container, false)
        return binding.root
    }

    override fun onDestroy() {
        shp?.saveToSp("ConversationSearchName","")
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        shp = Shp(requireContext())
        shp?.saveToSp("ConversationSearchName","")
        tokenDialogUtil = TokenDialogUtil(requireContext())
        initView()
        conversationListAdapter = ConversationListAdapter(chatViewModel)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.apply {
            adapter = conversationListAdapter
            layoutManager = linearLayoutManager
        }
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        chatViewModel.conversationListLiveData.observe(requireActivity(), Observer {
            lifecycleScope.launch {
//                delay(100)
                if (null != it){
                    if (it.size>0){
                        for (i in 0 until it.size){
                            chatViewModel.chatCountTotal.value = chatViewModel.chatCountTotal.value!! + it[i]!!.numberOfUnreadMessages!!
                        }
                    }
                }
                conversationListAdapter?.notifyDataSetChanged()
                conversationListAdapter?.submitList(it)
                if (chatViewModel.needToScrollToTopConversationList){
                    delay(50)
                    linearLayoutManager.scrollToPositionWithOffset(0,0)
                }
                chatViewModel.needToScrollToTopConversationList = false
            }
        })

        chatViewModel.imConversationListNetWorkStatus.observe(requireActivity(), Observer {
            if (it == ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADED){

//                binding.userFragmentRecyclerView.scrollToPosition(0)
                linearLayoutManager.scrollToPositionWithOffset(0,0)
                chatViewModel.needToScrollToTopConversationList = true
            }
            conversationListAdapter?.updateNetWorkStatus(it)
//            binding.refresh.isRefreshing = it == ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADING
            binding.refresh.isRefreshing = false
        })

        binding.refresh.setOnRefreshListener {
            chatViewModel.conversationRefresh.value = 1

        }
        //刷新列表
        chatViewModel.conversationRefresh.observe(requireActivity(), Observer {
            when(it){
                1 -> {
                    conversationListAdapter?.notifyDataSetChanged()
                    chatViewModel.resetConversationListQuery()

                    chatViewModel.conversationRefresh.value = 0
                }
                else -> {
                    Log.d("frontUserRefresh", "不刷新 ")
                }
            }
        })

        vm.groupAcceptInvitation.observe(requireActivity(), Observer {
            if (it == 1){
                chatViewModel.conversationRefresh.value = 1
                vm.groupAcceptInvitation.value = 0
            }
        })


        binding.search.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                //Perform Code
                binding.refresh.isRefreshing = true
                shp?.saveToSp("ConversationSearchName",binding.search.text.toString())

                chatViewModel.conversationRefresh.value = 1
                HideKeyboard.hideKeyboard(v,requireContext())

            }
            false
        }

        binding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().equals("")){
                    binding.refresh.isRefreshing = true
                    shp?.saveToSp("ConversationSearchName","")
                    chatViewModel.conversationRefresh.value = 1
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        Log.d("token", "${shp?.getToken()} ")

        conversationListAdapter?.setOnItemClickListener(object :ConversationListAdapter.OnItemClickListener{
            override fun onItemClick(groupId: Int,groupName:String,joinState:Int,groupThirdPartyId:String,position:Int) {

                chatViewModel.conversationPosition.value = position
                shp?.saveToSpInt("groupId",groupId)
                val intent = Intent(requireActivity(),ChatActivity::class.java)
                intent.putExtra("groupId",groupId)
                intent.putExtra("groupThirdPartyId",groupThirdPartyId)
                intent.putExtra("groupName",groupName)
                intent.putExtra("joinState",joinState)
                permission(intent)

            }

        })

        chatViewModel.readMsgCode.observe(requireActivity(), Observer {
            when(it){
                0 -> {}
                1 -> {
                    bodyReadAllMsg = null
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(requireContext(),chatViewModel.readMsgMsg.value)
                }
            }
        })

        vm.receiverCount.observe(requireActivity(), Observer { receiverCount->
            var index = 0
            try {
                if (receiverCount > 0 ){
                    for (i in 0 until vm.dataClassReceiverList.value!!.size){
                        if (chatViewModel.isChat.value == false){
                            chatViewModel.chatCountTotal.value = chatViewModel.chatCountTotal.value!! + 1
                        }
                        index = chatViewModel.conversationListLiveData.value?.indexOfFirst { pageList->
                            pageList.groupThirdPartyId.equals(vm.dataClassReceiverList.value!![i].conversationId)
                        }!!
                        if (index != -1){
                            chatViewModel.conversationListLiveData.value!![index]!!.numberOfUnreadMessages = chatViewModel.conversationListLiveData.value!![index]!!.numberOfUnreadMessages!! + 1
                            chatViewModel.conversationListLiveData.value!![index]!!.lastMessageType = vm.dataClassReceiverList.value!![i].messageType
                            chatViewModel.conversationListLiveData.value!![index]!!.lastMessage = vm.dataClassReceiverList.value!![i].messageContent
                            chatViewModel.conversationListLiveData.value!![index]!!.lastMsgTime = DateUtil.stamp2Date(System.currentTimeMillis())

                            conversationListAdapter?.notifyItemChanged( index,chatViewModel.conversationListLiveData.value!![index])
                        }


                    }
                }

            }catch (e:Exception){
                Log.d("receiverCount", "ChatFragment Exception:$e ")
            }

        })

    }

    fun permission(intent: Intent){
        PermissionX.init(this).permissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).request{allGrand,_,_ ->
            if (allGrand){

                startActivityForResult(intent,CHAT_REQUEST_CODE)
            }else{
                ToastUtil.showToast(requireContext(),"未打开相应权限")
            }

        }





    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHAT_REQUEST_CODE){
            chatViewModel.resetConversationListQuery()
            if (resultCode == 20){
                ToastUtil.showToast(requireContext(),"已退出群聊")
            }
//            if (resultCode == RESULT_OK){
////                bodyReadAllMsg = BodyReadAllMsg(data?.getIntExtra("groupId",0))
////                chatViewModel.readAllMsg(bodyReadAllMsg!!)
//                if (null != chatViewModel.conversationListLiveData.value){
//                    chatViewModel.chatCountTotal.value = chatViewModel.chatCountTotal.value!! - chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.numberOfUnreadMessages!!
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.numberOfUnreadMessages = 0
//                }
//
//                if (data?.getStringExtra("lastMsgTime") != null){
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.lastMsgTime = data.getStringExtra("lastMsgTime")
//                }
//                if (data?.getStringExtra("lastMessage") != null){
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.lastMessage = data.getStringExtra("lastMessage")
//                }
//
//                if (data?.getIntExtra("lastMessageType",1) != null){
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.lastMessageType = data.getIntExtra("lastMessageType",1)
//                }
//
//                conversationListAdapter?.notifyItemChanged( chatViewModel.conversationPosition.value!!,chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!])
//            }else if (resultCode == 20){
//                bodyReadAllMsg = BodyReadAllMsg(data?.getIntExtra("groupId",0))
//                chatViewModel.readAllMsg(bodyReadAllMsg!!)
//                if (null != chatViewModel.conversationListLiveData.value){
//                    chatViewModel.chatCountTotal.value = chatViewModel.chatCountTotal.value!! - chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.numberOfUnreadMessages!!
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.numberOfUnreadMessages = 0
//                }
//
//                if (data?.getStringExtra("lastMsgTime") != null){
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.lastMsgTime = data.getStringExtra("lastMsgTime")
//                }
//                if (data?.getStringExtra("lastMessage") != null){
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.lastMessage = data.getStringExtra("lastMessage")
//                }
//
//                if (data?.getIntExtra("lastMessageType",1) != null){
//                    chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.lastMessageType = data.getIntExtra("lastMessageType",1)
//                }
//                chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.joinState = 1
//                conversationListAdapter?.notifyItemChanged( chatViewModel.conversationPosition.value!!,chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!])
//            }
        }
    }

    override fun onDestroyView() {
        tokenDialogUtil?.disMissTokenDialog()
        super.onDestroyView()
    }

    private fun initView(){
        binding.head.content.text = "聊天列表"
        binding.head.back.visibility = View.GONE
    }
}