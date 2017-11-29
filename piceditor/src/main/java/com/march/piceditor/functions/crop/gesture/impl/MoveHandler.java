package com.march.piceditor.functions.crop.gesture.impl;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.march.piceditor.functions.crop.gesture.CropBaseHandler;

/**
 * CreateAt : 7/18/17
 * Describe :
 *
 * @author chendong
 */
//  触摸处理器的实现
public class MoveHandler extends CropBaseHandler {

    private float mLastX, mLastY;

    // 中间移动处理
    private void handleCenterTouchMove(float diffX, float diffY) {
        if (mTouchRegion == CENTER) {
            float newLeft = Math.max(0, mCenterRectF.left + diffX);
            float newTop = Math.max(0, mCenterRectF.top + diffY);

            if (newLeft + mCenterRectF.width() >= mWidth) {
                newLeft = mCenterRectF.left;
            }
            if (newTop + mCenterRectF.height() >= mHeight) {
                newTop = mCenterRectF.top;
            }
            RectF temp = new RectF(newLeft, newTop, newLeft + mCenterRectF.width(), newTop + mCenterRectF.height());
            mCenterRectF.set(temp);
        }
    }

    @Override
    public void onTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        float diffX = event.getX() - mLastX;
        float diffY = event.getY() - mLastY;
        mLastX = event.getX();
        mLastY = event.getY();
        handleCenterTouchMove(diffX, diffY);
    }
}