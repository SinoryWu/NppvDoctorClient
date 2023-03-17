package com.hzdq.nppvdoctorclient.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.FragmentMineBinding
import com.hzdq.nppvdoctorclient.mine.AboutActivity
import com.hzdq.nppvdoctorclient.mine.MineViewModel
import com.hzdq.nppvdoctorclient.mine.ModifyPasswordActivity
import com.hzdq.nppvdoctorclient.mine.PrivacyAgreementActivity

class MineFragment : Fragment() {

    private lateinit var mineViewModel: MineViewModel
    private lateinit var binding:FragmentMineBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mineViewModel = ViewModelProvider(requireActivity()).get(MineViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_mine, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        click()

    }

    private fun initView(){
        binding.modifyPassword.img.setImageResource(R.mipmap.mine_modify_password)
        binding.modifyPassword.content.text = "修改密码"

        binding.userAgreement.img.setImageResource(R.mipmap.mine_user_agreement)
        binding.userAgreement.content.text = "用户协议"

        binding.privacyAgreement.img.setImageResource(R.mipmap.mine_privacy_agreement)
        binding.privacyAgreement.content.text = "隐私协议"

        binding.about.img.setImageResource(R.mipmap.mine_about)
        binding.about.content.text = "关于"
        binding.about.version.visibility = View.VISIBLE
        binding.about.version.text = "版本 ${mineViewModel.getVerName(requireContext())}"

        binding.logOut.img.setImageResource(R.mipmap.mine_log_out)
        binding.logOut.content.text = "退出登录"
    }

    private fun click(){


        binding.logOut.layout.setOnClickListener {
            mineViewModel.logOut.value = "show"
        }

        binding.about.layout.setOnClickListener {
            startActivity(Intent(requireActivity(),AboutActivity::class.java))
        }

        binding.privacyAgreement.layout.setOnClickListener {
            val intent = Intent(requireActivity(),PrivacyAgreementActivity::class.java)
            intent.putExtra("type","privacy")
            startActivity(intent)
        }

        binding.userAgreement.layout.setOnClickListener {
            val intent = Intent(requireActivity(),PrivacyAgreementActivity::class.java)
            intent.putExtra("type","user")
            startActivity(intent)
        }

        binding.modifyPassword.layout.setOnClickListener {
            startActivity(Intent(requireActivity(),ModifyPasswordActivity::class.java))
        }
    }



}