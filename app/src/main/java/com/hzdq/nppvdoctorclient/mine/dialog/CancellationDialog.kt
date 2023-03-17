package com.hzdq.nppvdoctorclient.mine.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.hzdq.nppvdoctorclient.R


class CancellationDialog(context: Context, themeResId:Int): Dialog(context,themeResId)  {
    private var confirmAction: ConfirmAction? = null
    private var cancelAction: CancelAction? = null
    interface ConfirmAction {

        fun onRightClick()

    }

    interface CancelAction {

        fun onLeftClick( view: View)

    }
    fun setCancel(cancelAction: CancelAction):CancellationDialog{
        this.cancelAction = cancelAction
        return this
    }

    fun setConfirm(confirmAction: ConfirmAction): CancellationDialog {
        this.confirmAction = confirmAction
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_cancelled)
        val confirm = findViewById<Button>(R.id.dialog_cancelled_confirm)
        confirm.setOnClickListener {
            confirmAction?.onRightClick()
        }
    }

}