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
import com.march.dev.utils.LogUtils;
import com.march.piceditor.common.model.Point;
import com.march.piceditor.graffiti.model.GraffitiPart;

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


    private PorterDuffXfermode mSrcInXfermode;
    private PorterDuffXfermode mClearXfermode;
    private PorterDuffXfermode mSrcOverXfermode;

    private RectF   mImageRect;
    private int     mPathWidth;
    private float   mImageWidth;
    private float   mImageHeight;
    private boolean mIsTouching;
    private boolean mIsErase;

    private Bitmap mSourceImage;
    private Bitmap mGraffitiImage;

    private Paint mGraffitiLayerPaint;
    private Paint mTouchGraffitiPaint;
    private Paint mTouchPointPaint;

    private List<GraffitiPart> mGraffitiPartList;
    private GraffitiPart       mTouchGraffitiPart;
    private Point              mTouchPoint;

    private TouchMode mTouchMode = TouchMode.PATH;

    private int mEraseColor = Color.WHITE;
    private int mDrawColor  = Color.GRAY;

    private void init() {
        // current touch paint
        mTouchGraffitiPaint = DrawUtils.newPaint(Color.WHITE, 10, Paint.Style.STROKE);
        mTouchGraffitiPaint.setPathEffect(new CornerPathEffect(10));
        DrawUtils.initRoundPaint(mTouchGraffitiPaint);

        // current touch paint
        mTouchPointPaint = DrawUtils.newPaint(Color.argb(0xdd, 0xff, 0xff, 0xff), 20, Paint.Style.FILL_AND_STROKE);

        // effect paint
        mGraffitiLayerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraffitiLayerPaint.setPathEffect(new CornerPathEffect(10));
        DrawUtils.initAntiAliasPaint(mGraffitiLayerPaint);
        DrawUtils.initRoundPaint(mGraffitiLayerPaint);

        mTouchPoint = new Point();
        mTouchPoint.reset();
        mImageRect = new RectF();
        mPathWidth = 50;
        mSrcInXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mClearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mSrcOverXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
        mGraffitiPartList = new ArrayList<>();

        setErase(false);
    }

    // 设置 src
    public void setSrc(String file) {
        BitmapFactory.Options bitmapSize = BitmapUtils.getBitmapSize(file);
        mImageWidth = bitmapSize.outWidth;
        mImageHeight = bitmapSize.outHeight;
        mSourceImage = BitmapFactory.decodeFile(file);
        requestLayout();
        postInvalidate();
    }

    // 设置涂抹 layer
    public void setGraffitiLayer(GraffitiLayer graffitiLayer) {
        if (mSourceImage == null) {
            throw new IllegalArgumentException("init setSrc() first");
        }
        long start = System.currentTimeMillis();
        mGraffitiImage = graffitiLayer.buildLayer(getContext(), mSourceImage);
        LogUtils.e(TAG, "generate GraffitiLayer cost = " + (System.currentTimeMillis() - start));
        postInvalidate();
    }

    // 设置涂抹的触摸方式
    public void setTouchMode(TouchMode touchMode) {
        mTouchMode = touchMode;
    }

    // 设置是不是擦除状态
    public void setErase(boolean erase) {
        mIsErase = erase;
        mTouchGraffitiPaint.setColor(erase ? mEraseColor : mDrawColor);
    }

    // 绘制和擦除时的颜色
    public void setEraseAndDrawColor(int eraseColor, int drawColor) {
        mEraseColor = eraseColor;
        mDrawColor = drawColor;
    }

    // 重置
    public void reset() {
        BitmapUtils.recycleBitmaps(mSourceImage, mGraffitiImage);
        mGraffitiPartList.clear();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || event.getPointerCount() > 1
                || mSourceImage == null || mGraffitiImage == null) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchPoint.set(event);
                mIsTouching = true;
                mTouchGraffitiPart = new GraffitiPart(mIsErase, mTouchMode);
                mTouchGraffitiPart.onTouchDown(event, mPathWidth);
                mGraffitiPartList.add(mTouchGraffitiPart);
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchPoint.set(event);
                if (mTouchGraffitiPart != null) {
                    mTouchGraffitiPart.onTouchMove(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchPoint.reset();
                mIsTouching = false;
                mTouchGraffitiPart = null;
                break;
        }
        postInvalidate();
        return true;
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOnCanvas(canvas, false);
    }

    // 分两层绘制，source image & mosaic image
    // 1.初始设计(已弃用)：
    // 马赛克层需要结合3部分，擦除路径，涂抹路径，马赛克涂层
    // 先绘制涂抹路径，然后使用 clear 模式绘制擦除路径，清除与涂抹路径重叠的部分，
    // 然后使用 srcIn 模式绘制马赛克涂层，绘制马赛克涂层与清除后的涂抹路径重合地方
    // 修复设计：
    // 擦除和涂抹在一个列表中，使用 clear mode 绘制擦除
    // 使用 src over mode 绘制涂抹
    // 最后使用 src in mode 绘制绘制涂层
    private void drawOnCanvas(Canvas canvas, boolean isSave) {
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
                    mGraffitiLayerPaint.setXfermode(graffitiPart.isErase()
                            ? mClearXfermode
                            : mSrcOverXfermode);
                    graffitiPart.onDraw(canvas, mGraffitiLayerPaint);
                }

                // 使用 src in mode 绘制马赛克涂层
                mGraffitiLayerPaint.setXfermode(mSrcInXfermode);
                canvas.drawBitmap(mGraffitiImage, null, mImageRect, mGraffitiLayerPaint);

                //最后将画笔去除Xfermode
                mGraffitiLayerPaint.setXfermode(null);
                canvas.restoreToCount(mosaicLayerCount);
            }

            if (!isSave) {
                if (mTouchGraffitiPart != null && mTouchGraffitiPart.getRectF() != null) {
                    // mTouchGraffitiPart.onDraw(canvas, mTouchGraffitiPaint);
                    canvas.drawRect(mTouchGraffitiPart.getRectF(), mTouchPointPaint);
                } else if (mTouchPoint.isValid()) {
                    canvas.drawCircle(mTouchPoint.x, mTouchPoint.y, mPathWidth / 2, mTouchPointPaint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }
        int contentWidth = right - left;
        int contentHeight = bottom - top;
        float widthRatio = contentWidth / mImageWidth;
        float heightRatio = contentHeight / mImageHeight;
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);

        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
    }


    public Bitmap save() {
        Bitmap bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        drawOnCanvas(canvas, true);

        canvas.save();

        return Bitmap.createBitmap(bitmap, ((int) mImageRect.left), ((int) mImageRect.top), ((int) mImageRect.width()), ((int) mImageRect.height()));
    }
}
