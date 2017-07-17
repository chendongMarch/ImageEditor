package com.march.picedit.crop;

/**
 * CreateAt : 7/17/17
 * Describe :
 *
 * @author chendong
 */
public interface TouchRegionHandler {

    int LEFT   = 0;
    int TOP    = 1;
    int RIGHT  = 2;
    int BOTTOM = 3;
    int CENTER = 4;

    int LEFT_TOP     = 5;
    int LEFT_BOTTOM  = 6;
    int RIGHT_TOP    = 7;
    int RIGHT_BOTTOM = 8;

    void handleTouch(float diffX, float diffY);
}
