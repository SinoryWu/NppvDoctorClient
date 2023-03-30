package com.hzdq.nppvdoctorclient.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.bean.Person

/**
 *Time:2023/3/16
 *Author:Sinory
 *Description:
 */
class PersonAdapter:ListAdapter<Person, PersonAdapter.MyViewHolder>(DIFFCALLBACK) {


    object DIFFCALLBACK: DiffUtil.ItemCallback<Person>() {
        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            //判断两个item是否相同这里是比较对象是否为同一个对象  ===表示判断是否是同一个对象 ==比较的是内容
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            //判断两个item内容是否相同
            return oldItem.position == newItem.position
        }

    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.item_add_doctor_name)
        val wordLayout = itemView.findViewById<FrameLayout>(R.id.item_add_doctor_word_layout)
        val word = itemView.findViewById<TextView>(R.id.item_add_doctor_word)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_doctor, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dataItem = getItem(position)
        holder.name.text = dataItem.name
        val word = dataItem.pinyin!!.substring(0,1)
        holder.word.text = word


        if (position == 0){  //若每种信息字母的第一行显示
            holder.wordLayout.setVisibility(View.VISIBLE)
        }else {
            //得到前一个位置对应的字母，如果当前的字母和上一个相同，隐藏TextView；否则就显示
            val preWord = getItem(position - 1).pinyin!!.substring(0, 1) //得到上一个字母A~Z
            if (word.equals(preWord)){  //若word和preWord相同
                holder.wordLayout.setVisibility(View.GONE) //隐藏
            }else {
                holder.wordLayout.setVisibility(View.VISIBLE) //显示
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}