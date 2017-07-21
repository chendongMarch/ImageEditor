package com.march.piceditor.sticker.handler.impl;

import android.view.MotionEvent;

import com.march.piceditor.sticker.handler.StickerBaseTouchHandler;

/**
 * CreateAt : 7/20/17
 * Describe : 贴纸移动的处理类
 *
 * @author chendong
 */
public class MoveHandler extends StickerBaseTouchHandler {

    private float mInitX, mInitY;

    @Override
    public void onTouchDown(MotionEvent event) {
        if (event.getPointerCount() == 1 && isHadStickerActive()) {
            mInitX = event.getX();
            mInitY = event.getY();
        }
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        if(event.getPointerCount() == 1 && isHadStickerActive()
                && mInitX>0&&mInitY>0) {
            float diffX = event.getX() - mInitX;
            float diffY = event.getY() - mInitY;
            // 位移
            mActiveSticker.getMatrix().postTranslate(diffX, diffY);
            mInitX = event.getX();
            mInitY = event.getY();
        }
    }
}
