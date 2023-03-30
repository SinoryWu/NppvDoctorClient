package com.hzdq.nppvdoctorclient.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
class GroupMemberAdapter(val userName:String):ListAdapter<DataGroupMember,GroupMemberAdapter.MyViewHolder>(DIFFCALLBACK) {
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_group_member,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(holder.absoluteAdapterPosition)
        holder.name.text = data.name
        holder.type.text = when(data.userType){
            1 -> "医生"
            2 -> "医助"
            else -> ""
        }
        if (userName.equals(data.name)){
            holder.arrow.visibility = View.GONE
        }else {
            holder.arrow.visibility = View.VISIBLE
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