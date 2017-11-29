package com.march.piceditor.functions.crop.gesture.impl;

import android.view.MotionEvent;

import com.march.piceditor.functions.crop.gesture.CropBaseHandler;

/**
 * CreateAt : 7/18/17
 * Describe : 不指定比例缩放
 *
 * @author chendong
 */
//  触摸处理器的实现
public class NoAspectRatioCropHandler extends CropBaseHandler {

    private float mLastX, mLastY;

    // 没有强制比例时的处理，此时会触发 edge trigger
    private void handleNoAspectRatioTriggerScale(float diffX, float diffY) {

        float newLeft = Math.min(mCenterRectF.right - mMinWidth, Math.max(0, mCenterRectF.left + diffX));
        float newTop = Math.min(mCenterRectF.bottom - mMinHeight, Math.max(0, mCenterRectF.top + diffY));
        float newRight = Math.max(mCenterRectF.left + mMinWidth, Math.min(mWidth, mCenterRectF.right + diffX));
        float newBottom = Math.max(mCenterRectF.top + mMinHeight, Math.min(mHeight, mCenterRectF.bottom + diffY));

        switch (mTouchRegion) {
            case LEFT:
                mCenterRectF.left = newLeft;
                break;
            case RIGHT:
                mCenterRectF.right = newRight;
                break;
            case TOP:
                mCenterRectF.top = newTop;
                break;
            case BOTTOM:
                mCenterRectF.bottom = newBottom;
                break;
            case LEFT_TOP:
                mCenterRectF.top = newTop;
                mCenterRectF.left = newLeft;
                break;
            case LEFT_BOTTOM:
                mCenterRectF.left = newLeft;
                mCenterRectF.bottom = newBottom;
                break;
            case RIGHT_TOP:
                mCenterRectF.right = newRight;
                mCenterRectF.top = newTop;
                break;
            case RIGHT_BOTTOM:
                mCenterRectF.right = newRight;
                mCenterRectF.bottom = newBottom;
                break;
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
        handleNoAspectRatioTriggerScale(diffX, diffY);
    }


}