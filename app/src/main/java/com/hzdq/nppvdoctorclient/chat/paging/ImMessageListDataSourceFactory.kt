package com.hzdq.nppvdoctorclient.chat.paging

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */
class ImMessageListDataSourceFactory(private val context: Context):DataSource.Factory<Int,ImMessageList>() {
    val imMessageListDataSource = MutableLiveData<ImMessageListDataSource>()

    override fun create(): DataSource<Int, ImMessageList> {
        return ImMessageListDataSource(context).also {
            imMessageListDataSource.postValue(it)
        }
    }

}