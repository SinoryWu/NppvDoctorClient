package com.hzdq.nppvdoctorclient.chat.paging

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.hzdq.nppvdoctorclient.dataclass.ImConversationList

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */
class ImConversationListDataSourceFactory(private val context: Context):DataSource.Factory<Int, ImConversationList>() {
    private var _imConversationListDataSource = MutableLiveData<ImConversationListDataSource>()
    val imConversationListDataSource : LiveData<ImConversationListDataSource> = _imConversationListDataSource
    override fun create(): DataSource<Int, ImConversationList> {
        return ImConversationListDataSource(context).also {
            _imConversationListDataSource.postValue(it)
        }
    }
}