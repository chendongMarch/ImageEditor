package com.march.piceditor.utils;

import android.graphics.ColorMatrixColorFilter;

/**
 * CreateAt : 7/22/17
 * Describe :
 *
 * @author chendong
 */
public class Utils {

    public static ColorMatrixColorFilter buildColorFilter(int red, int green, int blue) {
        float[] src = new float[]{
                0, 0, 0, 0, red,
                0, 0, 0, 0, green,
                0, 0, 0, 0, blue,
                0, 0, 0, 1, 0};
        return new ColorMatrixColorFilter(src);
    }

}
