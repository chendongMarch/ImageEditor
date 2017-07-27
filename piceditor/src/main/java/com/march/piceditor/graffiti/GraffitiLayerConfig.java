package com.march.piceditor.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.march.piceditor.utils.Blur;
import com.march.piceditor.utils.GraffitiUtils;
import com.uniquestudio.lowpoly.LowPoly;

import static com.march.piceditor.graffiti.GraffitiLayerConfig.GraffitiEffect.BLUR;
import static com.march.piceditor.graffiti.GraffitiLayerConfig.GraffitiEffect.COLOR;
import static com.march.piceditor.graffiti.GraffitiLayerConfig.GraffitiEffect.IMAGE;
import static com.march.piceditor.graffiti.GraffitiLayerConfig.GraffitiEffect.LOW_POLY;
import static com.march.piceditor.graffiti.GraffitiLayerConfig.GraffitiEffect.MOSAIC;

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
public class GraffitiLayerConfig {

    enum GraffitiEffect {
        COLOR, MOSAIC, IMAGE, BLUR, LOW_POLY
    }

    public static final int DEF_VALUE = -1;

    private GraffitiEffect mGraffitiEffect  = MOSAIC;
    private int            mColor           = Color.TRANSPARENT;
    private int            mMosaicGridWidth = 10;
    private int            mGradientThresh  = 40;
    private Bitmap         mImageBitmap     = null;
    private int            mBlurRadius      = Blur.BlurFactor.DEFAULT_RADIUS;

    private GraffitiLayerConfig(GraffitiEffect graffitiEffect) {
        mGraffitiEffect = graffitiEffect;
    }

    public static GraffitiLayerConfig newColorLayer(int color) {
        // 7s cost
        GraffitiLayerConfig graffitiLayer = new GraffitiLayerConfig(COLOR);
        graffitiLayer.setColor(color);
        return graffitiLayer;
    }

    public static GraffitiLayerConfig newMosaicLayer(int mosaicGridWidth) {
        GraffitiLayerConfig graffitiLayer = new GraffitiLayerConfig(MOSAIC);
        graffitiLayer.setMosaicGridWidth(mosaicGridWidth);
        return graffitiLayer;
    }

    public static GraffitiLayerConfig newBlurLayer(int blurRadius) {
        GraffitiLayerConfig graffitiLayer = new GraffitiLayerConfig(BLUR);
        graffitiLayer.setBlurRadius(blurRadius);
        return graffitiLayer;
    }

    public static GraffitiLayerConfig newImageLayer(Bitmap bitmap) {
        GraffitiLayerConfig graffitiLayer = new GraffitiLayerConfig(IMAGE);
        graffitiLayer.setImageBitmap(bitmap);
        return graffitiLayer;
    }

    public static GraffitiLayerConfig newLowPolyLayer(int gradientThresh) {
        GraffitiLayerConfig graffitiLayer = new GraffitiLayerConfig(LOW_POLY);
        graffitiLayer.setMosaicGridWidth(gradientThresh);
        return graffitiLayer;
    }


    Bitmap buildLayer(Context context, Bitmap srcBitmap) {

        if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0)
            return null;

        if (mGraffitiEffect == MOSAIC) {
            // mosaic 15s cost
            return GraffitiUtils.getMosaicBitmap(srcBitmap, mMosaicGridWidth);
        } else if (mGraffitiEffect == GraffitiEffect.BLUR) {
            // blur 45s cost
            return GraffitiUtils.getBlurBitmap(context, srcBitmap, mBlurRadius);
        } else if (mGraffitiEffect == GraffitiEffect.COLOR) {
            // pure color
            return GraffitiUtils.getColorBitmap(srcBitmap, mColor);
        } else if (mGraffitiEffect == GraffitiEffect.IMAGE) {
            // image 0s cost
            return GraffitiUtils.getCenterCropBitmap(srcBitmap, mImageBitmap);
        } else if (mGraffitiEffect == GraffitiEffect.LOW_POLY) {
            // low poly 480s cost
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
