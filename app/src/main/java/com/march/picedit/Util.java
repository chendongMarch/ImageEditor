package com.march.picedit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;

/**
 * CreateAt : 7/18/17
 * Describe :
 *
 * @author chendong
 */
public class Util {
//
//    private StateListDrawable newSelectDrawable(Context context) {
//        Drawable checked = ContextCompat.getDrawable(context, );
//        Drawable unchecked = context.getResources().getDrawable(R.drawable.switch_bg_off_emui);
//        Drawable disabled = context.getResources().getDrawable(R.drawable.switch_bg_disabled_emui);
//        StateListDrawable stateList = new StateListDrawable();
//        int statePressed = android.R.attr.state_pressed;
//        int stateChecked = android.R.attr.state_checked;
//        int stateFocused = android.R.attr.state_focused;
//        int stateEnable = android.R.attr.state_enabled;
//        stateList.addState(new int[]{-stateEnable}, disabled);
//        stateList.addState(new int[]{stateChecked}, checked);
//        stateList.addState(new int[]{statePressed}, checked);
//        stateList.addState(new int[]{stateFocused}, checked);
//        stateList.addState(new int[]{}, unchecked);
//
//
//        return stateList;
//    }


    public static StateListDrawable newSelectDrawable(Context context, int selectRes, int unSelectRes) {
        Drawable selectDrawable = ContextCompat.getDrawable(context, selectRes);
        Drawable unSelectDrawable = ContextCompat.getDrawable(context, unSelectRes);
        StateListDrawable stateList = new StateListDrawable();
        int stateSelected = android.R.attr.state_selected;
        stateList.addState(new int[]{stateSelected}, selectDrawable);
        stateList.addState(new int[]{stateSelected}, unSelectDrawable);
        stateList.addState(new int[]{}, unSelectDrawable);
        return stateList;
    }
}
