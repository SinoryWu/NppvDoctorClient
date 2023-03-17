package com.hzdq.nppvdoctorclient.util

import android.content.Context
import android.content.Intent
import com.hzdq.nppvdoctorclient.TokenDialog
import com.hzdq.nppvdoctorclient.login.LoginActivity

class TokenDialogUtil (val context: Context){
    private var tokenDialog: TokenDialog? = null
    val shp = Shp(context)
    fun showTokenDialog(){
        if (tokenDialog == null){
            tokenDialog = TokenDialog(context, object : TokenDialog.ConfirmAction {
                override fun onRightClick() {
                    shp.saveToSp("token", "")
                    shp.saveToSp("uid", "")

                    context.startActivity(
                        Intent(context,
                            LoginActivity::class.java)
                    )
                    ActivityCollector.finishAll()
                }

            })
            tokenDialog!!.show()
            tokenDialog?.setCanceledOnTouchOutside(false)
        }else {
            tokenDialog!!.show()
            tokenDialog?.setCanceledOnTouchOutside(false)
        }



    }

    fun disMissTokenDialog(){
        tokenDialog?.dismiss()
    }


}