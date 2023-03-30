package com.hzdq.nppvdoctorclient.chat.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.liuzhuang.rcimageview.RoundCornerImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.chat.ChatViewModel
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.DateFormatUtil
import com.hzdq.nppvdoctorclient.util.SizeUtil
import com.hzdq.nppvdoctorclient.util.TimeIntervalUtil
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 *Time:2023/3/22
 *Author:Sinory
 *Description:
 */
class MessageListAdapter2(private val context:Context,val chatViewModel: ChatViewModel):ListAdapter<ImMessageList,MessageListAdapter2.MyViewHolder> (DIFFCALLBACK){
    val urlCollection = URLCollection
    // 预加载回调
    var onPreload: (() -> Unit)? = null
    // 预加载偏移量
    var preloadItemCount = 0
    // 增加预加载状态标记位
    var isPreloading = false

    // 列表滚动状态
    private var scrollState = SCROLL_STATE_IDLE


    private var mClickListener: OnItemClickListener? = null
    //设置回调接口
    interface OnItemClickListener {
        fun onItemClick(messageType:Int,imageUrl:String,view:View,position: Int)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mClickListener = listener
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
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val fromLayout = itemView.findViewById<ConstraintLayout>(R.id.item_chat_from)
        val fromHead  = itemView.findViewById<ImageView>(R.id.item_chat_from_head)
        val fromName = itemView.findViewById<TextView>(R.id.item_chat_from_name)
        val fromType = itemView.findViewById<TextView>(R.id.item_chat_from_type)
        val fromContent = itemView.findViewById<TextView>(R.id.item_chat_from_content)
        val fromShimmer = itemView.findViewById<ShimmerLayout>(R.id.item_chat_from_shimmer)
        val fromPic = itemView.findViewById<RoundCornerImageView>(R.id.item_chat_from_pic)



        val toLayout = itemView.findViewById<ConstraintLayout>(R.id.item_chat_to)
        val toHead  = itemView.findViewById<ImageView>(R.id.item_chat_to_head)
        val toName = itemView.findViewById<TextView>(R.id.item_chat_to_name)
        val toType = itemView.findViewById<TextView>(R.id.item_chat_to_type)
        val toContent = itemView.findViewById<TextView>(R.id.item_chat_to_content)
        val toShimmer = itemView.findViewById<ShimmerLayout>(R.id.item_chat_to_shimmer)
        val toPic = itemView.findViewById<RoundCornerImageView>(R.id.item_chat_to_pic)

        val time = itemView.findViewById<TextView>(R.id.item_chat_time)
        val layout = itemView.findViewById<ConstraintLayout>(R.id.item_chat_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat,parent,false)
        val holder = MyViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        checkPreload(holder.absoluteAdapterPosition)
        val imMessageList = getItem(holder.absoluteAdapterPosition)


        if (holder.absoluteAdapterPosition == 0){
            holder.layout.setPadding(0,SizeUtil.dip2px(context,10f),0,SizeUtil.dip2px(context,10f))
        }

        if (holder.absoluteAdapterPosition != chatViewModel.messageList.value?.size!!-1){
            if (TimeIntervalUtil.isTimeDifferenceGreaterThan(imMessageList.gmtCreate,getItem(holder.absoluteAdapterPosition+1).gmtCreate,15)){
                holder.time.visibility = View.VISIBLE
                holder.time.text = DateFormatUtil.likeWeChatTime(imMessageList.gmtCreate)
            }else {
                holder.time.visibility = View.GONE
                holder.time.text = ""
            }
        }

        when(imMessageList.oneself){
            true -> {

                holder.fromLayout.visibility = View.GONE
//                holder.fromLayout.visibility = View.INVISIBLE
                holder.toLayout.visibility = View.VISIBLE
                holder.toName.text = imMessageList.fromUser?.userName
                holder.toPic.setOnClickListener {
                    imMessageList.messageType?.let { it1 -> imMessageList.message?.let { it2 ->
                        mClickListener?.onItemClick(it1,
                            it2,holder.toPic,holder.absoluteAdapterPosition
                        )
                    } }


                }
                if (imMessageList.messageType == 1){
                    holder.toContent.visibility = View.VISIBLE
                    holder.toShimmer.visibility = View.GONE
//                    holder.toShimmer.visibility = View.INVISIBLE
                    holder.toPic.visibility = View.GONE
//                    holder.toPic.visibility = View.INVISIBLE
                    holder.toContent.text = imMessageList.message
                    Log.d("adapter2", "onBindViewHolder:${holder.absoluteAdapterPosition} ${imMessageList.id} ")
                }else {

                    holder.toContent.visibility = View.GONE
//                    holder.toContent.visibility = View.INVISIBLE
                    holder.toShimmer.visibility = View.VISIBLE
                    holder.toPic.visibility = View.VISIBLE
                    holder.toShimmer.apply {
                        setShimmerColor(0x55FFFFFF) //设置闪烁颜色
                        setShimmerAngle(0) //设置闪烁角度
                        setShimmerAnimationDuration(600)
                        startShimmerAnimation() //开始闪烁
                    }
                    val params = holder.toPic.layoutParams as FrameLayout.LayoutParams
                    Glide.with(holder.itemView)
                        .asBitmap()
                        .load(imMessageList.message+urlCollection.COMPRESS_PICTURES)
                        .placeholder(R.drawable.pic_initial_bg)
                        .error(R.drawable.pic_initial_bg)//加载异常显示的图片
                        .fallback(R.drawable.pic_initial_bg) //url为空的时候,显示的图片
                        .listener(object :RequestListener<Bitmap>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false.also {
                                    val height = resource?.height?.toDouble()
                                    val width = resource?.width?.toDouble()
                                    val proportion = (width!! / height!!)
                                    params.width = ((params.height * proportion).toInt())
                                    holder.toShimmer.layoutParams = params
                                    holder.toShimmer.stopShimmerAnimation()
                                    holder.toPic.layoutParams = params

                                }
                            }

                        }).into(holder.toPic)
//                    holder.toContent.text = "[图片]"
                }

                when (imMessageList.fromUser?.userType){
                    1 -> {
                        holder.toHead.setImageResource(R.mipmap.chat_doctor_icon)
                        holder.toType.text = "医生"
                        val params = holder.toName.layoutParams as ConstraintLayout.LayoutParams

                        params.marginEnd = SizeUtil.dip2px(context,8f)
                        holder.toName.layoutParams = params
                    }
                    2 -> {
                        holder.toHead.setImageResource(R.mipmap.chat_bajie_icon)
                        holder.toType.text = "医助"
                        val params = holder.toName.layoutParams as ConstraintLayout.LayoutParams

                        params.marginEnd = SizeUtil.dip2px(context,8f)
                        holder.toName.layoutParams = params
                    }
                    else -> {
                        holder.toHead.setImageResource(R.mipmap.chat_patient_icon)
                        holder.toType.text = ""
                        val params = holder.toName.layoutParams as ConstraintLayout.LayoutParams

                        params.marginEnd = SizeUtil.dip2px(context,0f)
                        holder.toName.layoutParams = params
                    }
                }
            }
            else -> {
                holder.fromLayout.visibility = View.VISIBLE
                holder.toLayout.visibility = View.GONE
//                holder.toLayout.visibility = View.INVISIBLE
                holder.fromName.text = imMessageList.fromUser?.userName

                holder.fromPic.setOnClickListener {
                    imMessageList.messageType?.let { it1 -> imMessageList.message?.let { it2 ->
                        mClickListener?.onItemClick(it1,
                            it2,holder.fromPic,holder.absoluteAdapterPosition
                        )
                    } }


                }

                if (imMessageList.messageType == 1){
                    holder.fromShimmer.visibility = View.GONE
//                    holder.fromShimmer.visibility = View.INVISIBLE
                    holder.fromPic.visibility = View.GONE
//                    holder.fromPic.visibility = View.INVISIBLE
                    holder.fromContent.visibility = View.VISIBLE
                    holder.fromContent.text = imMessageList.message

                }else {
                    holder.fromContent.visibility = View.GONE
//                    holder.fromContent.visibility = View.INVISIBLE
                    holder.fromShimmer.visibility = View.VISIBLE
                    holder.fromPic.visibility = View.VISIBLE
                    holder.fromShimmer.apply {
                        setShimmerColor(0x55FFFFFF) //设置闪烁颜色
                        setShimmerAngle(0) //设置闪烁角度
                        setShimmerAnimationDuration(600)
                        startShimmerAnimation() //开始闪烁
                    }
                    val params = holder.fromPic.layoutParams as FrameLayout.LayoutParams
                    Glide.with(holder.itemView)
                        .asBitmap()
                        .load(imMessageList.message+urlCollection.COMPRESS_PICTURES)
                        .fallback(R.drawable.pic_initial_bg) //url为空的时候,显示的图片
                        .error(R.drawable.pic_initial_bg)//加载异常显示的图片
                        .placeholder(R.drawable.pic_initial_bg)
                        .listener(object :RequestListener<Bitmap>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false.also {
                                    val height = resource?.height?.toDouble()
                                    val width = resource?.width?.toDouble()
                                    val proportion = (width!! / height!!)
                                    params.width = ((params.height * proportion).toInt())
                                    holder.fromShimmer.layoutParams = params
                                    holder.fromShimmer.stopShimmerAnimation()
                                    holder.fromPic.layoutParams = params

                                }
                            }

                        }).into(holder.fromPic)

//
                }

                when (imMessageList.fromUser?.userType){
                    1 -> {
                        holder.fromHead.setImageResource(R.mipmap.chat_doctor_icon)
                        holder.fromType.text = "医生"
                    }
                    2 -> {
                        holder.fromHead.setImageResource(R.mipmap.chat_bajie_icon)
                        holder.fromType.text = "医助"
                    }
                    else -> {
                        holder.fromHead.setImageResource(R.mipmap.chat_doctor_icon)
                        holder.fromType.text = ""
                    }
                }
            }

        }
    }

//    //局部刷新
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
//
//        if (payloads.isEmpty()){
//            super.onBindViewHolder(holder, position, payloads)
//            return
//        }else {
//
//        }
//    }
    // 判断是否进行预加载
    private fun checkPreload(position: Int) {
    if (onPreload != null
        && position == max(itemCount - 1 - preloadItemCount, 0)// 索引值等于阈值
        && scrollState != SCROLL_STATE_IDLE // 列表正在滚动
        && !isPreloading // 预加载不在进行中
    ) {
        isPreloading = true // 表示正在执行预加载

        onPreload?.invoke()
    }
//       if (chatViewModel.messageCount == 1){
//           if (onPreload != null
//               && position == max(itemCount - 1 - preloadItemCount, 0)// 索引值等于阈值
//               && scrollState != SCROLL_STATE_IDLE // 列表正在滚动
//               && !isPreloading // 预加载不在进行中
//           ) {
//               isPreloading = true // 表示正在执行预加载
//
//               onPreload?.invoke()
//           }
//       }else {
//           if (onPreload != null
//               && position == max(itemCount - 1 - chatViewModel.preloadItemCount.value!!, 0)// 索引值等于阈值
//               && scrollState != SCROLL_STATE_IDLE // 列表正在滚动
//               && !isPreloading // 预加载不在进行中
//           ) {
//               isPreloading = true // 表示正在执行预加载
//
//               onPreload?.invoke()
//           }
//       }

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

}