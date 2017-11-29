package com.march.piceditor.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;

import android.view.View;
import android.view.ViewGroup;

/**
 * CreateAt : 2017/11/29
 * Describe :
 *
 * @author chendong
 */
public class ViewUtils {

    public static void setBackground(    View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            view.setBackground(drawable);
        else
            view.setBackgroundDrawable(drawable);
    }


    public static void setVisibility(    View view, int visibility) {
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    public static void setLayoutParam(int width, int height, View... views) {
        ViewGroup.LayoutParams layoutParams;
        for (View view : views) {
            layoutParams = view.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }
}
