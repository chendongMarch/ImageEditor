package com.march.piceditor.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * CreateAt : 2017/11/29
 * Describe :
 * bitmap 处理类
 *
 * @author chendong
 */
public class BitmapUtils {

    public static BitmapFactory.Options getBitmapSize(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * @param bmp       位图
     * @param file      文件
     * @param format    format
     * @param quality   质量
     * @param isRecycle 是否回收资源
     * @return 存入的file
     */
    public static File compressImage(Bitmap bmp, File file, Bitmap.CompressFormat format, int quality, boolean isRecycle) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            bmp.compress(format, quality, bos);
            bos.flush();
            bos.close();
            if (isRecycle)
                recycleBitmaps(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    public static int calculateSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float inSampleSize = 1.0f;
        while ((inSampleSize * 2) <= ratio) {
            inSampleSize *= 2;
        }
        return (int) inSampleSize;
    }

    // 通过传入的bitmap，进行压缩，得到符合标准的bitmap
    public static Bitmap createScaleBitmap(Bitmap tempBitmap, int destWidth, int destHeight) {
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > destWidth || tempBitmap.getHeight() > destHeight)) {
            // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
            Bitmap bitmap = Bitmap.createScaledBitmap(tempBitmap, destWidth, destHeight, true);
            recycleBitmaps(tempBitmap);
            return bitmap;
        } else {
            return tempBitmap; // 如果没有缩放，那么不回收
        }
    }


    public static void recycleBitmaps(Bitmap... bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            try {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
