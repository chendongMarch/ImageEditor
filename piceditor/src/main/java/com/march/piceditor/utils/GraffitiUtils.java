package com.march.piceditor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.march.piceditor.utils.BitmapUtils;

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


    /**
     * 获取模糊图，模糊算法使用 rs 和 fast blur 兼容
     *
     * @param context    ctx
     * @param srcBitmap  src
     * @param blurRadius 模糊半径
     * @return 模糊后的图片
     */
    public static Bitmap getBlurBitmap(Context context, Bitmap srcBitmap, int blurRadius) {
        Blur.BlurFactor factor = new Blur.BlurFactor();
        factor.width = srcBitmap.getWidth();
        factor.height = srcBitmap.getHeight();
        factor.radius = blurRadius;
        return Blur.blur(context, srcBitmap, factor);
    }


    /**
     * 将 cropImage 中心裁剪适配 srcBitmap
     * @param srcBitmap 操作的 bitmap
     * @param cropImage 作为涂抹涂层的 bitmap
     * @return 裁剪后的 bitmap
     */
    public static Bitmap getCenterCropBitmap(Bitmap srcBitmap, Bitmap cropImage) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        if (cropImage.getWidth() == width && cropImage.getHeight() == height) {
            return cropImage;
        } else {
            float scale = Math.min(cropImage.getWidth() * 1f / width, cropImage.getHeight() * 1f / height);
            Bitmap finalBitmap;
            if (scale > 1) {
                // 截取中间部分
                finalBitmap = Bitmap.createBitmap(cropImage, (cropImage.getWidth() - width) / 2, (cropImage.getHeight() - height) / 2, width, height);
                BitmapUtils.recycleBitmaps(cropImage);
            } else {
                // 放大到至少和 src 一样大
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(cropImage, ((int) (cropImage.getWidth() / scale)), ((int) (cropImage.getHeight() / scale)), false);
                // 截取中间部分
                finalBitmap = Bitmap.createBitmap(scaledBitmap, (scaledBitmap.getWidth() - width) / 2, (scaledBitmap.getHeight() - height) / 2, width, height);
                BitmapUtils.recycleBitmaps(cropImage, scaledBitmap);
            }
            return finalBitmap;
        }
    }
}
