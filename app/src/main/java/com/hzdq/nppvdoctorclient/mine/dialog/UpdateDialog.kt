package com.hzdq.nppvdoctorclient.mine.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.hzdq.nppvdoctorclient.R

class UpdateDialog(val version:String,context: Context, themeResId:Int): Dialog(context,themeResId)  {

    private var confirmAction: ConfirmAction? = null
    private var cancelAction: CancelAction? = null
    interface ConfirmAction {

        fun onRightClick()

    }

    interface CancelAction {

        fun onLeftClick( view: View)

    }
    fun setCancel(cancelAction: CancelAction):UpdateDialog{
        this.cancelAction = cancelAction
        return this
    }

    fun setConfirm(confirmAction: ConfirmAction): UpdateDialog {
        this.confirmAction = confirmAction
        return this
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update)
        val content = findViewById<TextView>(R.id.dialog_update_content)
        content.text = "有新版本发布，您当前版本过低\n请升级至v${version}使用"
        val confirm = findViewById<Button>(R.id.dialog_update_confirm)
        confirm.setOnClickListener {
            confirmAction?.onRightClick()
        }
    }
}