package com.hzdq.nppvdoctorclient.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.mine.dialog.CancellationDialog

/**
 *Time:2023/3/16
 *Author:Sinory
 *Description:退出群聊dialog
 */
class ExitGroupDialog(context: Context, themeResId:Int): Dialog(context,themeResId) {
    interface ConfirmAction {

        fun onRightClick()
    }

    interface CancelAction{
        fun onLeftClick()
    }
    fun setCancel(cancelAction: CancelAction): ExitGroupDialog {
        this.cancelAction = cancelAction
        return this
    }

    fun setConfirm(confirmAction: ConfirmAction): ExitGroupDialog {
        this.confirmAction = confirmAction
        return this
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

        }

        cancel.setOnClickListener {
            cancelAction?.onLeftClick()
            dismiss()
        }
    }
}