package com.hzdq.nppvdoctorclient.login.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.FragmentPassWordBinding
import com.hzdq.nppvdoctorclient.login.LoginViewModel
import com.hzdq.nppvdoctorclient.util.SizeUtil

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PassWordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PassWordFragment : Fragment() {
    private lateinit var binding: FragmentPassWordBinding
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_pass_word, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel  = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        initEdit()



    }

    private fun initEdit(){
        val passwordParams = binding.password.image.layoutParams as ConstraintLayout.LayoutParams
        passwordParams.width = SizeUtil.dip2px(requireContext(),20f)
        passwordParams.height = SizeUtil.dip2px(requireContext(),20f)
        binding.password.image.layoutParams = passwordParams
        val phoneParams = binding.user.image.layoutParams as ConstraintLayout.LayoutParams
        phoneParams.width = SizeUtil.dip2px(requireContext(),12f)
        phoneParams.height = SizeUtil.dip2px(requireContext(),18f)
        binding.user.image.layoutParams = phoneParams
        binding.user.edit.hint = "账号"
        binding.password.edit.hint ="密码"
        binding.password.image.setImageResource(R.mipmap.icon_login_password)
        binding.user.image.setImageResource(R.mipmap.icon_login_phone)
        binding.password.edit.transformationMethod = PasswordTransformationMethod()
        binding.user.edit.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))
        if (loginViewModel.user.value.equals("")){
            //账号为空直接设置账号为手机号
            binding.user.edit.setText(loginViewModel.phone.value)
            loginViewModel.user.value = loginViewModel.phone.value
        }else {
            //账号不为空
            binding.user.edit.setText(loginViewModel.user.value)
        }


        binding.password.edit.setText(loginViewModel.password.value)



        binding.user.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.user.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.password.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.password.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }
}