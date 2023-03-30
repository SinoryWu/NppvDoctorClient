package com.hzdq.nppvdoctorclient.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodyGroupInvitation
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.dataclass.DoctorList
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.EnglishUtil
import java.util.regex.Pattern
import kotlin.math.max

/**
 *Time:2023/3/27
 *Author:Sinory
 *Description:
 */
class DoctorListAdapter(val chatViewModel: ChatViewModel):ListAdapter<DoctorList, DoctorListAdapter.MyViewHolder>(
    DIFFCALLBACK
) {

    var bodyGroupInvitation :BodyGroupInvitation? = null
    val urlCollection = URLCollection
    // 预加载回调
    var onPreload: (() -> Unit)? = null
    // 预加载偏移量
    var preloadItemCount = 0
    // 增加预加载状态标记位
    var isPreloading = false
    // 列表滚动状态
    private var scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE

    object DIFFCALLBACK: DiffUtil.ItemCallback<DoctorList>() {
        override fun areItemsTheSame(oldItem: DoctorList, newItem: DoctorList): Boolean {
            //判断两个item是否相同这里是比较对象是否为同一个对象  ===表示判断是否是同一个对象 ==比较的是内容
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: DoctorList, newItem: DoctorList): Boolean {
            //判断两个item内容是否相同
            return oldItem.uid == newItem.uid
        }

    }

    private var mClickListener: OnItemClickListener? = null
    //设置回调接口
    interface OnItemClickListener {
        fun onItemClick()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mClickListener = listener
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        checkPreload(holder.absoluteAdapterPosition)
        val dataItem = getItem(holder.absoluteAdapterPosition)
        holder.name.text = dataItem.name
        val word = dataItem.pinyin!!.substring(0,1)
        holder.word.text = word

        holder.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                chatViewModel.groupInvitationListSize.value = chatViewModel.groupInvitationListSize.value!! + 1
                bodyGroupInvitation = BodyGroupInvitation(chatViewModel.groupId.value,dataItem.uid,dataItem.userType)
                chatViewModel.groupInvitationList.value!!.add(bodyGroupInvitation!!)
            }else {
                chatViewModel.groupInvitationListSize.value = chatViewModel.groupInvitationListSize.value!! - 1
                bodyGroupInvitation = BodyGroupInvitation(1,dataItem.uid,dataItem.userType)
                chatViewModel.groupInvitationList.value!!.remove(bodyGroupInvitation!!)
            }
        }
        if (position == 0){  //若每种信息字母的第一行显示
            holder.wordLayout.setVisibility(View.VISIBLE)
        }else {
            //得到前一个位置对应的字母，如果当前的字母和上一个相同，隐藏TextView；否则就显示
            var preWord = ""
            if (EnglishUtil.isEnglishAlphabet(getItem(position - 1).pinyin!!.substring(0, 1))){
                preWord = getItem(position - 1).pinyin!!.substring(0, 1) //得到上一个字母A~Z
                if (word.equals(preWord)){  //若word和preWord相同
                    holder.wordLayout.setVisibility(View.GONE) //隐藏
                }else {
                    holder.wordLayout.setVisibility(View.VISIBLE) //显示
                }
            }else {
                if (word.equals("#")){  //若word和preWord相同
                    holder.wordLayout.setVisibility(View.GONE) //隐藏
                }else {
                    holder.wordLayout.setVisibility(View.VISIBLE) //显示
                }

            }


        }
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.item_add_doctor_name)
        val wordLayout = itemView.findViewById<FrameLayout>(R.id.item_add_doctor_word_layout)
        val word = itemView.findViewById<TextView>(R.id.item_add_doctor_word)
        val checkBox = itemView.findViewById<CheckBox>(R.id.item_add_doctor_checkBox)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_doctor, parent, false)
        )
    }


    // 判断是否进行预加载
    private fun checkPreload(position: Int) {
        if (onPreload != null
            && position == max(itemCount - 1 - preloadItemCount, 0)// 索引值等于阈值
            && scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE // 列表正在滚动
            && !isPreloading // 预加载不在进行中
        ) {
            isPreloading = true // 表示正在执行预加载

            onPreload?.invoke()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // 更新滚动状态
                scrollState = newState
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }


    override fun getItemViewType(position: Int): Int {
        return getItem(position).uid!!
    }






}


