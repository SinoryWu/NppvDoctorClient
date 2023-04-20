package com.hzdq.nppvdoctorclient.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.hzdq.nppvdoctorclient.R;
import com.hzdq.nppvdoctorclient.util.SizeUtil;

import org.jetbrains.annotations.Nullable;

/**
 * Time:2023/4/17
 * Author:Sinory
 * Description:
 */
public class SharpView extends View {
    private int mWidth =0; //三角形的宽度
    private int mHeight =0; //三角形的高度
    private Context mContext;
    //创建画笔
    private Paint paint;
    //创建路径
    private Path path;
    private String paintColor = "";
    public SharpView(Context context) {
        super(context);
        this.mContext=context;
        initView();
    }

    public SharpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
//        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.SharpView);
//        this.mWidth = SizeUtil.INSTANCE.dip2px(context, array.getInt(R.styleable.SharpView_mWidth,8));
//        this.mHeight =  SizeUtil.INSTANCE.dip2px(context, array.getInt(R.styleable.SharpView_mHeight,4));
//        this.paintColor = array.getString(R.styleable.SharpView_paintColor);
        initView();
    }




    private void initView() {
        if (mWidth == 0){
            mWidth = SizeUtil.INSTANCE.dip2px(mContext, 8);
        }

        if (mHeight == 0){
            mHeight = SizeUtil.INSTANCE.dip2px(mContext,4);
        }

        paint = new Paint();
        path = new Path();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (paintColor.equals("")){
            paint.setColor(Color.parseColor("#ffffff"));
        }else {
            paint.setColor(Color.parseColor(paintColor));
        }

        paint.setAntiAlias(true); //抗锯齿
        paint.setStyle(Paint.Style.FILL);//实线

        path.moveTo(0,mHeight);
        path.lineTo(mWidth,mHeight);
        path.lineTo(mWidth/2,0);
        path.close();//闭合路径
        //画在画布上
        canvas.drawPath(path,paint);
    }
}
