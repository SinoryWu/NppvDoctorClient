package com.hzdq.nppvdoctorclient.chat

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hzdq.nppvdoctorclient.ChatCommonViewModel
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.body.BodyImMessageList
import com.hzdq.nppvdoctorclient.body.BodyReadAllMsg
import com.hzdq.nppvdoctorclient.body.BodySendMessage
import com.hzdq.nppvdoctorclient.chat.adapter.MessageListAdapter2
import com.hzdq.nppvdoctorclient.chat.dialog.ShareDialog
import com.hzdq.nppvdoctorclient.databinding.ActivityChatBinding
import com.hzdq.nppvdoctorclient.dataclass.FromUser
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.util.*
import com.hzdq.nppvdoctorclient.util.ViewClickDelay.clickDelay
import com.hzdq.viewmodelshare.shareViewModels
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.style.TitleBarStyle
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.ToastUtils
import com.permissionx.guolindev.PermissionX
import com.wanglu.photoviewerlibrary.OnLongClickListener
import com.wanglu.photoviewerlibrary.PhotoViewer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


/**
*@desc 聊天界面
*@Author Sinory
*@date 2023/3/16 12:46
*/
class ChatActivity : AppCompatActivity() {
    private var tokenDialogUtil: TokenDialogUtil? = null
    private lateinit var binding:ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private val TAG = "ChatActivity"
    private lateinit var shp: Shp
    private var messageListAdapter2: MessageListAdapter2? = null
    private var linearLayoutManager:LinearLayoutManager? = null
    private var bodySendMessage:BodySendMessage? = null
    private var imMessageList:ImMessageList? = null
    private var fromUser:FromUser? = null
    private var bodyImMessageList:BodyImMessageList? = null
    private var shareDialog:ShareDialog? = null

    private var file:File? = null
    private var  uiStyle : PictureSelectorStyle? = null
    private var pictureSelector: PictureSelector? = null
    private var bodyReadAllMsg:BodyReadAllMsg? = null
    var editHeight = 0
    private val vm: ChatCommonViewModel by shareViewModels("sinory")

    private val GROUP_DETAIL_REQUEST_CODE = 0x000019

    override fun onStop() {
        Log.d(TAG, "onStop: ")
        super.onStop()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        shareDialog?.dismiss()
        DataCleanManagerKotlin.cleanInternalCache(this)
        vm.groupThirdPartyId.value = ""
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        chatViewModel.groupId.value = intent.getIntExtra("groupId",0)
        Log.d(TAG, "onCreate: ${intent.getStringExtra("groupThirdPartyId")}")
        vm.groupThirdPartyId.value = intent.getStringExtra("groupThirdPartyId")
        shp = Shp(this)
        ActivityCollector.addActivity(this)
        tokenDialogUtil = TokenDialogUtil(this)


        pictureSelector = PictureSelector.create(this)
        weChatStyle()


        bodyReadAllMsg = BodyReadAllMsg(chatViewModel.groupId.value)
        chatViewModel.readAllMsg(bodyReadAllMsg!!)
        initView()


        binding.sendMsg.setOnClickListener {
            vm.receiverList.value!!.clear()
            vm.receiverCount.value = 0
            if (binding.edit.text.toString().equals("")){
                ToastUtil.showToast(this,"聊天内容不能为空")
            }else {
                //发送消息初始化
                bodySendMessage = BodySendMessage(2,"",0,chatViewModel.groupId.value)
                bodySendMessage?.messageType = 1
                bodySendMessage?.message = binding.edit.text.toString()
                fromUser = FromUser(0,0,shp.getUserName(),"",0)
                if (shp.getRoleType() == 2){
                    fromUser?.userType = 1
                }else {
                    fromUser?.userType = 2
                }
                imMessageList = ImMessageList(0,2,fromUser,"","",0,"",true)
                imMessageList?.gmtCreate = DateUtil.stamp2Date(System.currentTimeMillis())
                imMessageList?.message = binding.edit.text.toString()
                if (chatViewModel.messageList.value!!.size > 0){
                    imMessageList?.id = chatViewModel.messageList.value!![0].id!! + 1
                    vm.listId.value = chatViewModel.messageList.value!![0].id!! + 1
                }else {
                    imMessageList?.id = 1
                    vm.listId.value = 1
                }
                imMessageList?.messageType = 1
                chatViewModel.messageList.value!!.add(0,imMessageList!!)

                chatViewModel.sendMessage(1,bodySendMessage!!)
            }
        }

        chatViewModel.readMsgCode.observe(this, Observer {
            when(it){
                0 -> {}
                1 -> {
                    bodyReadAllMsg = null
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this,chatViewModel.readMsgMsg.value)
                }
            }
        })

        observer()

        binding.sendImg.setOnClickListener {
//            messageListAdapter2!!.notifyItemInserted(0)
            permission()
//            chatViewModel.fileCode.value = 1
//            chatViewModel.picAddress.value = "https://bajie-sleep.oss-cn-hangzhou.aliyuncs.com/file/1679561280623.jpg"



        }
    }


    private fun observer(){
        chatViewModel.sendMessageCode.observe(this, Observer {
            when(it){
                0->{}
                1->{

                    binding.recyclerView.setItemAnimator(DefaultItemAnimator())
                    Log.d(TAG, "insert:这里添加3")
                    Log.d(TAG, "insert:这里添加3 ${chatViewModel.messageList.value!![0]}")

                    messageListAdapter2!!.notifyItemInserted(0)
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)

                    binding.edit.setText("")
                    fromUser = null
                    imMessageList = null
                    bodySendMessage = null

                    chatViewModel.sendMessageCode.value = 0
                }
                20 -> {
                    binding.recyclerView.setItemAnimator(DefaultItemAnimator())
                    Log.d(TAG, "insert:这里添加2 ")
                    messageListAdapter2!!.notifyItemInserted(0)
                    messageListAdapter2!!.notifyItemChanged(0)
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)

                    fromUser = null
                    imMessageList = null
                    bodySendMessage = null

                    Log.d(TAG, "上传文件1 messagePicIndex:${chatViewModel.messagePicIndex.value!!} ")
                    Log.d(TAG, "上传文件1 messagePicList.SIZE:${chatViewModel.messagePicList.value!!.size-1} ")
                    if (chatViewModel.messagePicIndex.value!! > -1){
                        if (chatViewModel.messagePicIndex.value!! < chatViewModel.messagePicList.value!!.size-1){


                            chatViewModel.messagePicIndex.value = chatViewModel.messagePicIndex.value!!+1
                            Log.d(TAG, "上传文件:1 ")
                            uploadFile()
                        }else {
                            chatViewModel.messagePicList.value!!.clear()
                            chatViewModel.messagePicIndex.value = -1
                        }
                    }


                    chatViewModel.sendMessageCode.value = 0
                }
                11->{
                    tokenDialogUtil?.showTokenDialog()
                }
                else->{
                    ToastUtil.showToast(this,chatViewModel.sendMessageMsg.value)
                }
            }
        })




        chatViewModel.fileCode.observe(this, Observer {
            when(it){
                0 -> {

                }
                1 -> {
                    file = null
//                    chatViewModel.picAddress.value = "https://bajie-sleep.oss-cn-hangzhou.aliyuncs.com/file/1679561502893.jpg"
                    //发送图片初始化
                    bodySendMessage = BodySendMessage(2,"",0,chatViewModel.groupId.value)
                    bodySendMessage?.messageType = 2
                    bodySendMessage?.message = chatViewModel.picAddress.value
                    fromUser = FromUser(0,0,shp.getUserName(),"",0)
                    if (shp.getRoleType() == 2){
                        fromUser?.userType = 1
                    }else {
                        fromUser?.userType = 2
                    }
                    imMessageList = ImMessageList(0,2,fromUser,"","",0,"",true)
                    imMessageList?.gmtCreate = DateUtil.stamp2Date(System.currentTimeMillis())
                    imMessageList?.message = chatViewModel.picAddress.value
                    imMessageList?.messageType = 2
                    if (chatViewModel.messageList.value!!.size > 0){
                        imMessageList?.id = chatViewModel.messageList.value!![0].id!! + 1
                        vm.listId.value = chatViewModel.messageList.value!![0].id!! + 1
                    }else {
                        imMessageList?.id = 1
                        vm.listId.value = 1
                    }

                    chatViewModel.imageList.value?.add(0,chatViewModel.picAddress.value!!)

                    chatViewModel.messageList.value?.add(0,imMessageList!!)
                    chatViewModel.sendMessage(2,bodySendMessage!!)



                    chatViewModel.fileCode.value = 0

                }
                11 -> {tokenDialogUtil?.showTokenDialog()}
                else -> {
                    ToastUtil.showToast(this,chatViewModel.fileMsg.value)
                }
            }
        })

        vm.receiverCount.observe(this, Observer {
            Log.d(TAG, "receiverCount:$it ")
            if (it > 0){
                Log.d(TAG, "receiverCount:增加item ")

                chatViewModel.messageList.value?.addAll(0,vm.receiverList.value!!)
                chatViewModel.imageList.value?.addAll(0,vm.imageList.value!!)
                binding.recyclerView.setItemAnimator(DefaultItemAnimator())
                Log.d(TAG, "insert:这里添加1 ")
                messageListAdapter2!!.notifyItemRangeInserted(0,vm.receiverList.value!!.size)
//                messageListAdapter2!!.notifyItemRangeChanged(0,vm.receiverList.value!!.size)
                linearLayoutManager?.scrollToPositionWithOffset(0,0)
                vm.receiverList.value!!.clear()
                vm.imageList.value!!.clear()


                vm.receiverCount.value = 0
            }
        })


    }

    override fun onBackPressed() {
        HideKeyboard.hideKeyboard(binding.root,this)
//        val intent = Intent()
//        if (null != chatViewModel.messageList.value){
//            if (chatViewModel.messageList.value!!.size > 0){
//                intent.putExtra("lastMsgTime",chatViewModel.messageList.value!![0].gmtCreate)
//                intent.putExtra("lastMessage",chatViewModel.messageList.value!![0].message)
//                intent.putExtra("lastMessageType",chatViewModel.messageList.value!![0].messageType)
//            }
//        }
//
//        intent.putExtra("groupId",chatViewModel.groupId.value)
//        setResult(RESULT_OK,intent)
        finish()
    }

    private fun initView(){
        binding.head.content.text = intent.getStringExtra("groupName")

        binding.head.back.setOnClickListener {
            onBackPressed()
        }

        if (intent.getIntExtra("joinState",0) == 0){
            binding.head.more.visibility = View.VISIBLE
            binding.sendLayout.visibility = View.VISIBLE
        }else {
            binding.head.more.visibility = View.GONE
            binding.sendLayout.visibility = View.GONE
        }

        val startActivityGroupDetail = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == 20){
//                if (null != chatViewModel.messageList.value){
//                    if (chatViewModel.messageList.value!!.size > 0){
//                        intent.putExtra("lastMsgTime",chatViewModel.messageList.value!![0].gmtCreate)
//                        intent.putExtra("lastMessage",chatViewModel.messageList.value!![0].message)
//                        intent.putExtra("lastMessageType",chatViewModel.messageList.value!![0].messageType)
//                    }
//                }
//
//                intent.putExtra("groupId",chatViewModel.groupId.value)
                setResult(20)
                finish()
            }
        }

        binding.head.more.clickDelay {
            Log.d(TAG, "more click:${chatViewModel.groupId.value} ")
            val intent = Intent(this,GroupDetailActivity::class.java)
            intent.putExtra("groupId",chatViewModel.groupId.value)

            startActivityGroupDetail.launch(intent)
        }


        bodyImMessageList = BodyImMessageList(0,0,0,null)
        bodyImMessageList?.groupId = chatViewModel.groupId.value
        bodyImMessageList?.index = null
        bodyImMessageList?.pageNum = 1
        bodyImMessageList?.pageSize = 30
        chatViewModel.getMessageList(bodyImMessageList!!)


        linearLayoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = true
        }
        messageListAdapter2 = MessageListAdapter2(this,chatViewModel).apply {
            preloadItemCount = 15
            onPreload = {
                // 预加载业务逻辑
                Log.d(TAG, "预加载:已经到倒数第15个item了去加载下一页 ")
                bodyImMessageList = BodyImMessageList(0,0,0,null)
                bodyImMessageList?.groupId = chatViewModel.groupId.value
                bodyImMessageList?.index = chatViewModel.index.value
                bodyImMessageList?.pageNum = 1
                bodyImMessageList?.pageSize = 50
                chatViewModel.getMessageList(bodyImMessageList!!)
            }
        }

//        messageListAdapter2!!.setHasStableIds(true)


        binding.recyclerView.apply {

            layoutManager = linearLayoutManager
            adapter = messageListAdapter2


        }

        chatViewModel.messageCode.observe(this, Observer {
            when(it){
                0 ->{}
                1 -> {
                    if (chatViewModel.messageList.value!!.size > 0){
                        vm.listId.value = chatViewModel.messageList.value!![0].id
                    }
                    messageListAdapter2?.notifyDataSetChanged()
                    messageListAdapter2?.submitList(chatViewModel.messageList.value)
                    Log.d(TAG, "滚动到底部:2 ")
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
                    bodyImMessageList = null
                }
                20 -> {
                    messageListAdapter2?.isPreloading = false
                    binding.recyclerView.setItemAnimator(null);
                    messageListAdapter2?.notifyItemRangeChanged(chatViewModel.positionStart.value!!,chatViewModel.loaderItemCount.value!!)
//                    messageListAdapter2?.notifyDataSetChanged()
//                    binding.recyclerView.setItemAnimator(DefaultItemAnimator())
                }
                11 -> {
                    tokenDialogUtil?.showTokenDialog()
                }
                else -> {
                    ToastUtil.showToast(this,chatViewModel.messageMsg.value)
                }
            }
        })

        val observer = binding.sendLayout.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 获取View的新高度
                val height = binding.sendLayout.height
                if (height != editHeight){
                    Log.d(TAG, "滚动到底部:3 ")
                    linearLayoutManager?.scrollToPositionWithOffset(0,0)
                    editHeight = height
                }
                Log.d(TAG, "onGlobalLayout:$height")
                // TODO: 处理高度变化

            }
        })




        EPSoftKeyBoardListener.setListener(this,object :EPSoftKeyBoardListener.OnSoftKeyBoardChangeListener{
            override fun keyBoardShow(height: Int) {
                Log.d(TAG, "滚动到底部:4 ")
                linearLayoutManager?.scrollToPositionWithOffset(0,0)
            }

            override fun keyBoardHide(height: Int) {

            }

        })

        binding.recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 当不滚动时处理
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    // 获取当前滚动到的条目位置
                    getPositionAndOffset()
                }

//                chatViewModel.scrollItemPosition.value = linearLayoutManager?.findFirstCompletelyVisibleItemPosition()
//                Log.d(TAG, "scrollItemPosition1: ${chatViewModel.scrollItemPosition.value}")



//                Log.d(TAG, "scrollItemPosition2: ${lastPosition}")
            }
        })


        messageListAdapter2?.setOnItemClickListener(object :MessageListAdapter2.OnItemClickListener{
            override fun onItemClick(messageType:Int,imageUrl: String,view: View,position:Int) {
                var currentPage = 0
                for (i in 0 until chatViewModel.imageList.value!!.size){
                    if (imageUrl.equals(chatViewModel.imageList.value!![i])){
                        currentPage = i
                        break
                    }
                }
                if (messageType == 2){
                    try {
                        var bitmap : Bitmap? = null
                        PhotoViewer
                            .setClickSingleImg(imageUrl,view)
                            .setCurrentPage(currentPage)
                            .setData(chatViewModel.imageList.value!!)
                            .setImgContainer(binding.recyclerView)
                            .setShowImageViewInterface(object :PhotoViewer.ShowImageViewInterface{
                                override fun show(iv: ImageView, url: String) {
                                    if (iv != null){
                                        Glide.with(this@ChatActivity).load(url).into(iv)
                                        iv.drawable
                                    }

                                }

                            })

                            .setOnPhotoViewerDestroyListener {
                                scrollToPosition()

//                        linearLayoutManager?.scrollToPositionWithOffset(chatViewModel.lastPosition.value!!,0)
                            }
                            .setOnLongClickListener(object :OnLongClickListener{
                                override fun onLongClick(view: View) {
                                    Log.d(TAG, "onLongClick: ")
                                    val bitmap = createBitmap(view)
                                    val file = createFilePic(this@ChatActivity, bitmap!!)
                                    if (shareDialog == null){
                                        shareDialog = ShareDialog(this@ChatActivity,R.style.CustomDialog)
                                        shareDialog!!.setSave(object :ShareDialog.SaveAction{
                                            override fun onSaveClick() {
                                                savePhoto(bitmap)
                                                shareDialog!!.dismiss()
                                            }

                                        })
                                        shareDialog!!.setShare(object :ShareDialog.ShareAction{
                                            override fun onShareClick() {
                                                shareFile(file!!,path)
                                                shareDialog!!.dismiss()

                                            }

                                        })
                                        shareDialog!!.show()
                                        shareDialog!!.setOnDismissListener {
                                            shareDialog = null
                                        }
                                    }
                                }

                            }).start(this@ChatActivity)
                    }catch (e:Exception){
                        Log.d(TAG, "PhotoViewer Exception: ")
                    }



                }





            }

        })




    }



    /**
     * 获取view的bitmap
     */
    private fun createBitmap(view: View): Bitmap? {
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache() //启用DrawingCache并创建位图
        val bitmap = Bitmap.createBitmap(view.drawingCache) //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        view.isDrawingCacheEnabled = false //禁用DrawingCahce否则会影响性能
        return bitmap
    }

    /**
     * ====根据 bitmap生成新的file
     */
    var path = ""
    fun createFilePic(context: Context, bitmap: Bitmap): File? {

        //文件夹  这两种文件夹都会随软件卸载而删掉
        val folder = context.cacheDir //在Android>data>包名>的cache目录下，一般存放临时缓存数据
        //File folder = this.getExternalFilesDir("image");//在Android>data>包名>的files的image目录下，一般放一些长时间保存的数据
        if (!folder!!.exists()) {
            folder.mkdir()
        }
        var df: SimpleDateFormat? = null //设置日期格式在android中，创建文件时，文件名中不能包含“：”冒号
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            df = SimpleDateFormat("yyyyMMddHHmmss")
        }
        var filename: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            filename = df!!.format(Date())
        }
        //file图片
        val file = File(folder.absolutePath + File.separator + filename + ".jpg")
        path = folder.absolutePath + File.separator + filename + ".jpg"
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 分享图片
     */
    private fun shareFile(file:File,path:String){
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //给临时权限
        sharingIntent.type = "image/*" //根据文件类型设定type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sharingIntent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    this, packageName + ".fileProvider",
                    file
                )
            )
        } else {

            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        }
        startActivity(Intent.createChooser(sharingIntent, "分享"))
    }

    /**
     * 保存图片
     * 将现有的图片转换为位图保存下来
     * 保存图片是一个耗时操作 需要开启一个协程执行这些操作
     * 先找到viewholder再找到图
     */
    private fun  savePhoto(bitmap: Bitmap){
//        withContext(Dispatchers.IO) {
//            //从viewgroup的0号位置找到recyclerview 再找到viewholder
////            val holder = (binding.viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(binding.viewPager2.currentItem) as PagerPhotoViewHolder
//            //toBitmap里面传递的两个参数是宽和高
////            val bitmap = imageWidth?.let { imageHeight?.let { it1 ->
////                    photoView.drawable.toBitmap(it, it1)
////                }
//        }
        if (Build.VERSION.SDK_INT < 29){
            if (MediaStore.Images.Media.insertImage(this.contentResolver,bitmap,"","") == null){
                //吐司如果不放在主线程里面就会报错
                MainScope().launch {
                    ToastUtils.showToast(this@ChatActivity,"图片保存失败")
                }
            }else {
                MainScope().launch {
                    ToastUtils.showToast(this@ChatActivity,"图片已保存至相册")
                }
            }
        }else {
            //保存图片用的uri
            val saveUri = this.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )?: kotlin.run {
                MainScope().launch {
                    ToastUtils.showToast(this@ChatActivity,"图片保存失败")
                }
                return
            }

            //use在用完之后可以将流自动关闭 用OutputStream写入流
            this.contentResolver.openOutputStream(saveUri).use {
                //bitmap写入流都用compress
                //使用jpg格式压缩率为90
                if(bitmap?.compress(Bitmap.CompressFormat.JPEG,100,it) == true){
                    MainScope().launch {
                        ToastUtils.showToast(this@ChatActivity,"图片已保存至相册")
                    }
                }else{
                    MainScope().launch {
                        ToastUtils.showToast(this@ChatActivity,"图片保存失败")

                    }
                }
            }
        }

//        //保存图片用的uri
//        val saveUri = this.contentResolver.insert(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            ContentValues()
//        )?: kotlin.run {
//            MainScope().launch {
//                ToastUtils.showToast(this@ChatActivity,"图片保存失败")
//            }
//            return
//        }
//
//        //use在用完之后可以将流自动关闭 用OutputStream写入流
//        this.contentResolver.openOutputStream(saveUri).use {
//            //bitmap写入流都用compress
//            //使用jpg格式压缩率为90
//            if(bitmap?.compress(Bitmap.CompressFormat.JPEG,100,it) == true){
//                MainScope().launch {
//                    ToastUtils.showToast(this@ChatActivity,"图片已保存至相册")
//                }
//            }else{
//                MainScope().launch {
//                    ToastUtils.showToast(this@ChatActivity,"图片保存失败")
//
//                }
//            }
//        }
    }

    /**
     * 记录RecyclerView当前位置
     */
    private fun getPositionAndOffset() {
        //获取可视的第一个view
//        Log.d(TAG, "getPositionAndOffset:${lastPosition} ")
        val topView = linearLayoutManager?.getChildAt(0)
        Log.d(TAG, "getPositionAndOffset:${topView != null} ")
        if (topView != null) {
            //获取与该view的顶部的偏移量
//            lastOffset = topView.top
            chatViewModel.lastOffset.value= binding.recyclerView.height - topView.height -topView.top
            //得到该View的数组位置
            chatViewModel.lastPosition.value = linearLayoutManager?.getPosition(topView)!!
//            lastPosition = chatViewModel.scrollItemPosition.value!!

        }
    }

    /**
     * 让RecyclerView滚动到指定位置
     */
    private fun scrollToPosition() {
        if (linearLayoutManager != null && chatViewModel.lastPosition.value!! >= 0) {
            linearLayoutManager?.scrollToPositionWithOffset(
                chatViewModel.lastPosition.value!!,
                chatViewModel.lastOffset.value!!
            )
        }

    }


    /**
     * 微信风格照片选择器
     */
    private fun weChatStyle(){
        uiStyle = PictureSelectorStyle()


        // 主体风格
        val numberSelectMainStyle = SelectMainStyle()
        numberSelectMainStyle.isSelectNumberStyle = true
        numberSelectMainStyle.isPreviewSelectNumberStyle = false
        numberSelectMainStyle.isPreviewDisplaySelectGallery = true
        numberSelectMainStyle.selectBackground = R.drawable.ps_default_num_selector
        numberSelectMainStyle.previewSelectBackground = R.drawable.ps_preview_checkbox_selector
        numberSelectMainStyle.selectNormalBackgroundResources =
            R.drawable.ps_select_complete_normal_bg
        numberSelectMainStyle.selectNormalTextColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_53575e)
        numberSelectMainStyle.selectNormalText = getString(R.string.ps_send)
        numberSelectMainStyle.adapterPreviewGalleryBackgroundResource =
            R.drawable.ps_preview_gallery_bg
        numberSelectMainStyle.adapterPreviewGalleryItemSize =
            DensityUtil.dip2px(applicationContext, 52f)
        numberSelectMainStyle.previewSelectText = getString(R.string.ps_select)
        numberSelectMainStyle.previewSelectTextSize = 14
        numberSelectMainStyle.previewSelectTextColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_white)
        numberSelectMainStyle.previewSelectMarginRight =
            DensityUtil.dip2px(applicationContext, 6f)
        numberSelectMainStyle.selectBackgroundResources = R.drawable.ps_select_complete_bg
        numberSelectMainStyle.selectText = getString(R.string.ps_send_num)
        numberSelectMainStyle.selectTextColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_white)
        numberSelectMainStyle.mainListBackgroundColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_black)
        numberSelectMainStyle.isCompleteSelectRelativeTop = true
        numberSelectMainStyle.isPreviewSelectRelativeBottom = true
        numberSelectMainStyle.isAdapterItemIncludeEdge = false

        // 头部TitleBar 风格

        // 头部TitleBar 风格
        val numberTitleBarStyle = TitleBarStyle()
        numberTitleBarStyle.isHideCancelButton = true
        numberTitleBarStyle.isAlbumTitleRelativeLeft = true

//        numberTitleBarStyle.titleAlbumBackgroundResource = R.drawable.ps_demo_only_album_bg

        // 底部NavBar 风格

        // 底部NavBar 风格
        val numberBottomNavBarStyle = BottomNavBarStyle()
        numberBottomNavBarStyle.bottomPreviewNarBarBackgroundColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_half_grey)
        numberBottomNavBarStyle.bottomPreviewNormalText = getString(R.string.ps_preview)
        numberBottomNavBarStyle.bottomPreviewNormalTextColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_9b)
        numberBottomNavBarStyle.bottomPreviewNormalTextSize = 16
        numberBottomNavBarStyle.isCompleteCountTips = false
        numberBottomNavBarStyle.bottomPreviewSelectText = getString(R.string.ps_preview_num)
        numberBottomNavBarStyle.bottomPreviewSelectTextColor =
            ContextCompat.getColor(applicationContext, R.color.ps_color_white)


        uiStyle?.setTitleBarStyle(numberTitleBarStyle)
        uiStyle?.setBottomBarStyle(numberBottomNavBarStyle)
        uiStyle?.setSelectMainStyle(numberSelectMainStyle)

    }

    /**
     * 打开相机
     */
    private fun camera(){
        pictureSelector!!
            .openCamera(SelectMimeType.ofImage())
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {}
                override fun onCancel() {}
            })
    }

    /**
     * 打开相册
     */
    private fun album(){
        pictureSelector!!
            .openGallery(SelectMimeType.ofImage())
            .setSelectorUIStyle(uiStyle)
            .setImageEngine(GlideEngine.createGlideEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    Log.d(TAG, "PictureSelector onResult:${result!![0]!!.availablePath} ")


                    try {

                        for (i in 0 until result.size){
                            val uri = Uri.parse(result[i]!!.availablePath)
                            val path = UriToPathUtils.getRealPathFromUri(this@ChatActivity,uri)
                            chatViewModel.messagePicList.value?.add(path)
                        }
                        chatViewModel.messagePicIndex.value = 0
                        Log.d(TAG, "上传文件:2 ")
                        uploadFile()
                    }catch (e:Exception){
                        Log.d(TAG, "PictureSelector Exception:$e ")
                    }



                }
                override fun onCancel() {

                }
            })
    }

    /**
     * 上传文件
     */
    fun uploadFile(){

        file = File(chatViewModel.messagePicList.value!![chatViewModel.messagePicIndex.value!!])
        val requestFile =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file?.name, requestFile)

        chatViewModel.uploadFile(body)
    }

    fun permission(){
        PermissionX.init(this).permissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).request{allGrand,_,_ ->
            if (allGrand){
                Log.d(TAG, "permission: true")
                album()
            }else{
               ToastUtil.showToast(this,"未打开相应权限")
            }

        }





    }

    //path转uri
    fun getImageStreamFromExternal(imageName: String?): Uri? {
        val externalPubPath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val picPath = File(externalPubPath, imageName)
        var uri: Uri? = null
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath)
        }
        return uri
    }



}