package com.march.picedit.sticker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.SparseArrayCompat;

/**
 * CreateAt : 7/21/17
 * Describe :
 *
 * @author chendong
 */
public class ResourceFactory {

    private SparseArrayCompat<Drawable> mMenuIconMap;
    private Context                     mContext;

    public ResourceFactory(Context context) {
        mContext = context;
        mMenuIconMap = new SparseArrayCompat<>();
    }


    public Drawable decodeDrawable(int res) {
        Drawable drawable = mMenuIconMap.get(res);
        if (drawable == null) {
            drawable = new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeResource(mContext.getResources(), res));
            mMenuIconMap.put(res, drawable);
        } else {
            // LogUtils.e(TAG, "使用已有图片");
        }
        return drawable;
    }
}
