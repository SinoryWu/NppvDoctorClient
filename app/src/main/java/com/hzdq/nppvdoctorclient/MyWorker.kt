package com.hzdq.nppvdoctorclient

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hzdq.viewmodelshare.shareViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    val TAG = "MyWorker"
    private var dowork = false
    override fun doWork(): Result {


        Log.d(TAG, "doWork ")
        return Result.success()


//        Result.success()：工作成功完成。
//        Result.failure()：工作失败。
//        Result.retry()：工作失败，应根据其重试政策在其他时间尝试。
    }
}