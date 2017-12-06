package com.march.picedit.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * CreateAt : 2017/12/6
 * Describe :
 *
 * @author chendong
 */
public class PorterDuffXfermodeView extends View {
    public PorterDuffXfermodeView(Context context) {
        this(context, null);
    }

    public PorterDuffXfermodeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PorterDuffXfermodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private PorterDuffXfermode mPorterDuffXfermode;
    private Paint mSrcPaint, mDstPaint;

    private void init() {
        mSrcPaint = new Paint();
        mSrcPaint.setAntiAlias(true);
        mSrcPaint.setColor(Color.BLUE);
        mSrcPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        mDstPaint = new Paint();
        mDstPaint.setAntiAlias(true);
        mDstPaint.setColor(Color.YELLOW);
        mDstPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        setPorterDuffXfermodeIndex(1);
    }

    public static PorterDuff.Mode[] mPorterDuffXfermodeArray = new PorterDuff.Mode[]{
            PorterDuff.Mode.CLEAR,
            PorterDuff.Mode.SRC,
            PorterDuff.Mode.DST,
            PorterDuff.Mode.SRC_OVER,
            PorterDuff.Mode.DST_OVER,
            PorterDuff.Mode.SRC_IN,
            PorterDuff.Mode.DST_IN,
            PorterDuff.Mode.SRC_OUT,
            PorterDuff.Mode.DST_OUT,
            PorterDuff.Mode.SRC_ATOP,
            PorterDuff.Mode.DST_ATOP,
            PorterDuff.Mode.XOR,
            PorterDuff.Mode.DARKEN,
            PorterDuff.Mode.LIGHTEN,
            PorterDuff.Mode.MULTIPLY,
            PorterDuff.Mode.SCREEN,
    };

    public void setPorterDuffXfermodeIndex(int index) {
        mPorterDuffXfermode = new PorterDuffXfermode(mPorterDuffXfermodeArray[index]);
    }

    private float width;
    private float height;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (width == 0 || height == 0) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();
            setMeasuredDimension((int) width, (int) width);
        }
    }

    public void reset() {
        width = 0;
        height = 0;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0 || height == 0)
            return;
        int sc = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
        {
            canvas.drawCircle(width / 3, width / 3, width / 3, mDstPaint);
            mSrcPaint.setXfermode(mPorterDuffXfermode);
            canvas.drawRect(width / 3f, width / 3f, width, width, mSrcPaint);
            canvas.restore();
            mDstPaint.setXfermode(null);
        }

    }
}
