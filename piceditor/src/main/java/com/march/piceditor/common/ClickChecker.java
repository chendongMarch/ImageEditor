package com.march.piceditor.common;

import android.view.MotionEvent;

import com.march.piceditor.model.PointF;

/**
 * CreateAt : 7/22/17
 * Describe :
 * 对 touch event 进行是不是点击的检测
 *
 * @author chendong
 */
public class ClickChecker {

    private long   mLastDownTime;
    private PointF mLastDownPoint;

    public void onTouchDown(MotionEvent event) {
        mLastDownTime = System.currentTimeMillis();
        mLastDownPoint = new PointF(event.getX(), event.getY());
    }


    public boolean checkIsClickOnTouchUp(MotionEvent event) {
        long diffTime = System.currentTimeMillis() - mLastDownTime;
        float diffX = event.getX() - mLastDownPoint.x;
        float diffY = event.getY() - mLastDownPoint.y;
        return diffTime < 400 && Math.abs(diffX) < 50 && Math.abs(diffY) < 50;
    }
}
