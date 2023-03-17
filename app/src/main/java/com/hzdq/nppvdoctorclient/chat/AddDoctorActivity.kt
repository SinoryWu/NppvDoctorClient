package com.hzdq.nppvdoctorclient.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.adapter.PersonAdapter
import com.hzdq.nppvdoctorclient.bean.Person
import com.hzdq.nppvdoctorclient.databinding.ActivityAddDoctorBinding
import com.hzdq.nppvdoctorclient.util.AnimationUtil
import com.hzdq.nppvdoctorclient.util.PinYinUtils
import com.hzdq.nppvdoctorclient.view.SideBar
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
*@desc 添加医生/医助
*@Author Sinory
*@date 2023/3/16 12:46
*/
class AddDoctorActivity : AppCompatActivity() {

    /**
     * 联系人集合
     */
    private var persons: ArrayList<Person>? = null
    private lateinit var binding:ActivityAddDoctorBinding
    private val handler: Handler = Handler() //用于隐藏切换后的字母,在主线程中运行
    private var personAdapter :PersonAdapter? = null
    private var linearLayoutManager:LinearLayoutManager? = null
    private var pinYinUtils: PinYinUtils? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_doctor)

        pinYinUtils = PinYinUtils()

        initView()

        binding.sideBar.setOnIndexChangeListener(MyOnIndexChangeListener())

        linearLayoutManager = LinearLayoutManager(this)

        personAdapter = PersonAdapter()
        binding.recyclerView.apply {
            adapter = personAdapter
            layoutManager = linearLayoutManager
        }

        initData()
    }

    private fun initView(){
        binding.head.content.text = "新增人员"
        binding.head.back.setOnClickListener {
            finish()
        }


    }

    inner class MyOnIndexChangeListener :SideBar.OnIndexChangeListener{
        override fun onIndexChange(word: String) {
            updateWord(word)
            updateRecyclerView(word);//A～Z字母

        }

    }

    /**
     * 初始化数据
     */
    private fun initData() {
        persons = ArrayList()
        persons!!.add(Person("张小光","",0)) //将人名添加到集合中
        persons!!.add(Person("杨大雷","",0))
        persons!!.add(Person("胡继开","",0))
        persons!!.add(Person("刘三","",0))
        persons!!.add(Person("钟兴","",0))
        persons!!.add(Person("尹顺","",0))
        persons!!.add(Person("安杰","",0))
        persons!!.add(Person("张骞","",0))
        persons!!.add(Person("温小松","",0))
        persons!!.add(Person("李凤","",0))
        persons!!.add(Person("杜甫","",0))
        persons!!.add(Person("娄志超","",0))
        persons!!.add(Person("张飞","",0))
        persons!!.add(Person("王杰","",0))
        persons!!.add(Person("李三","",0))
        persons!!.add(Person("孙二娘","",0))
        persons!!.add(Person("唐小雷","",0))
        persons!!.add(Person("牛二","",0))
        persons!!.add(Person("姜光刃","",0))
        persons!!.add(Person("刘能","",0))
        persons!!.add(Person("张四","",0))
        persons!!.add(Person("张五","",0))
        persons!!.add(Person("侯大帅","",0))
        persons!!.add(Person("刘洪","",0))
        persons!!.add(Person("乔三","",0))
        persons!!.add(Person("徐达健","",0))
        persons!!.add(Person("吴洪亮","",0))
        persons!!.add(Person("王兆雷","",0))
        persons!!.add(Person("阿四","",0))
        persons!!.add(Person("李洪磊","",0))

        for (i in 0 until persons!!.size){
            persons!![i].pinyin = pinYinUtils!!.getPinYin(persons!![i].name)
            persons!![i].position = i
        }

        //排序 按照A-Z排序
        persons!!.sortWith(Comparator { o1, o2 ->
            o1!!.pinyin!!.compareTo(o2!!.pinyin!!) //根据拼音排序
        })
        personAdapter?.submitList(persons)

    }

    private fun updateRecyclerView(word:String){

        for (i in 0 until persons!!.size) {
            val listWord = persons!![i].pinyin!!.substring(0, 1) //YANGGUANGFU-(转成)->Y
            if (word.equals(listWord)) {
                //i是ListView中的位置
//                binding.recyclerView.scrollToPosition(i) //定位到recyclerView中的某个位置
                linearLayoutManager?.scrollToPositionWithOffset(i,0)
                return
            }
        }
    }

    /**
     * 显示英文字母
     */
    private fun updateWord(word:String){
        AnimationUtil.fadeIn(binding.word)
        binding.word.text = word
        handler.removeCallbacksAndMessages(null); //先把每次的消息移除
        handler.postDelayed({
            //因为handler在主线程中运行，Runnable方法也是运行在主线程
            //（打日志System.out.println是判断Runnable在哪个线程中运行，出现main证明就是在主线程中运行）
            //                System.out.println(Thread.currentThread().getName() +"-------------------");
//            binding.word.visibility = View.GONE; //1秒后隐藏
               AnimationUtil.fadeOut(binding.word)
        },1000) //1秒后隐藏
    }
}