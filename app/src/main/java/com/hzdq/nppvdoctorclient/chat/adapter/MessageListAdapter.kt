package com.hzdq.nppvdoctorclient.chat.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListNetWorkStatus
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListNetWorkStatus
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.util.SizeUtil

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */
class MessageListAdapter(val chatViewModel: ChatViewModel):PagedListAdapter<ImMessageList,RecyclerView.ViewHolder>(
   DIFFCALLBACK
) {
    init {
        chatViewModel.retryMessageList()
    }
    object DIFFCALLBACK: DiffUtil.ItemCallback<ImMessageList>() {
        override fun areItemsTheSame(oldItem: ImMessageList, newItem: ImMessageList): Boolean {
            //判断两个item是否相同这里是比较对象是否为同一个对象  ===表示判断是否是同一个对象 ==比较的是内容
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ImMessageList, newItem: ImMessageList): Boolean {
            //判断两个item内容是否相同
            return oldItem.id == newItem.id
        }

    }

    //创建一个成员变量来保存网络状态
    private var netWorkStatus: ImMessageListNetWorkStatus?=  null

    //加载第一组数据之前不显示footer 因为图片都会插入到footer前面导致一开始加载出的列表在最底部
    private var hasFooter = false
    private var mClickListener: OnItemClickListener? = null
    //设置回调接口
    interface OnItemClickListener {
        fun onItemClick()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mClickListener = listener
    }

    //根据网络状态更新底部页脚是否显示
    fun updateNetWorkStatus(netWorkStatus: ImMessageListNetWorkStatus?){
        this.netWorkStatus = netWorkStatus
        if(netWorkStatus == ImMessageListNetWorkStatus.IM_MESSAGE_LIST_INITIAL_LOADING){
            hideFooter()
        }else{
            showFooter()
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() +if (hasFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        //根据position位置 如果等于最后一个item 返回FOOTER_VIEW_TYPE 否则返回NORMAL_VIEW_TYPE
        return if (hasFooter && position == itemCount-1) R.layout.layout_footer_message else R.layout.item_chat
    }

    //写两个函数根据网络状态决定显示footer或者不显示footer
    private fun hideFooter(){
        if(hasFooter){
            notifyItemRemoved(itemCount - 1)
        }
        hasFooter = false
    }
    private fun showFooter(){
        if (hasFooter){
            notifyItemChanged(itemCount - 1)
        }else {
            hasFooter = true
            notifyItemInserted(itemCount - 1)
        }
    }

    //局部刷新
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()){
            super.onBindViewHolder(holder, position, payloads)
            return
        }else {
            when(holder.itemViewType){
                R.layout.layout_footer_message -> {
                    (holder as MessageListFooterViewHolder).bindWithNetWorkStatus(netWorkStatus)
                }
                else -> {
                    val dataItem = getItem(position) ?: return
                    (holder as MessageListViewHolder).bindWithItem(dataItem)

                }
            }
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            R.layout.layout_footer_message -> {
                (holder as MessageListFooterViewHolder).bindWithNetWorkStatus(netWorkStatus)
            }
            else -> {
                val dataItem = getItem(position) ?: return
                (holder as MessageListViewHolder).bindWithItem(dataItem)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            R.layout.item_chat -> {
                MessageListViewHolder.newInstance(parent)
            }
            else -> {
                MessageListFooterViewHolder.newInstance(parent).also {
                    it.itemView.setOnClickListener {
                        chatViewModel.retryMessageList()
                    }
                }
            }
        }
    }





}

class MessageListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    val fromLayout = itemView.findViewById<ConstraintLayout>(R.id.item_chat_from)
    val fromHead  = itemView.findViewById<ImageView>(R.id.item_chat_from_head)
    val fromName = itemView.findViewById<TextView>(R.id.item_chat_from_name)
    val fromType = itemView.findViewById<TextView>(R.id.item_chat_from_type)
    val fromContent = itemView.findViewById<TextView>(R.id.item_chat_from_content)


    val toLayout = itemView.findViewById<ConstraintLayout>(R.id.item_chat_to)
    val toHead  = itemView.findViewById<ImageView>(R.id.item_chat_to_head)
    val toName = itemView.findViewById<TextView>(R.id.item_chat_to_name)
    val toType = itemView.findViewById<TextView>(R.id.item_chat_to_type)
    val toContent = itemView.findViewById<TextView>(R.id.item_chat_to_content)

    val time = itemView.findViewById<TextView>(R.id.item_chat_time)

    companion object{
        fun newInstance(parent: ViewGroup):MessageListViewHolder{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat,parent,false)
            return MessageListViewHolder(view)
        }
    }
    fun bindWithItem(imMessageList: ImMessageList){
        with(itemView){
            Log.d("asdasd", "bindWithItem:${imMessageList.gmtCreate} ")
            when(imMessageList.oneself){
                true -> {
                    fromLayout.visibility = View.GONE
                    toLayout.visibility = View.VISIBLE
                    toName.text = imMessageList.formUser?.userName
                    if (imMessageList.messageType == 1){

                        toContent.text = imMessageList.message
                    }else {
                        toContent.text = "[图片]"
                    }
                    when (imMessageList.formUser?.userType){
                        1 -> {
                            toType.text = "医生"
                            val params = toName.layoutParams as ConstraintLayout.LayoutParams

                            params.marginEnd = SizeUtil.dip2px(context,8f)
                            toName.layoutParams = params
                        }
                        2 -> {
                            toType.text = "医助"
                            val params = toName.layoutParams as ConstraintLayout.LayoutParams

                            params.marginEnd = SizeUtil.dip2px(context,8f)
                            toName.layoutParams = params
                        }
                        else -> {
                            toType.text = ""
                            val params = toName.layoutParams as ConstraintLayout.LayoutParams

                            params.marginEnd = SizeUtil.dip2px(context,0f)
                            toName.layoutParams = params
                        }
                    }
                }
                else -> {
                    fromLayout.visibility = View.VISIBLE
                    toLayout.visibility = View.GONE
                    fromName.text = imMessageList.formUser?.userName
                    if (imMessageList.messageType == 1){

                        fromContent.text = imMessageList.message
                    }else {
                        fromContent.text = "[图片]"
                    }
                    when (imMessageList.formUser?.userType){
                        1 -> {
                            fromType.text = "医生"
                        }
                        2 -> {
                            fromType.text = "医助"
                        }
                        else -> {
                            fromType.text = ""
                        }
                    }
                }

            }


        }
    }
}

class MessageListFooterViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    val layout = itemView.findViewById<LinearLayout>(R.id.footer_text_layout)
    val textView = itemView.findViewById<TextView>(R.id.footer_text_message)
    val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_footer_message)
    companion object{
        fun newInstance(parent: ViewGroup): MessageListFooterViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_footer_message,parent,false)
            return MessageListFooterViewHolder(view)
        }
    }
    fun bindWithNetWorkStatus(netWorkStatus: ImMessageListNetWorkStatus?){


        with(itemView){
            when(netWorkStatus){
                ImMessageListNetWorkStatus.IM_MESSAGE_LIST_FAILED -> {
                    layout.visibility = View.VISIBLE
                    textView.text = "点击重试"
                    progressBar.visibility = View.GONE
                    isClickable = true
                }
                ImMessageListNetWorkStatus.IM_MESSAGE_LIST_COMPLETED -> {
                    layout.visibility = View.GONE
                    textView.text = "全部加载完成"
                    progressBar.visibility = View.GONE
                    isClickable = false
                }
                else -> {
                    layout.visibility = View.VISIBLE
                    textView.text = "正在加载"
                    progressBar.visibility = View.VISIBLE
                    isClickable = false
                }
            }
        }
    }


}
