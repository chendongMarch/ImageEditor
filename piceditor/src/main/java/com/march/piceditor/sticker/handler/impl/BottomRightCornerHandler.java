package com.march.piceditor.sticker.handler.impl;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.march.piceditor.sticker.handler.StickerBaseTouchHandler;
import com.march.piceditor.utils.CalculateUtils;

/**
 * CreateAt : 7/20/17
 * Describe : 右下角缩放旋转的事件
 *
 * @author chendong
 */
public class BottomRightCornerHandler extends StickerBaseTouchHandler {

    private float mLastDistance;// 上一次触摸点距离贴纸中心距离
    private float mLastRotation;// 上一次触摸点旋转角度

    @Override
    public void onTouchDown(MotionEvent event) {
        if (event.getPointerCount() == 1 && isHadStickerActive()) {
            RectF rectF = mActiveSticker.getRectF();
            mLastDistance = CalculateUtils.calculateDistance(rectF.centerX(), rectF.centerY(), event.getX(), event.getY());
            mLastRotation = CalculateUtils.calculateRotation(rectF.centerX(), rectF.centerY(), event.getX(), event.getY());
        }
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        if (event.getPointerCount() == 1
                && isHadStickerActive()
                && mLastDistance > 0) {

            RectF rectF = mActiveSticker.getRectF();

            float distance = CalculateUtils.calculateDistance(rectF.centerX(), rectF.centerY(), event.getX(), event.getY());

            float scale = distance * 1f / mLastDistance;
//            if (mActiveSticker.isCanScale(scale)) {
                mActiveSticker.postMatrixScale(scale, scale);
//            }
            mLastDistance = distance;

            // 旋转
            float rotation = CalculateUtils.calculateRotation(rectF.centerX(), rectF.centerY(), event.getX(), event.getY());
            mActiveSticker.postMatrixRotate(rotation - mLastRotation);
            mLastRotation = rotation;
        }

    }
}
