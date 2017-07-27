package com.march.piceditor.common.model;

import android.view.MotionEvent;

/**
 * CreateAt : 7/21/17
 * Describe : 点
 *
 * @author chendong
 */
public class PointF {
    public float x;
    public float y;

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PointF() {
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(MotionEvent event) {
        set(event.getX(), event.getY());
    }

    public boolean isValid() {
        return x != -1 && y != -1;
    }

    public void reset() {
        x = -1;
        y = -1;
    }
}
