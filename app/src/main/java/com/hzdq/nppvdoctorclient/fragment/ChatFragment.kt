package com.hzdq.nppvdoctorclient.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.chat.ChatActivity
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.chat.adapter.ConversationListAdapter
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListNetWorkStatus
import com.hzdq.nppvdoctorclient.databinding.FragmentChatBinding
import com.hzdq.nppvdoctorclient.util.HideKeyboard
import com.hzdq.nppvdoctorclient.util.Shp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.http.POST

class ChatFragment : Fragment() {

    private lateinit var binding:FragmentChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private  var shp:Shp? = null
    private val CHAT_REQUEST_CODE = 0x000015
    private var  conversationListAdapter:ConversationListAdapter? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        shp = Shp(requireContext())
        shp?.saveToSp("ConversationSearchName","")
        initView()
        conversationListAdapter = ConversationListAdapter(chatViewModel)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.apply {
            adapter = conversationListAdapter
            layoutManager = linearLayoutManager
        }


        chatViewModel.conversationListLiveData.observe(requireActivity(), Observer {
            lifecycleScope.launch {
                delay(50)
                conversationListAdapter?.notifyDataSetChanged()
                conversationListAdapter?.submitList(it)
                if (chatViewModel.needToScrollToTopConversationList){
                    delay(50)
                    linearLayoutManager.scrollToPositionWithOffset(0,0)
                }
                chatViewModel.needToScrollToTopConversationList = false
            }
        })

        chatViewModel.imConversationListNetWorkStatus.observe(viewLifecycleOwner, Observer {
            if (it == ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADED){

//                binding.userFragmentRecyclerView.scrollToPosition(0)
                linearLayoutManager.scrollToPositionWithOffset(0,0)
                chatViewModel.needToScrollToTopConversationList = true
            }
            conversationListAdapter?.updateNetWorkStatus(it)
            binding.refresh.isRefreshing = it == ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADING
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


        binding.search.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                //Perform Code
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
                    shp?.saveToSp("ConversationSearchName","")
                    chatViewModel.conversationRefresh.value = 1
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        conversationListAdapter?.setOnItemClickListener(object :ConversationListAdapter.OnItemClickListener{
            override fun onItemClick(groupId: Int,groupName:String,position:Int) {
                chatViewModel.conversationPosition.value = position
                shp?.saveToSpInt("groupId",groupId)
                val intent = Intent(requireActivity(),ChatActivity::class.java)
                intent.putExtra("groupId",groupId)
                intent.putExtra("groupName",groupName)
                startActivityForResult(intent,CHAT_REQUEST_CODE)
            }

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHAT_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!]!!.numberOfUnreadMessages = 0
                conversationListAdapter?.notifyItemChanged( chatViewModel.conversationPosition.value!!,chatViewModel.conversationListLiveData.value!![chatViewModel.conversationPosition.value!!])
            }
        }
    }

    private fun initView(){
        binding.head.content.text = "聊天列表"
        binding.head.back.visibility = View.GONE
    }
}