package com.hzdq.nppvdoctorclient.login.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hzdq.nppvdoctorclient.R;


/**
 * <pre>
 *     author : Sinory
 *     e-mail : 249668399@qq.com
 *     time   : 2022/04/12
 *     desc   : Android Developer
 *     tel    : 15355090637
 * </pre>
 */
public class PrivateDialog extends Dialog implements View.OnClickListener {

    private Context context;

    private String cancel, confirm;
    private IOnCancelListener cancelListener;
    private IOnConfirmListener confirmListener;
    private IOnUserLinkListener userLinkListener;
    private IOnPrivateLinkListener privateLinkListener;
    private Button mBtnConfirm, mBtnCancel;
    private TextView mTv;

    public PrivateDialog(@NonNull Context context) {
        super(context);

    }

    public PrivateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;

    }

    public PrivateDialog setCancel(String cancel, IOnCancelListener listener) {
        this.cancel = cancel;
        this.cancelListener = listener;
        return this;
    }

    public PrivateDialog setConfirm(String confirm, IOnConfirmListener listener) {
        this.confirm = confirm;
        this.confirmListener = listener;
        return this;
    }

    public PrivateDialog setUser(IOnUserLinkListener listener) {

        this.userLinkListener = listener;
        return this;
    }

    public PrivateDialog setPrivate(IOnPrivateLinkListener listener) {

        this.privateLinkListener = listener;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_private);

        mBtnConfirm = findViewById(R.id.private_dialog_btn_confirm);
        mBtnCancel = findViewById(R.id.private_dialog_btn_cancel);
        mTv = findViewById(R.id.private_dialog_text);
        String content = "        本应用尊重并保护所有用户的隐私权。为了给您提供更准确、更有个性化的服务，本应用会按照隐私政策的规定使用和披露您的个人信息。可阅读《用户协议》和《隐私声明》。";

        mBtnConfirm.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        SpannableStringBuilder tSpannableStringBuilder = new SpannableStringBuilder();
        tSpannableStringBuilder.append(content);
        int startIndex1 = content.indexOf("《");
        int endIndex1 = content.indexOf("》");

        int startIndex2 = content.indexOf("《") + 7;
        int endIndex2 = content.indexOf("》") + 8;

        ClickableSpan serveClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (userLinkListener != null) {
                    userLinkListener.onUser();
                }
            }
        };

        ClickableSpan secretClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (privateLinkListener != null) {
                    privateLinkListener.onPrivate();
                }
            }
        };
        //点击1
        tSpannableStringBuilder.setSpan(
                serveClickableSpan,
                startIndex1,
                endIndex1 + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        //颜色1
        tSpannableStringBuilder.setSpan(
                new ForegroundColorSpan(Color.parseColor("#580EFF")),
                startIndex1,
                endIndex1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        //点击2
        tSpannableStringBuilder.setSpan(
                secretClickableSpan,
                startIndex2,
                endIndex2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        //颜色2
        tSpannableStringBuilder.setSpan(
                new ForegroundColorSpan(Color.parseColor("#580EFF")),
                startIndex2,
                endIndex2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        mTv.setText(tSpannableStringBuilder);
        //设置点击事件，加上这句话才有效果
        mTv.setMovementMethod(LinkMovementMethod.getInstance());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.private_dialog_btn_confirm:

                if (confirmListener != null) {
                    confirmListener.onConfirm(this);
                    dismiss();
                }

                break;
            case R.id.private_dialog_btn_cancel:
                if (cancelListener != null) {
                    cancelListener.onCancel(this);
                    dismiss();

                }

                break;
        }
    }


    public interface IOnConfirmListener {
        void onConfirm(PrivateDialog dialog);
    }

    public interface IOnCancelListener {
        void onCancel(PrivateDialog dialog);
    }

    public interface IOnUserLinkListener {
        void onUser();
    }

    public interface IOnPrivateLinkListener {
        void onPrivate();
    }


}

