package com.hzdq.nppvdoctorclient.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val chatCountTotal = MutableLiveData(0)
}