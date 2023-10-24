package com.hzdq.nppvdoctorclient.mine.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.util.ToastUtil

/**
 * <pre>
 *     author : Sinory
 *     e-mail : 249668399@qq.com
 *     time   : 2022/05/19
 *     desc   : Android Developer
 *     tel    : 15355090637
 * </pre>
 */
class SafeDialog (context: Context, themeResId:Int):Dialog(context,themeResId){

    private var confirmAction: ConfirmAction? = null

    fun setConfirm(confirmAction: ConfirmAction): SafeDialog? {
        this.confirmAction = confirmAction
        return this
    }

    interface ConfirmAction {

        fun onRightClick()
    }

    private var exitTime: Long = 0
    private fun exit() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtil.showToast(context,"再按一次退出程序")
            exitTime = System.currentTimeMillis()
        } else {

            System.exit(0)
        }
    }

    override fun onBackPressed() {
        exit()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_safe)

        val mBtnConfirm = findViewById<Button>(R.id.dialog_safe_btn_confirm)

        mBtnConfirm.setOnClickListener {
            confirmAction?.onRightClick()
        }
    }


}