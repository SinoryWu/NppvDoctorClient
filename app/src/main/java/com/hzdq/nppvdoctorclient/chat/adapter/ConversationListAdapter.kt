package com.hzdq.nppvdoctorclient.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListNetWorkStatus
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListNetWorkStatus
import com.hzdq.nppvdoctorclient.dataclass.ImConversationList
import com.hzdq.nppvdoctorclient.util.DateFormatUtil
import com.hzdq.nppvdoctorclient.util.ViewClickDelay.clickDelay

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */
class ConversationListAdapter(val chatViewModel: ChatViewModel):PagedListAdapter<ImConversationList,RecyclerView.ViewHolder>(DIFFCALLBACK) {

    init {
        chatViewModel.retryConversationList()

    }
    object DIFFCALLBACK: DiffUtil.ItemCallback<ImConversationList>() {
        override fun areItemsTheSame(oldItem: ImConversationList, newItem: ImConversationList): Boolean {
            //判断两个item是否相同这里是比较对象是否为同一个对象  ===表示判断是否是同一个对象 ==比较的是内容
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ImConversationList, newItem: ImConversationList): Boolean {
            //判断两个item内容是否相同
            return oldItem.groupId == newItem.groupId
        }

    }

    //创建一个成员变量来保存网络状态
    private var netWorkStatus: ImConversationListNetWorkStatus?=  null

    //加载第一组数据之前不显示footer 因为图片都会插入到footer前面导致一开始加载出的列表在最底部
    private var hasFooter = false
    private var mClickListener: OnItemClickListener? = null
    //设置回调接口
    interface OnItemClickListener {
        fun onItemClick(groupId:Int,groupName:String,joinState:Int,groupThirdPartyId:String,position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mClickListener = listener
    }

    //根据网络状态更新底部页脚是否显示
    fun updateNetWorkStatus(netWorkStatus: ImConversationListNetWorkStatus?){
        this.netWorkStatus = netWorkStatus
        if(netWorkStatus == ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADING){
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
        return if (hasFooter && position == itemCount-1) R.layout.layout_footer_message else R.layout.item_conversation_list
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
                    (holder as ConversationListFooterViewHolder).bindWithNetWorkStatus(netWorkStatus)
                }
                else -> {
                    val dataItem = getItem(position) ?: return
                    (holder as ConversationListViewHolder).bindWithItem(dataItem)
                    holder.itemView.clickDelay {
                        dataItem.groupThirdPartyId?.let { it1 ->
                            mClickListener?.onItemClick(dataItem.groupId!!,dataItem.groupName!!,dataItem.joinState!!,
                                it1,holder.absoluteAdapterPosition)
                        }
                    }
                }
            }
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            R.layout.layout_footer_message -> {
                (holder as ConversationListFooterViewHolder).bindWithNetWorkStatus(netWorkStatus)
            }
            else -> {
                val dataItem = getItem(position) ?: return
                (holder as ConversationListViewHolder).bindWithItem(dataItem)
                holder.itemView.clickDelay {
                    dataItem.groupThirdPartyId?.let { it1 ->
                        mClickListener?.onItemClick(dataItem.groupId!!,dataItem.groupName!!,dataItem.joinState!!,
                            it1,holder.absoluteAdapterPosition)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            R.layout.item_conversation_list -> {
                ConversationListViewHolder.newInstance(parent)
            }
            else -> {
                ConversationListFooterViewHolder.newInstance(parent).also {
                    it.itemView.setOnClickListener {
                        chatViewModel.retryConversationList()
                    }
                }
            }
        }
    }


}

class ConversationListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    val head  = itemView.findViewById<ImageView>(R.id.item_conversation_list_head)
    val name  = itemView.findViewById<TextView>(R.id.item_conversation_list_name)
    val content  = itemView.findViewById<TextView>(R.id.item_conversation_list_content)
    val count  = itemView.findViewById<FrameLayout>(R.id.item_conversation_list_count)
    val number  = itemView.findViewById<TextView>(R.id.item_conversation_list_count_number)
    val time  = itemView.findViewById<TextView>(R.id.item_conversation_list_time)


    companion object{
        fun newInstance(parent: ViewGroup):ConversationListViewHolder{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation_list,parent,false)
            return ConversationListViewHolder(view)
        }
    }
    fun bindWithItem(imConversationList: ImConversationList){
        with(itemView){
            name.text = imConversationList.groupName
            if (imConversationList.lastMessageType == 1){
                content.text = imConversationList.lastMessage
            }else if (imConversationList.lastMessageType == 2) {
                content.text = "[图片]"
            }else {
                content.text = ""
            }
            if (imConversationList.numberOfUnreadMessages == 0){
                count.visibility = View.GONE
            }else {
                count.visibility = View.VISIBLE
                if (imConversationList.numberOfUnreadMessages!! > 99){
                    number.text  = "99+"
                }else {
                    number.text  = "${imConversationList.numberOfUnreadMessages}"
                }
            }

            if (imConversationList.lastMsgTime != null){
                time.text = DateFormatUtil.getData(imConversationList.lastMsgTime)
            }else {
                time.text = DateFormatUtil.getData(imConversationList.gmtCreate)
            }

        }
    }
}

class ConversationListFooterViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    val layout = itemView.findViewById<LinearLayout>(R.id.footer_text_layout)
    val textView = itemView.findViewById<TextView>(R.id.footer_text_message)
    val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar_footer_message)
    companion object{
        fun newInstance(parent: ViewGroup): ConversationListFooterViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_footer_message,parent,false)
            return ConversationListFooterViewHolder(view)
        }
    }
    fun bindWithNetWorkStatus(netWorkStatus: ImConversationListNetWorkStatus?){


        with(itemView){

            when(netWorkStatus){
                ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_FAILED -> {
                    layout.visibility = View.VISIBLE
                    textView.text = "点击重试"
                    progressBar.visibility = View.GONE
                    isClickable = true
                }
                ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_COMPLETED -> {
                    layout.visibility = View.GONE
                    textView.text = "全部加载完成"
                    progressBar.visibility = View.GONE
                    isClickable = false
                }
                ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADING->{
                    layout.visibility = View.GONE
                    textView.text = "正在加载"
                    progressBar.visibility = View.GONE
                    isClickable = false
                }
                ImConversationListNetWorkStatus.IM_CONVERSATION_LIST_INITIAL_LOADED->{
                    layout.visibility = View.GONE
                    textView.text = "加载完成"
                    progressBar.visibility = View.GONE
                    isClickable = false
                }
                else -> {
                    layout.visibility = View.GONE
                    textView.text = "正在加载"
                    progressBar.visibility = View.GONE
                    isClickable = false
//                    layout.visibility = View.VISIBLE
//                    textView.text = "正在加载"
//                    progressBar.visibility = View.VISIBLE
//                    isClickable = false
                }
            }
        }
    }


}