package com.hzdq.nppvdoctorclient.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.chat.adapter.GroupMemberAdapter
import com.hzdq.nppvdoctorclient.chat.dialog.ExitGroupDialog
import com.hzdq.nppvdoctorclient.databinding.ActivityGroupDetailBinding
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.ToastUtil
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil
import com.hzdq.nppvdoctorclient.util.ViewClickDelay.clickDelay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@desc 群设置（详情）
 *@Author Sinory
 *@date 2023/3/16 12:46
 */
class GroupDetailActivity : AppCompatActivity() {
    private var tokenDialogUtil: TokenDialogUtil? = null
    private lateinit var binding: ActivityGroupDetailBinding
    private val ADD_DOCTOR_REQUEST_CODE = 0x000014
    private lateinit var chatViewModel: ChatViewModel
    private val TAG = "GroupDetailActivity"
    private var groupMemberAdapter: GroupMemberAdapter? = null
    private lateinit var shp: Shp
    private var exitGroupDialog: ExitGroupDialog? = null
    override fun onDestroy() {
        exitGroupDialog?.dismiss()
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_detail)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        tokenDialogUtil = TokenDialogUtil(this)
        ActivityCollector.addActivity(this)
        shp = Shp(this)
        chatViewModel.groupId.value = intent.getIntExtra("groupId", 0)
        chatViewModel.getGroupMembers(chatViewModel.groupId.value!!)
        groupMemberAdapter = GroupMemberAdapter(shp.getUserName()!!)
        initView()
    }

    private fun initView() {
        binding.head.content.text = "群设置"
        binding.head.back.setOnClickListener {
            finish()
        }
        if (shp.getRoleType() == 2) {
            //医生
            binding.head.add.visibility = View.GONE
        } else {
            binding.head.add.visibility = View.VISIBLE
        }

        val startActivityAddDoctor = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == RESULT_OK){
                lifecycleScope.launch {
                    delay(500)
                    ToastUtil.showToast(this@GroupDetailActivity,"添加群成员成功")
                    chatViewModel.groupMemberList.value?.clear()
                    chatViewModel.getGroupMembers(chatViewModel.groupId.value!!)
                }

            }else if (it.resultCode == 20){
                lifecycleScope.launch {
                    delay(500)
                    ToastUtil.showToast(this@GroupDetailActivity,"群成员未完全添加成功")
                    chatViewModel.groupMemberList.value?.clear()
                    chatViewModel.getGroupMembers(chatViewModel.groupId.value!!)
                }
            }
        }


        binding.head.add.clickDelay {
            var uidList = ArrayList<Int>()
            if (chatViewModel.groupMemberList.value!!.size > 0){
                for (i in 0 until chatViewModel.groupMemberList.value!!.size){
                    uidList.add(chatViewModel.groupMemberList.value!![i]!!.uid!!)
                }
            }

            val intent = Intent(this, AddDoctorActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("groupId",chatViewModel.groupId.value!!)
            bundle.putIntegerArrayList("uidList",uidList)
            intent.putExtra("bundle",bundle)
            startActivityAddDoctor.launch(intent)

        }


        chatViewModel.groupCode.observe(this, Observer {
            when (it) {
                0 -> {
                }
                1 -> {
                    if (chatViewModel.groupMemberList.value!!.isNotEmpty()) {
                        //成员列表
                        groupMemberAdapter?.notifyDataSetChanged()
                        groupMemberAdapter?.submitList(chatViewModel.groupMemberList.value)
                    }
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this, chatViewModel.groupMsg.value)
                }
            }
        })

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = groupMemberAdapter
            layoutManager = linearLayoutManager
        }
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.exitGroup.setOnClickListener {

            if (exitGroupDialog == null) {
                Log.d(TAG, "initView: exitGroupDialog = null")
                exitGroupDialog = ExitGroupDialog(this, R.style.CustomDialog)
                exitGroupDialog!!.setCancel(object : ExitGroupDialog.CancelAction {
                    override fun onLeftClick() {
                        exitGroupDialog = null
                    }

                })
                exitGroupDialog!!.setConfirm(object : ExitGroupDialog.ConfirmAction {
                    override fun onRightClick() {
                        //退出群聊
                        exitGroupDialog!!.dismiss()
                        exitGroupDialog = null
//                        setResult(20)
//                        finish()
                        chatViewModel.exitGroup()

                    }

                })
                exitGroupDialog!!.show()
                exitGroupDialog!!.setCanceledOnTouchOutside(false)
            }else {
                Log.d(TAG, "initView: exitGroupDialog != null")
            }
        }

        chatViewModel.exitCode.observe(this, Observer {
            when (it) {
                0 -> {
                }
                1 -> {
                    setResult(20)
                    finish()
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this, chatViewModel.exitMsg.value)
                }
            }
        })
    }
}