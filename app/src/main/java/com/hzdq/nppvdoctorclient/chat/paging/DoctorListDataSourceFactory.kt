package com.hzdq.nppvdoctorclient.chat.paging

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.hzdq.nppvdoctorclient.dataclass.DoctorList
import com.hzdq.nppvdoctorclient.dataclass.ImConversationList

/**
 *Time:2023/3/27
 *Author:Sinory
 *Description:
 */
class DoctorListDataSourceFactory (private val context: Context):DataSource.Factory<Int, DoctorList>(){
    private var _doctorListDataSource = MutableLiveData<DoctorListDataSource>()
    val doctorListDataSource : LiveData<DoctorListDataSource> = _doctorListDataSource
    override fun create(): DataSource<Int, DoctorList> {
        return DoctorListDataSource(context).also {
            _doctorListDataSource.postValue(it)
        }
    }
}