package com.hzdq.nppvdoctorclient.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.dataclass.DataGroupMember
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList

/**
 *Time:2023/3/24
 *Author:Sinory
 *Description:
 */
class GroupMemberAdapter(val userName:String,val roleType:Int):ListAdapter<DataGroupMember,GroupMemberAdapter.MyViewHolder>(DIFFCALLBACK) {
    private var mClickListener: OnItemClickListener? = null
    //设置回调接口
    interface OnItemClickListener {
        fun onItemClick(type:Int,id:Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mClickListener = listener
    }
    object DIFFCALLBACK: DiffUtil.ItemCallback<DataGroupMember>() {
        override fun areItemsTheSame(oldItem: DataGroupMember, newItem: DataGroupMember): Boolean {
            //判断两个item是否相同这里是比较对象是否为同一个对象  ===表示判断是否是同一个对象 ==比较的是内容
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: DataGroupMember, newItem: DataGroupMember): Boolean {
            //判断两个item内容是否相同
            return oldItem.uid == newItem.uid
        }

    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val head  = itemView.findViewById<ImageView>(R.id.item_group_head)
        val name  = itemView.findViewById<TextView>(R.id.item_group_name)
        val type  = itemView.findViewById<TextView>(R.id.item_group_type)
        val arrow  = itemView.findViewById<ImageView>(R.id.item_group_arrow)
        val layout  = itemView.findViewById<ConstraintLayout>(R.id.item_group_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_group_member,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(holder.absoluteAdapterPosition)
        holder.name.text = data.name
        when(data.userType){
            1 -> {
                holder.type.text= "医生"
                holder.head.setImageResource(R.mipmap.group_doctor_head_icon)
            }
            2 -> {
                holder.type.text= "医助"
                holder.head.setImageResource(R.mipmap.group_bajie_icon)
            }
            else -> {
                holder.type.text= ""
                holder.head.setImageResource(R.mipmap.group_patient_head_icon)
            }
        }


        if (data.userType == 2){
            holder.arrow.visibility = View.GONE
        }else {
            //如果是医助 可以看到医生患者的详情
            if (roleType == 3){
                holder.arrow.visibility = View.VISIBLE
                if (data.userType == 1){
                    holder.layout.setOnClickListener {
                        data.uid?.let { it1 -> mClickListener?.onItemClick(1, it1) }
                    }
                }else if (data.userType == 3){
                    holder.layout.setOnClickListener {
                        data.uid?.let { it1 -> mClickListener?.onItemClick(3, it1) }
                    }
                }
            }else {
                //如果是医生 只能看到患者详情
                if (data.userType == 3){
                    holder.arrow.visibility = View.VISIBLE
                    holder.layout.setOnClickListener {
                        data.uid?.let { it1 -> mClickListener?.onItemClick(3, it1) }
                    }
                }else if (data.userType == 1){
                    holder.arrow.visibility = View.GONE
                }
            }

        }



//        when(data.userType){
//            1 -> {
//                holder.type.text = "医生"
//                holder.arrow.visibility = View.VISIBLE
//            }
//            2 -> {
//                holder.type.text = "医助"
//                holder.arrow.visibility = View.VISIBLE
//            }
//            else -> {
//                holder.type.text = ""
//                holder.arrow.visibility = View.GONE
//            }
//        }

    }
}