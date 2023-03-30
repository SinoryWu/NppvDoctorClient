package com.hzdq.nppvdoctorclient.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.hzdq.nppvdoctorclient.R

/**
 *Time:2023/3/28
 *Author:Sinory
 *Description:
 */
class ShareDialog(context: Context, themeResId:Int): Dialog(context,themeResId) {
    interface ShareAction {

        fun onShareClick()
    }

    interface SaveAction{
        fun onSaveClick()
    }
    fun setSave(saveAction: SaveAction): ShareDialog {
        this.saveAction = saveAction
        return this
    }

    fun setShare(shareAction: ShareAction): ShareDialog {
        this.shareAction = shareAction
        return this
    }
    private var shareAction: ShareAction? = null
    private var saveAction: SaveAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_share)
        val save = findViewById<Button>(R.id.dialog_share_save)
        val share = findViewById<Button>(R.id.dialog_share_share)
        save.setOnClickListener {
            saveAction?.onSaveClick()
        }
        share.setOnClickListener {
            shareAction?.onShareClick()
        }
    }
}