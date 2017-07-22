package com.march.piceditor.common;

import android.view.MotionEvent;

/**
 * CreateAt : 7/20/17
 * Describe : 触摸事件处理
 *
 * @author chendong
 */
public interface TouchHandler {

    void onTouchDown(MotionEvent event);

    void onTouchMove(MotionEvent event);
}
