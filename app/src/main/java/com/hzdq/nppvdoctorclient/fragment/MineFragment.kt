package com.hzdq.nppvdoctorclient.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.MainViewModel
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.FragmentMineBinding
import com.hzdq.nppvdoctorclient.mine.AboutActivity
import com.hzdq.nppvdoctorclient.mine.MineViewModel
import com.hzdq.nppvdoctorclient.mine.ModifyPasswordActivity
import com.hzdq.nppvdoctorclient.mine.PrivacyAgreementActivity
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.ViewClickDelay.clickDelay

class MineFragment : Fragment() {

    private lateinit var mineViewModel: MineViewModel
    private lateinit var binding:FragmentMineBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var shp:Shp
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mineViewModel = ViewModelProvider(requireActivity()).get(MineViewModel::class.java)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_mine, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shp = Shp(requireContext())
        initView()

        click()

        mainViewModel.userName.observe(requireActivity(), Observer {
            binding.name.text = it
        })

        mainViewModel.hospitalName.observe(requireActivity(), Observer {
            binding.hospital.text = it
        })

        mainViewModel.userInfoCode.observe(requireActivity(), Observer {
            if (it == 1){
                if (shp.getRoleType() == 2){
                    binding.type.text = mainViewModel.doctorTitle.value
                }
            }
        })
        if (shp.getRoleType() == 2){

            binding.head.setImageResource(R.mipmap.mine_head_doctor)

        }else if (shp.getRoleType() == 7){
            binding.type.text = "仓库管理"
            binding.head.setImageResource(R.mipmap.mine_head_bajie)
        }else {
            binding.type.text = "医助"
            binding.head.setImageResource(R.mipmap.mine_head_bajie)
        }
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

        binding.about.layout.clickDelay {
            startActivity(Intent(requireActivity(),AboutActivity::class.java))
        }

        binding.privacyAgreement.layout.clickDelay {
            val intent = Intent(requireActivity(),PrivacyAgreementActivity::class.java)
            intent.putExtra("type","privacy")
            startActivity(intent)
        }

        binding.userAgreement.layout.clickDelay {
            val intent = Intent(requireActivity(),PrivacyAgreementActivity::class.java)
            intent.putExtra("type","user")
            startActivity(intent)
        }

        binding.modifyPassword.layout.clickDelay {
            startActivity(Intent(requireActivity(),ModifyPasswordActivity::class.java))
        }
    }



}