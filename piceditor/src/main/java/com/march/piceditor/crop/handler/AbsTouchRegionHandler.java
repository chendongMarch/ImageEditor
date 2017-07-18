package com.march.piceditor.crop.handler;

import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * CreateAt : 7/18/17
 * Describe : 事件处理基类
 *
 * @author chendong
 */
public abstract class AbsTouchRegionHandler {

    public static final int LEFT   = 0;
    public static final int TOP    = 1;
    public static final int RIGHT  = 2;
    public static final int BOTTOM = 3;
    public static final int CENTER = 4;

    public static final int LEFT_TOP     = 5;
    public static final int LEFT_BOTTOM  = 6;
    public static final int RIGHT_TOP    = 7;
    public static final int RIGHT_BOTTOM = 8;

    public static final int TWO_FINGER = 9;

    protected int mTouchRegion;
    protected int mWidth, mHeight;
    protected int mMinWidth, mMinHeight;
    protected RectF mCenterRectF;
    protected float mAspectRatio;

    public AbsTouchRegionHandler() {
    }

    public void init(int touchRegion, RectF centerRectF,
                     int width, int height,
                     int minWidth, int minHeight,
                     float aspectRatio) {
        mTouchRegion = touchRegion;
        mCenterRectF = centerRectF;
        mWidth = width;
        mHeight = height;
        mMinWidth = minWidth;
        mMinHeight = minHeight;
        mAspectRatio = aspectRatio;
    }

    public abstract void onTouchDown(MotionEvent event);

    public abstract void onTouchMove(MotionEvent event);
}
