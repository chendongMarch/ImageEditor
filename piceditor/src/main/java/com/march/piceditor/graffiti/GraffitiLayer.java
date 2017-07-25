package com.march.piceditor.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.march.dev.utils.BitmapUtils;
import com.march.piceditor.utils.Blur;
import com.march.piceditor.utils.GraffitiUtils;

import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.BLUR;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.COLOR;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.IMAGE;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.MOSAIC;

/**
 * CreateAt : 7/25/17
 * Describe : 绘制涂层样式
 * 纯色/颜色
 * 方形马赛克/方格大小
 * 图片/bitmap
 * 高斯模糊/模糊半径
 *
 * @author chendong
 */
public class GraffitiLayer {

    enum GraffitiEffect {
        COLOR, MOSAIC, IMAGE, BLUR
    }

    private GraffitiEffect mGraffitiEffect  = MOSAIC;
    private int            mColor           = Color.TRANSPARENT;
    private int            mMosaicGridWidth = 10;
    private Bitmap         mImageBitmap     = null;
    private int            mBlurRadius      = Blur.BlurFactor.DEFAULT_RADIUS;

    private GraffitiLayer(GraffitiEffect graffitiEffect) {
        mGraffitiEffect = graffitiEffect;
    }

    public static GraffitiLayer newColorLayer(int color) {
        GraffitiLayer graffitiLayer = new GraffitiLayer(COLOR);
        graffitiLayer.setColor(color);
        return graffitiLayer;
    }

    public static GraffitiLayer newMosaicLayer(int mosaicGridWidth) {
        GraffitiLayer graffitiLayer = new GraffitiLayer(MOSAIC);
        graffitiLayer.setMosaicGridWidth(mosaicGridWidth);
        return graffitiLayer;
    }

    public static GraffitiLayer newBlurLayer(int blurRadius) {
        GraffitiLayer graffitiLayer = new GraffitiLayer(BLUR);
        graffitiLayer.setBlurRadius(blurRadius);
        return graffitiLayer;
    }

    public static GraffitiLayer newImageLayer(Bitmap bitmap) {
        GraffitiLayer graffitiLayer = new GraffitiLayer(IMAGE);
        graffitiLayer.setImageBitmap(bitmap);
        return graffitiLayer;
    }


    Bitmap buildLayer(Context context, Bitmap srcBitmap) {

        if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0)
            return null;

        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        if (mGraffitiEffect == MOSAIC) {
            // mosaic
            return GraffitiUtils.getMosaicBitmap(srcBitmap, mMosaicGridWidth);
        } else if (mGraffitiEffect == GraffitiEffect.BLUR) {
            // blur
            Blur.BlurFactor factor = new Blur.BlurFactor();
            factor.width = width;
            factor.height = height;
            factor.radius = mBlurRadius;
            return Blur.blur(context, srcBitmap, factor);
        } else if (mGraffitiEffect == GraffitiEffect.COLOR) {
            // pure color
            return GraffitiUtils.getColorBitmap(srcBitmap, mColor);
        } else if (mGraffitiEffect == GraffitiEffect.IMAGE) {
            // image
            if (mImageBitmap.getWidth() == width && mImageBitmap.getHeight() == height) {
                return mImageBitmap;
            } else {
                float scale = Math.min(mImageBitmap.getWidth() *1f/ width, mImageBitmap.getHeight() *1f / height);
                Bitmap finalBitmap;
                if (scale > 1) {
                    // 截取中间部分
                    finalBitmap = Bitmap.createBitmap(mImageBitmap, (mImageBitmap.getWidth() - width) / 2, (mImageBitmap.getHeight() - height) / 2, width, height);
                    BitmapUtils.recycleBitmaps(mImageBitmap);
                } else {
                    // 放大到至少和 src 一样大
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(mImageBitmap, ((int) (mImageBitmap.getWidth() / scale)), ((int) (mImageBitmap.getHeight() / scale)), false);
                    // 截取中间部分
                    finalBitmap = Bitmap.createBitmap(scaledBitmap, (scaledBitmap.getWidth() - width) / 2, (scaledBitmap.getHeight() - height) / 2, width, height);
                    BitmapUtils.recycleBitmaps(mImageBitmap, scaledBitmap);
                }
                return finalBitmap;
            }
        }

        return null;
    }

    private void setColor(int color) {
        mColor = color;
    }

    private void setMosaicGridWidth(int mosaicGridWidth) {
        mMosaicGridWidth = mosaicGridWidth;
    }

    private void setImageBitmap(Bitmap imageBitmap) {
        mImageBitmap = imageBitmap;
    }

    private void setBlurRadius(int blurRadius) {
        mBlurRadius = blurRadius;
    }
}
