package com.march.piceditor.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.march.piceditor.utils.Blur;
import com.march.piceditor.utils.GraffitiUtils;
import com.uniquestudio.lowpoly.LowPoly;

import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.BLUR;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.COLOR;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.IMAGE;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.LOWPOLY;
import static com.march.piceditor.graffiti.GraffitiLayer.GraffitiEffect.MOSAIC;

/**
 * CreateAt : 7/25/17
 * Describe : 绘制涂层样式
 * 纯色/颜色
 * 方形马赛克/方格大小
 * 图片/bitmap
 * 高斯模糊/模糊半径
 * poly/
 * @author chendong
 */
public class GraffitiLayer {

    enum GraffitiEffect {
        COLOR, MOSAIC, IMAGE, BLUR, LOWPOLY
    }

    public static final int DEF_VALUE = -1;

    private GraffitiEffect mGraffitiEffect  = MOSAIC;
    private int            mColor           = Color.TRANSPARENT;
    private int            mMosaicGridWidth = 10;
    private int            mGradientThresh  = 40;
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

    public static GraffitiLayer newLowPolyLayer(int gradientThresh) {
        GraffitiLayer graffitiLayer = new GraffitiLayer(LOWPOLY);
        graffitiLayer.setMosaicGridWidth(gradientThresh);
        return graffitiLayer;
    }


    Bitmap buildLayer(Context context, Bitmap srcBitmap) {

        if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0)
            return null;

        if (mGraffitiEffect == MOSAIC) {
            // mosaic
            return GraffitiUtils.getMosaicBitmap(srcBitmap, mMosaicGridWidth);
        } else if (mGraffitiEffect == GraffitiEffect.BLUR) {
            // blur
            return GraffitiUtils.getBlurBitmap(context, srcBitmap, mBlurRadius);
        } else if (mGraffitiEffect == GraffitiEffect.COLOR) {
            // pure color
            return GraffitiUtils.getColorBitmap(srcBitmap, mColor);
        } else if (mGraffitiEffect == GraffitiEffect.IMAGE) {
            // image
            return GraffitiUtils.getCenterCropBitmap(srcBitmap, mImageBitmap);
        } else if (mGraffitiEffect == GraffitiEffect.LOWPOLY) {
            // low poly
            return LowPoly.generate(srcBitmap, mGradientThresh);
        }
        return null;
    }


    private void setColor(int color) {
        if (color != DEF_VALUE)
            mColor = color;
    }

    private void setMosaicGridWidth(int mosaicGridWidth) {
        if (mosaicGridWidth != DEF_VALUE)
            mMosaicGridWidth = mosaicGridWidth;
    }

    private void setImageBitmap(Bitmap imageBitmap) {
        mImageBitmap = imageBitmap;
    }

    private void setBlurRadius(int blurRadius) {
        if (blurRadius != DEF_VALUE)
            mBlurRadius = blurRadius;
    }

    public void setGradientThresh(int gradientThresh) {
        if (gradientThresh != DEF_VALUE)
            mGradientThresh = gradientThresh;
    }
}
