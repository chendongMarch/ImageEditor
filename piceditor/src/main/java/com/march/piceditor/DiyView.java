package com.march.piceditor;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * CreateAt : 7/18/17
 * Describe :
 *
 * @author chendong
 */
public abstract class DiyView extends View {

    public DiyView(Context context) {
        this(context, null);
    }

    public DiyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected abstract void init();

    protected void onMeasureSuccess() {

    }

    protected int mWidth, mHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            onMeasureSuccess();
        }
    }

}
