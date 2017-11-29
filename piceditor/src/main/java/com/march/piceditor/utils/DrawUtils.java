package com.march.piceditor.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.view.View;


/**
 * CreateAt : 2017/11/29
 * Describe : 绘制工具类
 *
 * @author chendong
 */
public class DrawUtils {
    private DrawUtils() {
    }

    public static Canvas newCanvas(Bitmap bitmap) {
        Canvas canvas;
        if (bitmap != null) {
            canvas = new Canvas(bitmap);
        } else {
            canvas = new Canvas();
        }

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
        return canvas;
    }

    public static Paint newPaint(int color, int width, Paint.Style style) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(style);
        if (width > 0) {
            paint.setStrokeWidth((float) width);
        }

        initAntiAliasPaint(paint);
        return paint;
    }

    public static void initRoundPaint(Paint paint) {
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public static void initAntiAliasPaint(Paint paint) {
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
    }

    public static void drawViewOnCanvas(Canvas canvas, Paint paint, View view, int width, int height) {
        view.setDrawingCacheEnabled(true);
        Bitmap drawingCache = view.getDrawingCache();
        Bitmap scaledBitmap;
        if (drawingCache.getWidth() == width && drawingCache.getHeight() == height) {
            scaledBitmap = drawingCache;
        } else {
            scaledBitmap = Bitmap.createScaledBitmap(drawingCache, width, height, true);
        }

        canvas.drawBitmap(scaledBitmap, 0.0F, 0.0F, paint);
        view.setDrawingCacheEnabled(false);
         BitmapUtils.recycleBitmaps(drawingCache, scaledBitmap);
    }

    public static void drawViewOnCanvasNoScale(Canvas canvas, Paint paint, View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap drawingCache = view.getDrawingCache();
        canvas.drawBitmap(drawingCache, 0.0F, 0.0F, paint);
        view.setDrawingCacheEnabled(false);
        BitmapUtils.recycleBitmaps(drawingCache);
    }

    public static void drawHLine(Canvas canvas, Paint paint, float y, float fromX, float width) {
        canvas.drawLine(fromX, y, fromX + width, y, paint);
    }

    public static void drawVLine(Canvas canvas, Paint paint, float x, float fromY, float height) {
        canvas.drawLine(x, fromY, x, fromY + height, paint);
    }

    public static float measureTextHeight(Paint p) {
        return Math.abs(p.ascent() + p.descent());
    }
}

