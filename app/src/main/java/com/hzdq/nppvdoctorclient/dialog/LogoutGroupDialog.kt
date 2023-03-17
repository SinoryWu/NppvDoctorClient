package com.hzdq.nppvdoctorclient.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.hzdq.nppvdoctorclient.R

/**
 *Time:2023/3/16
 *Author:Sinory
 *Description:退出群聊dialog
 */
class LogoutGroupDialog(context: Context, themeResId:Int): Dialog(context,themeResId) {
    interface ConfirmAction {

        fun onRightClick()
    }

    interface CancelAction{
        fun onLeftClick()
    }

    private var confirmAction: ConfirmAction? = null
    private var cancelAction: CancelAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_log_out_group)
        val confirm  = findViewById<Button>(R.id.dialog_log_out_confirm)
        val cancel  = findViewById<Button>(R.id.dialog_log_out_cancel)

        confirm.setOnClickListener {
            confirmAction?.onRightClick()
            dismiss()

        }

        cancel.setOnClickListener {
            cancelAction?.onLeftClick()
            dismiss()
        }
    }
}