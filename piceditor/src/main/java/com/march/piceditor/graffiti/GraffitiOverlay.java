package com.march.piceditor.graffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.march.dev.utils.BitmapUtils;
import com.march.dev.utils.DrawUtils;
import com.march.piceditor.graffiti.model.GraffitiPart;
import com.march.piceditor.utils.GraffitiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateAt : 7/25/17
 * Describe :
 * 涂鸦
 * 涂抹和擦除样式，矩形框，path
 * 马赛克样式，纯色，高斯模糊，马赛克，指定图片
 *
 * @author chendong
 */
public class GraffitiOverlay extends View {

    public static final String TAG = GraffitiOverlay.class.getSimpleName();
    private PorterDuffXfermode mSrcOverXfermode;

    public GraffitiOverlay(Context context) {
        this(context, null);
    }

    public GraffitiOverlay(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraffitiOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 触摸的效果，矩形区域和路径
    public enum TouchMode {
        RECT, PATH
    }

    //  马赛克效果，纯色，方形马赛克，图片，高斯模糊
    public enum GraffitiEffect {
        COLOR, MOSAIC, IMAGE, BLUR
    }


    private GraffitiPart mTouchGraffitiPart;

    private PorterDuffXfermode mSrcInXfermode;
    private PorterDuffXfermode mClearXfermode;

    private RectF mImageRect;

    private float mImageWidth;
    private float mImageHeight;

    private Bitmap mSourceImage;
    private Bitmap mGraffitiImage;

    private Paint mGraffitiLayerPaint;
    private Paint mTouchGraffitiPaint;

    private int mPathWidth = 50;

    private List<GraffitiPart> mSmearPaths; // 涂抹路径
    private List<GraffitiPart> mErasePaths; // 擦除路径

    private List<GraffitiPart> mGraffitiPartList;

    private TouchMode mTouchMode = TouchMode.RECT;

    private void init() {
        // current touch paint
        mTouchGraffitiPaint = DrawUtils.newPaint(Color.WHITE, 10, Paint.Style.STROKE);
        mTouchGraffitiPaint.setPathEffect(new CornerPathEffect(10));
        DrawUtils.initRoundPaint(mTouchGraffitiPaint);
        setErase(false);

        // mosaic paint
        mGraffitiLayerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraffitiLayerPaint.setPathEffect(new CornerPathEffect(10));
        DrawUtils.initAntiAliasPaint(mGraffitiLayerPaint);
        DrawUtils.initRoundPaint(mGraffitiLayerPaint);

        mImageRect = new RectF();
        mSmearPaths = new ArrayList<>();
        mErasePaths = new ArrayList<>();
        mSrcInXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mClearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mSrcOverXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
        mGraffitiPartList = new ArrayList<>();
    }

//    public void reset() {
//        mSmearPaths.clear();
//        mErasePaths.clear();
//    }


    public void setTouchMode(TouchMode touchMode) {
        mTouchMode = touchMode;
    }

    public void setErase(boolean erase) {
        mIsErase = erase;
        mTouchGraffitiPaint.setColor(erase ? Color.WHITE : Color.GRAY);
    }

    public void setSrc(String file) {

        BitmapFactory.Options bitmapSize = BitmapUtils.getBitmapSize(file);
        mImageWidth = bitmapSize.outWidth;
        mImageHeight = bitmapSize.outHeight;

        mSourceImage = BitmapFactory.decodeFile(file);
        mGraffitiImage = GraffitiUtils.getMosaic(mSourceImage, 10);

        requestLayout();

        postInvalidate();

    }

    private boolean mIsTouching;

    private boolean mIsErase;

    // 如何处理涂抹路径？
    // down 时创建 path，存储，并指定起始位置，move 时移动 path
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouching = true;
                mTouchGraffitiPart = new GraffitiPart(mIsErase, mTouchMode);
                mTouchGraffitiPart.onTouchDown(event, mPathWidth);
                if (mIsErase) {
                    mErasePaths.add(mTouchGraffitiPart);
                } else {
                    mSmearPaths.add(mTouchGraffitiPart);
                }
                mGraffitiPartList.add(mTouchGraffitiPart);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchGraffitiPart != null) {
                    mTouchGraffitiPart.onTouchMove(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsTouching = false;
                mTouchGraffitiPart = null;
                break;
        }
        postInvalidate();
        return true;
    }


    // 分两层绘制，source image & mosaic image
    // 马赛克层需要结合3部分，擦除路径，涂抹路径，马赛克涂层
    // 先绘制涂抹路径，然后使用 clear 模式绘制擦除路径，清除与涂抹路径重叠的部分，
    // 然后使用 srcIn 模式绘制马赛克涂层，绘制马赛克涂层与清除后的涂抹路径重合地方
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {

            if (mSourceImage == null || mImageRect == null)
                return;

            canvas.drawBitmap(mSourceImage, null, mImageRect, null);

            if (mSrcInXfermode == null || mClearXfermode == null || mGraffitiImage == null)
                return;

            int mosaicLayerCount = canvas.saveLayer(mImageRect, null, Canvas.ALL_SAVE_FLAG);
            {
                // Collections.sort(mGraffitiPartList);
                // 按照时间排序绘制，erase 的用 clear mode，否则用 src over mode
                for (GraffitiPart graffitiPart : mGraffitiPartList) {
                    mGraffitiLayerPaint.setXfermode(graffitiPart.isErase() ? mClearXfermode : mSrcOverXfermode);
                    graffitiPart.onDraw(canvas, mGraffitiLayerPaint);
                }

                // 使用 src in mode 绘制马赛克涂层
                mGraffitiLayerPaint.setXfermode(mSrcInXfermode);
                canvas.drawBitmap(mGraffitiImage, null, mImageRect, mGraffitiLayerPaint);

                //最后将画笔去除Xfermode
                mGraffitiLayerPaint.setXfermode(null);
                canvas.restoreToCount(mosaicLayerCount);
            }

            if (mTouchGraffitiPart != null) {
                mTouchGraffitiPart.onDraw(canvas, mTouchGraffitiPaint);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        int contentWidth = right - left;
        int contentHeight = bottom - top;
        int viewWidth = contentWidth - 0 * 2;
        int viewHeight = contentHeight - 0 * 2;
        float widthRatio = viewWidth / ((float) mImageWidth);
        float heightRatio = viewHeight / ((float) mImageHeight);
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);

        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
    }
}
