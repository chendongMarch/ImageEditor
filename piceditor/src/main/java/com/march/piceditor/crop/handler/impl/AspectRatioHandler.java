package com.march.piceditor.crop.handler.impl;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.march.piceditor.crop.CropOverlay;
import com.march.piceditor.crop.handler.AbsTouchRegionHandler;

/**
 * CreateAt : 7/18/17
 * Describe : 指定比例缩放处理
 *
 * @author chendong
 */
//  触摸处理器的实现
public class AspectRatioHandler extends AbsTouchRegionHandler {

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

    // 强制比例时的处理，此时不会触发 edge trigger
    private void handleAspectRatioTriggerScale(float diffX, float diffY) {

        float aspectNewTop;
        float aspectNewLeft;
        float aspectNewRight;
        float aspectNewBottom;

        switch (mTouchRegion) {
            case LEFT_TOP:
                aspectNewTop = mCenterRectF.top + ((diffX + diffY) / 2);
                aspectNewLeft = mCenterRectF.left + ((diffX + diffY) / 2) * mAspectRatio;
                if (aspectNewTop <= (mCenterRectF.bottom - mMinHeight)
                        && aspectNewLeft <= (mCenterRectF.right - mMinWidth)) {
                    if (aspectNewTop >= 0 && aspectNewLeft >= 0) {
                        mCenterRectF.top = aspectNewTop;
                        mCenterRectF.left = aspectNewLeft;
                    }
                }
                break;
            case LEFT_BOTTOM:
                aspectNewLeft = mCenterRectF.left + ((diffX - diffY) / 2) * mAspectRatio;
                aspectNewBottom = mCenterRectF.bottom + (-diffX + diffY) / 2;
                if (aspectNewLeft <= (mCenterRectF.right - mMinWidth)
                        && aspectNewBottom >= mCenterRectF.top + mMinHeight) {
                    if (aspectNewLeft >= 0 && aspectNewBottom <= mHeight) {
                        mCenterRectF.left = aspectNewLeft;
                        mCenterRectF.bottom = aspectNewBottom;
                    }
                }
                break;
            case RIGHT_TOP:
                aspectNewRight = mCenterRectF.right + ((diffX - diffY) / 2) * mAspectRatio;
                aspectNewTop = mCenterRectF.top + (-diffX + diffY) / 2;
                if (aspectNewRight >= mCenterRectF.left + mMinWidth
                        && aspectNewTop <= mCenterRectF.bottom - mMinHeight) {
                    if (aspectNewRight <= mWidth && aspectNewTop >= 0) {
                        mCenterRectF.right = aspectNewRight;
                        mCenterRectF.top = aspectNewTop;
                    }
                }
                break;
            case RIGHT_BOTTOM:
                aspectNewRight = mCenterRectF.right + ((diffX + diffY) / 2) * mAspectRatio;
                aspectNewBottom = mCenterRectF.bottom + (diffX + diffY) / 2;
                if (aspectNewRight >= mCenterRectF.left + mMinWidth
                        && aspectNewBottom >= (mCenterRectF.top + mMinHeight)) {
                    if (aspectNewRight <= mWidth && aspectNewBottom <= mHeight) {
                        mCenterRectF.right = aspectNewRight;
                        mCenterRectF.bottom = aspectNewBottom;
                    }
                }
                break;
        }
    }

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

        if (mTouchRegion == CENTER) {
            handleCenterTouchMove(diffX, diffY);
        } else if (mAspectRatio == CropOverlay.NO_ASPECT_RATIO) {
            handleNoAspectRatioTriggerScale(diffX, diffY);
        } else {
            handleAspectRatioTriggerScale(diffX, diffY);
        }
    }
}