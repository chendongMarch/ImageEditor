package com.march.piceditor.crop.handler.impl;

import android.view.MotionEvent;

import com.march.piceditor.crop.handler.AbsTouchRegionHandler;

/**
 * CreateAt : 7/18/17
 * Describe : 双指缩放事件处理
 *
 * @author chendong
 */
public class TwoFingerHandler extends AbsTouchRegionHandler {

    private float mLastFingersDistance;

    @Override
    public void onTouchDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mLastFingersDistance = calculateFingersDistance(event);
        }
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float distance = calculateFingersDistance(event);
            if (mLastFingersDistance != 0) {
                scaleRect((distance * 1f / mLastFingersDistance));
            }
            mLastFingersDistance = distance;
        }
    }

    private void scaleRect(float scale) {

        float centerX = mCenterRectF.left + mCenterRectF.width() / 2;
        float centerY = mCenterRectF.top + mCenterRectF.height() / 2;

        float newWidth = mCenterRectF.width() * scale;
        float newHeight = mCenterRectF.height() * scale;

        float left = centerX - newWidth / 2;
        float top = centerY - newHeight / 2;
        float right = left + newWidth;
        float bottom = top + newHeight;

        if (left >= 0 && top >= 0 && right <= mWidth && bottom <= mHeight) {
            if (newWidth >= mMinWidth && newHeight >= mMinHeight) {
                mCenterRectF.set(left, top, left + newWidth, top + newHeight);
            }
        }
    }

    private float calculateFingersDistance(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

}
