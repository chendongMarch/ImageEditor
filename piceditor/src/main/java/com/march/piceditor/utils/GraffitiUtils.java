package com.march.piceditor.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * CreateAt : 7/25/17
 * Describe :
 *
 * @author chendong
 */
public class GraffitiUtils {


    /**
     * 创建纯色 bitmap
     *
     * @param srcBitmap src
     * @param color     color
     * @return 纯色 bitmap
     */
    public static Bitmap getColorBitmap(Bitmap srcBitmap, int color) {
        if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0)
            return null;
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rect, paint);
        canvas.save();
        return bitmap;
    }

    /**
     * 将图片转换为马赛克图片
     *
     * @param srcBitmap source srcBitmap
     * @param radius    马赛克半径
     * @return 马赛克后的图片
     */
    public static Bitmap getMosaicBitmap(Bitmap srcBitmap, int radius) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        Bitmap mosaicBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mosaicBitmap);

        int horCount = (int) Math.ceil(width / (float) radius);
        int verCount = (int) Math.ceil(height / (float) radius);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = radius * horIndex;
                int t = radius * verIndex;
                int r = l + radius;
                if (r > width) {
                    r = width;
                }
                int b = t + radius;
                if (b > height) {
                    b = height;
                }
                int color = srcBitmap.getPixel(l, t);
                Rect rect = new Rect(l, t, r, b);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
        canvas.save();

        return mosaicBitmap;
    }
}
