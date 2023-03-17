package com.hzdq.nppvdoctorclient

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView

class TokenDialog(context: Context, confirmAction: ConfirmAction): Dialog(context, R.style.CustomDialog) {
    private var confirmAction: ConfirmAction? = null
    interface ConfirmAction {
        fun onRightClick()
    }
    init {
        this.confirmAction = confirmAction
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_token)
        val confirm = findViewById<TextView>(R.id.token_confirm)
        confirm.setOnClickListener {
            confirmAction?.onRightClick()

        }
    }
}