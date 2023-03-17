package com.hzdq.nppvdoctorclient.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private lateinit var binding:FragmentChatBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chat, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()


    }

    private fun initView(){
        binding.head.content.text = "聊天列表"
        binding.head.back.visibility = View.GONE
    }
}