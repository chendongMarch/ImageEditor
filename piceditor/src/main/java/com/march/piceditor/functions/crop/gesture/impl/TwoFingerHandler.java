package com.march.piceditor.functions.crop.gesture.impl;

import android.view.MotionEvent;

import com.march.piceditor.functions.crop.gesture.CropBaseHandler;
import com.march.piceditor.utils.CalculateUtils;

/**
 * CreateAt : 7/18/17
 * Describe : 双指缩放事件处理
 *
 * @author chendong
 */
public class TwoFingerHandler extends CropBaseHandler {

    private float mLastFingersDistance;

    @Override
    public void onTouchDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mLastFingersDistance = CalculateUtils.calculateDistance(event);
        }
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        if (event.getPointerCount() == 2 && mLastFingersDistance > 0) {
            float distance = CalculateUtils.calculateDistance(event);
            if (mLastFingersDistance != 0) {
                float scale = distance * 1f / mLastFingersDistance;
                scaleRect(scale);
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

}
