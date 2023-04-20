package com.hzdq.nppvdoctorclient.login.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodySendMessage
import com.hzdq.nppvdoctorclient.body.BodySendMsg
import com.hzdq.nppvdoctorclient.databinding.FragmentVerificationCodeBinding
import com.hzdq.nppvdoctorclient.login.LoginViewModel
import com.hzdq.nppvdoctorclient.util.PhoneFormatCheckUtils
import com.hzdq.nppvdoctorclient.util.SizeUtil
import com.hzdq.nppvdoctorclient.util.ToastUtil

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VerificationCodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VerificationCodeFragment : Fragment() {

    private var bodySendMsg:BodySendMsg? = null

    private lateinit var binding:FragmentVerificationCodeBinding
    private lateinit var loginViewModel:LoginViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_verification_code, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(requireActivity()).get(LoginViewModel::class.java)
        initEdit()
        getCode()
    }

    /**
     * 获取验证码
     */
    private fun getCode(){
        binding.getCode.setOnClickListener {

            if (loginViewModel.phone.value.equals("")){
                ToastUtil.showToast(requireContext(),"手机号不能为空")
                return@setOnClickListener
            }
            if (!PhoneFormatCheckUtils.isPhoneLegal(loginViewModel.phone.value)){
                ToastUtil.showToast(requireContext(),"请输入正确的手机号码")
                return@setOnClickListener
            }
            bodySendMsg = BodySendMsg(5,loginViewModel.phone.value)
            loginViewModel.sendMsg(bodySendMsg!!)
            loginViewModel.countTime.start()
        }

        loginViewModel.timeCount.observe(requireActivity(), Observer {
            if (it > 0 && it <= 60){
                binding.getCode.text = "${it}秒"
                binding.getCode.isClickable = false
                binding.getCode.setBackgroundResource(R.drawable.login_get_code_bg_false)
            }else if ( it == 0) {
                binding.getCode.isClickable = true
                binding.getCode.setBackgroundResource(R.drawable.login_get_code_bg)
                binding.getCode.text = "重新获取验证码"
            }else if ( it == -1) {
                binding.getCode.isClickable = true
                binding.getCode.setBackgroundResource(R.drawable.login_get_code_bg)
                binding.getCode.text = "获取验证码"
            }
        })
    }

    private fun initEdit(){
        val passwordParams = binding.phone.image.layoutParams as ConstraintLayout.LayoutParams
        passwordParams.width = SizeUtil.dip2px(requireContext(),20f)
        passwordParams.height = SizeUtil.dip2px(requireContext(),20f)

        val phoneParams = binding.phone.image.layoutParams as ConstraintLayout.LayoutParams
        phoneParams.width = SizeUtil.dip2px(requireContext(),12f)
        phoneParams.height = SizeUtil.dip2px(requireContext(),18f)
        binding.phone.image.layoutParams = passwordParams
        binding.phone.edit.hint = "手机号"
        binding.phone.edit.inputType = InputType.TYPE_CLASS_NUMBER
        binding.phone.edit.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))
        binding.phone.edit.setText(loginViewModel.phone.value)
        binding.edit.setText(loginViewModel.verificationCode.value)

        binding.phone.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.phone.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        binding.edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginViewModel.verificationCode.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }
}