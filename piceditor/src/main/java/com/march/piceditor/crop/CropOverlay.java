package com.march.piceditor.crop;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.march.dev.utils.BitmapUtils;
import com.march.dev.utils.DrawUtils;
import com.march.dev.utils.LogUtils;
import com.march.dev.utils.ViewUtils;
import com.march.piceditor.crop.handler.AbsTouchRegionHandler;
import com.march.piceditor.crop.handler.impl.AspectRatioHandler;
import com.march.piceditor.crop.handler.impl.MoveHandler;
import com.march.piceditor.crop.handler.impl.NoAspectRatioHandler;
import com.march.piceditor.crop.handler.impl.TwoFingerHandler;

import java.util.Locale;

/**
 * CreateAt : 7/15/17
 * Describe : 裁剪蒙版操作层
 *
 * @author chendong
 */
public class CropOverlay extends View {

    public static final String TAG                 = CropOverlay.class.getSimpleName();
    public static final float  ORIGIN_SCALE_FACTOR = 0.618f;
    public static final int    NO_ASPECT_RATIO     = -1;
    public static final int    INVALID_VALUE       = -1;


    public CropOverlay(Context context) {
        this(context, null);
    }

    public CropOverlay(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private Paint mBgPaint; // 透明黑色背景
    private Paint mTriggerPaint;// 触发器绘制
    private Paint mIndicatorLinePaint; // 边框，指示器网格
    private Paint mTextPaint; // 文字

    private RectF mCenterRectF; // 中间截取的矩形
    private RectF mTopRectF; // 截取区域上方矩形
    private RectF mLeftRectF; // 截取区域左边矩形
    private RectF mBottomRectF; // 截取区域底部矩形
    private RectF mRightRectF; // 截取区域右边矩形
    private RectF mTestRectF; // 测试用

    private int mWidth, mHeight;// 控件的宽高
    private int mTriggerLength            = 100; // 触发器的长度
    private int mTriggerLineStrokeWidth   = 20; // 触发器器的条纹宽度
    private int mIndicatorLineStrokeWidth = 3; // 指示器的条纹宽度

    private int mMinWidth = 2 * mTriggerLength, mMinHeight = 2 * mTriggerLength; // 最小的宽高，保证触发器不会重叠的初始值
    public int mTouchEnlargeCheckRegion = mTriggerLineStrokeWidth * 3; // 触摸时检测方法检测范围，避免检测不到
    private boolean mIsInTouching; // 是否在触摸过程中
    private boolean mIsShowGridIndicator;// 是否显示网格指示器

    private float mAspectRatio = NO_ASPECT_RATIO; // 确定的比例 ,w/h

    private SparseArrayCompat<AbsTouchRegionHandler> mHandlerMap; // 触摸处理类的管理，避免多次初始化
    private AbsTouchRegionHandler                    mTouchRegionHandler; // 触摸处理


    private void init() {
        mBgPaint = DrawUtils.newPaint(Color.parseColor("#5f000000"), 0, Paint.Style.FILL_AND_STROKE);
        mTriggerPaint = DrawUtils.newPaint(Color.WHITE, mTriggerLineStrokeWidth, Paint.Style.STROKE);
        mIndicatorLinePaint = DrawUtils.newPaint(Color.WHITE, mIndicatorLineStrokeWidth, Paint.Style.STROKE);
        mTextPaint = DrawUtils.newPaint(Color.WHITE, 2, Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(45);
        setShowGridIndicator(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();

            setMeasuredDimension(mWidth, mHeight);
            int centerWidth = (int) (mWidth / 2f);
            int centerHeight = (int) (mHeight / 2f);
            mCenterRectF = new RectF((mWidth - centerWidth) / 2, (mHeight - centerHeight) / 2, (mWidth - centerWidth) / 2 + centerWidth, (mHeight - centerHeight) / 2 + centerHeight);

            mTopRectF = new RectF();
            mLeftRectF = new RectF();
            mRightRectF = new RectF();
            mBottomRectF = new RectF();

            updateBackgroundRectF();
        }
    }


    public void reset() {
        mWidth = 0;
        mHeight = 0;
        mAspectRatio = NO_ASPECT_RATIO;
        postInvalidate();
    }

    private boolean hasNoAspectRatio() {
        return mAspectRatio == NO_ASPECT_RATIO;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCenterRectF == null)
            return;

        // draw bg
        canvas.drawRect(mTopRectF, mBgPaint);
        canvas.drawRect(mLeftRectF, mBgPaint);
        canvas.drawRect(mRightRectF, mBgPaint);
        canvas.drawRect(mBottomRectF, mBgPaint);

        // 四边边缘触发器
        if (hasNoAspectRatio()) {
            float horizontalEdgeTriggerFromX = mCenterRectF.left + (mCenterRectF.width() - mTriggerLength) / 2;// 水平边缘触发器开始的x
            float verticalEdgeTriggerFromY = mCenterRectF.top + (mCenterRectF.height() - mTriggerLength) / 2;// 垂直边缘触发器开始的y
            DrawUtils.drawHLine(canvas, mTriggerPaint, mCenterRectF.top, horizontalEdgeTriggerFromX, mTriggerLength); // top
            DrawUtils.drawHLine(canvas, mTriggerPaint, mCenterRectF.bottom, horizontalEdgeTriggerFromX, mTriggerLength); // bottom
            DrawUtils.drawVLine(canvas, mTriggerPaint, mCenterRectF.left, verticalEdgeTriggerFromY, mTriggerLength); // left
            DrawUtils.drawVLine(canvas, mTriggerPaint, mCenterRectF.right, verticalEdgeTriggerFromY, mTriggerLength); // right
        }

        // 四角触发器
        float verticalBottomCornerTriggerFromY = mCenterRectF.top + mCenterRectF.height() - mTriggerLength / 2; // 垂直的 底部 角触发器开始的Y
        float horizontalRightCornerTriggerFromX = mCenterRectF.left + mCenterRectF.width() - mTriggerLength / 2; // 水平的 右边的 角触发器开始的X

        DrawUtils.drawHLine(canvas, mTriggerPaint, mCenterRectF.top, mCenterRectF.left - mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // top-left-h
        DrawUtils.drawVLine(canvas, mTriggerPaint, mCenterRectF.left, mCenterRectF.top - mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // top-left-v

        DrawUtils.drawHLine(canvas, mTriggerPaint, mCenterRectF.bottom, mCenterRectF.left - mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // bottom-left-h
        DrawUtils.drawVLine(canvas, mTriggerPaint, mCenterRectF.left, verticalBottomCornerTriggerFromY + mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // bottom-left-v

        DrawUtils.drawHLine(canvas, mTriggerPaint, mCenterRectF.top, horizontalRightCornerTriggerFromX + mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // top-right-h
        DrawUtils.drawVLine(canvas, mTriggerPaint, mCenterRectF.right, mCenterRectF.top - mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // top-right-v

        DrawUtils.drawHLine(canvas, mTriggerPaint, mCenterRectF.bottom, horizontalRightCornerTriggerFromX + mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // bottom-right-h
        DrawUtils.drawVLine(canvas, mTriggerPaint, mCenterRectF.right, verticalBottomCornerTriggerFromY + mTriggerLineStrokeWidth / 2, mTriggerLength / 2); // bottom-right-v

        // 边框
        canvas.drawRect(mCenterRectF, mIndicatorLinePaint);

        // 触摸时显示指示线
        if (mIsShowGridIndicator && mIsInTouching) {
            for (int i = 1; i < 3; i++) {
                DrawUtils.drawHLine(canvas, mIndicatorLinePaint, mCenterRectF.top + mCenterRectF.height() / 3 * i, mCenterRectF.left, mCenterRectF.width());
            }
            for (int i = 1; i < 3; i++) {
                DrawUtils.drawVLine(canvas, mIndicatorLinePaint, mCenterRectF.left + mCenterRectF.width() / 3 * i, mCenterRectF.top, mCenterRectF.height());
            }
        }

        // 文字
        String msg = String.format(Locale.CHINA, "%.0fx%.0f", mCenterRectF.width(), mCenterRectF.height());
//      String msg = String.format(Locale.CHINA, "%.0fX%.0f/%.2f", mCenterRectF.width(), mCenterRectF.height(), mCenterRectF.width() / mCenterRectF.height());
        canvas.drawText(msg, mCenterRectF.left + mCenterRectF.width() / 2, mCenterRectF.top + mCenterRectF.height() / 2 + DrawUtils.measureTextHeight(mTextPaint) / 2, mTextPaint);

//        if (mTestRectF != null)
//            canvas.drawRect(mTestRectF, DrawUtils.newPaint(Color.RED, 5, Paint.Style.STROKE));

    }

    // 根据确定的中间区域矩形更新其他矩形
    private void updateBackgroundRectF() {
        //judgeAspectRatioCompensation();
        mTopRectF.set(0, 0, mWidth, mCenterRectF.top);
        mLeftRectF.set(0, mCenterRectF.top, mCenterRectF.left, mCenterRectF.bottom);
        mRightRectF.set(mCenterRectF.right, mCenterRectF.top, mWidth, mCenterRectF.bottom);
        mBottomRectF.set(0, mCenterRectF.bottom, mWidth, mHeight);
    }


    // 绝对比例时，调整比例值，补偿数值转换的损失
    private void judgeAspectRatioCompensation() {
        if (mAspectRatio != -1) {
            float height = mCenterRectF.height();
            float width = height * mAspectRatio;
            mCenterRectF.set(mCenterRectF.left, mCenterRectF.top, mCenterRectF.left + width, mCenterRectF.top + height);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }
        int actionMasked = event.getActionMasked();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mIsInTouching = true;
                mTouchRegionHandler = findTouchHandlerOnTouchDown(event);
                postInvalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mIsInTouching = true;
                mTouchRegionHandler = findTouchHandlerOnTouchDown(event);
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchRegionHandler == null) {
                    return false;
                }
                mTouchRegionHandler.onTouchMove(event);
                updateBackgroundRectF();
                postInvalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsInTouching = false;
                mTouchRegionHandler = null;
                postInvalidate();
                break;
        }
        return true;
    }


    private AbsTouchRegionHandler findTouchHandlerOnTouchDown(MotionEvent event) {

        float initX = event.getX();
        float initY = event.getY();
        AbsTouchRegionHandler handler = null;
        // 双指缩放
        if (event.getPointerCount() == 2) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.TWO_FINGER);
            // left center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top + (mCenterRectF.height() - mTriggerLength) / 2,
                mTouchEnlargeCheckRegion,
                mTriggerLength).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.LEFT);
            // bottom center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left + (mCenterRectF.width() - mTriggerLength) / 2,
                mCenterRectF.bottom - mTouchEnlargeCheckRegion / 2,
                mTriggerLength,
                mTouchEnlargeCheckRegion).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.BOTTOM);
            // right center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left + mCenterRectF.width() - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top + (mCenterRectF.height() - mTriggerLength) / 2,
                mTouchEnlargeCheckRegion,
                mTriggerLength).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.RIGHT);
            // top center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left + (mCenterRectF.width() - mTriggerLength) / 2,
                mCenterRectF.top - mTouchEnlargeCheckRegion / 2,
                mTriggerLength,
                mTouchEnlargeCheckRegion).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.TOP);
            // left top corner
        } else if (createRectF(mCenterRectF.left - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top - mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.LEFT_TOP);
            // left bottom
        } else if (createRectF(mCenterRectF.left - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.bottom - mTriggerLength / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.LEFT_BOTTOM);
            // right bottom
        } else if (createRectF(mCenterRectF.left + mCenterRectF.width() - mTriggerLength / 2,
                mCenterRectF.bottom - mTriggerLength / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.RIGHT_BOTTOM);
            // right top
        } else if (createRectF(mCenterRectF.left + mCenterRectF.width() - mTriggerLength / 2,
                mCenterRectF.top - mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.RIGHT_TOP);
        } else if (createRectF(mCenterRectF.left + mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top + mTouchEnlargeCheckRegion / 2,
                mCenterRectF.width() - mTouchEnlargeCheckRegion,
                mCenterRectF.height() - mTouchEnlargeCheckRegion).contains(initX, initY)) {
            handler = getTouchRegionHandler(AbsTouchRegionHandler.CENTER);
        }
        if (handler != null) {
            handler.onTouchDown(event);
        }
        return handler;
    }

    private RectF createRectF(float left, float top, float width, float height) {
        return mTestRectF = new RectF(left, top, left + width, top + height);
    }

    // 获取触摸处理器
    private AbsTouchRegionHandler getTouchRegionHandler(int touchRegion) {
        if (mHandlerMap == null) {
            mHandlerMap = new SparseArrayCompat<>();
        }
        AbsTouchRegionHandler handler = mHandlerMap.get(touchRegion);
        if (handler == null) {
            if (touchRegion == AbsTouchRegionHandler.TWO_FINGER) {
                handler = new TwoFingerHandler();
            } else if (touchRegion == AbsTouchRegionHandler.CENTER) {
                handler = new MoveHandler();
            } else if (hasNoAspectRatio()) {
                handler = new NoAspectRatioHandler();
            } else {
                handler = new AspectRatioHandler();
            }
            mHandlerMap.put(touchRegion, handler);
        }
        handler.init(touchRegion, mCenterRectF, mWidth, mHeight, mMinWidth, mMinHeight, mAspectRatio);
        return handler;
    }


    // 动画转为成目标矩形
    private void animToTargetRectF(final RectF targetRectF) {
        if (targetRectF == null)
            return;
        final RectF currentRectF = new RectF(mCenterRectF);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                mCenterRectF.set(currentRectF.left + (targetRectF.left - currentRectF.left) * progress,
                        currentRectF.top + (targetRectF.top - currentRectF.top) * progress,
                        currentRectF.right + (targetRectF.right - currentRectF.right) * progress,
                        currentRectF.bottom + (targetRectF.bottom - currentRectF.bottom) * progress);
                updateBackgroundRectF();
                postInvalidate();
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setEnabled(true);
                animation.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.setDuration(250);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
        setEnabled(false);
    }

    /**
     * 设置比例，使用 {@link #NO_ASPECT_RATIO} 表示没有设定比例的自由模式
     *
     * @param aspectRatio 比例,w/h的值
     */
    public void setAspectRatio(float aspectRatio) {
        RectF target;
        float left;
        float top;
        float width;
        float height;
        if (aspectRatio == NO_ASPECT_RATIO) {
            mAspectRatio = aspectRatio;
            width = mWidth * ORIGIN_SCALE_FACTOR;
            height = mHeight * ORIGIN_SCALE_FACTOR;
            left = (mWidth - width) / 2;
            top = (mHeight - height) / 2;
            target = new RectF(left, top, left + width, top + height);
        } else {
            if (aspectRatio <= 0)
                return;
            mAspectRatio = aspectRatio;
            if (mAspectRatio == 1) {
                width = height = Math.min(mWidth, mHeight) * ORIGIN_SCALE_FACTOR;
            } else if (mAspectRatio > 1) {
                width = (int) (mWidth * ORIGIN_SCALE_FACTOR);
                height = (int) (width / mAspectRatio);
            } else {
                height = (int) (mHeight * ORIGIN_SCALE_FACTOR);
                width = (int) (height * mAspectRatio);
            }
            left = (mWidth - width) / 2;
            top = (mHeight - height) / 2;
            target = new RectF(left, top, left + width, top + height);
        }
        animToTargetRectF(target);
    }

    /**
     * @param showGridIndicator 是否显示网格指示器
     */
    public void setShowGridIndicator(boolean showGridIndicator) {
        mIsShowGridIndicator = showGridIndicator;
        postInvalidate();
    }


    public void attachImage(String filePath, int maxWidth, int maxHeight, float scale, View... views) {
        reset();
        int width;
        int height;
        BitmapFactory.Options bitmapSize = BitmapUtils.getBitmapSize(filePath);
        int bitmapHeight = bitmapSize.outHeight;
        int bitmapWidth = bitmapSize.outWidth;
        if (bitmapWidth == bitmapHeight) {
            width = height = (int) (Math.min(maxWidth, maxHeight) * scale);
        } else if (bitmapWidth > bitmapHeight) {
            width = (int) (maxWidth * scale);
            height = (int) (width * (bitmapHeight * 1f / bitmapWidth));
        } else {
            height = (int) (maxHeight * scale);
            width = (int) (height * (bitmapWidth * 1f / bitmapHeight));
        }
        ViewUtils.setLayoutParam(width, height, views);
        ViewUtils.setLayoutParam(width, height, this);
    }

    public void attachImage(Bitmap bitmap, int maxWidth, int maxHeight, float scale, View... views) {
        reset();
        int width;
        int height;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        if (bitmapWidth == bitmapHeight) {
            width = height = (int) (Math.min(maxWidth, maxHeight) * scale);
        } else if (bitmapWidth > bitmapHeight) {
            width = (int) (maxWidth * scale);
            height = (int) (width * (bitmapHeight * 1f / bitmapWidth));
        } else {
            height = (int) (maxHeight * scale);
            width = (int) (height * (bitmapWidth * 1f / bitmapHeight));
        }
        ViewUtils.setLayoutParam(width, height, views);
        ViewUtils.setLayoutParam(width, height, this);

        LogUtils.e(TAG,"width = "+width + ",height = " + height);
    }

    public Rect getCropRect(int imageWidth, int imageHeight) {
        int left = (int) (imageWidth * (mCenterRectF.left / mWidth));
        int top = (int) (imageHeight * (mCenterRectF.top / mHeight));
        int right = (int) (imageWidth * (mCenterRectF.right / mWidth));
        int bottom = (int) (imageHeight * (mCenterRectF.bottom / mHeight));
        if (mAspectRatio == 1) {
            int size = right - left;
            bottom = top + size;
        }

        return new Rect(left, top, right, bottom);
    }


    public Bitmap crop(String filePath, Bitmap.Config config) {
        try {
            // 生成decoder对象
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filePath, true);
            final int imgWidth = decoder.getWidth();
            final int imgHeight = decoder.getHeight();
            Rect rect = getCropRect(imgWidth, imgHeight);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 为了内存考虑，将图片格式转化为RGB_565
            opts.inPreferredConfig = config;
            // 将矩形区域解码生成要加载的Bitmap对象
            return decoder.decodeRegion(rect, opts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
