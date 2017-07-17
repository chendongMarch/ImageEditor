package com.march.picedit.crop;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.march.dev.utils.DrawUtils;
import com.march.dev.utils.LogUtils;

import java.util.Locale;

/**
 * CreateAt : 7/15/17
 * Describe :
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
    private float mInitX       = INVALID_VALUE; // 按压位置记录
    private float mInitY       = INVALID_VALUE; // 按压位置记录

    private SparseArrayCompat<TouchRegionHandler> mHandlerMap; // 触摸处理类的管理，避免多次初始化
    private TouchRegionHandler                    mTouchRegionHandler; // 触摸处理


    private void init() {
        mBgPaint = DrawUtils.newPaint(Color.parseColor("#2f000000"), 0, Paint.Style.FILL_AND_STROKE);
        mTriggerPaint = DrawUtils.newPaint(Color.WHITE, mTriggerLineStrokeWidth, Paint.Style.STROKE);
        mIndicatorLinePaint = DrawUtils.newPaint(Color.WHITE, mIndicatorLineStrokeWidth, Paint.Style.STROKE);
        mTextPaint = DrawUtils.newPaint(Color.WHITE, 2, Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(40);

        setShowGridIndicator(true);
//        postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setAspectRatio(9 / 16f);
//            }
//        }, 1000);
//
//        postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setAspectRatio(4f / 3);
//            }
//        }, 2000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();

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
        String msg = String.format(Locale.CHINA, "%.0fX%.0f", mCenterRectF.width(), mCenterRectF.height());
//        String msg = String.format(Locale.CHINA, "%.0fX%.0f/%.2f", mCenterRectF.width(), mCenterRectF.height(), mCenterRectF.width() / mCenterRectF.height());
        canvas.drawText(msg, mCenterRectF.left + mCenterRectF.width() / 2, mCenterRectF.top + mCenterRectF.height() / 2 + DrawUtils.measureTextHeight(mTextPaint) / 2, mTextPaint);

        if (mTestRectF != null)
            canvas.drawRect(mTestRectF, DrawUtils.newPaint(Color.RED, 5, Paint.Style.STROKE));
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
                mInitX = event.getX();
                mInitY = event.getY();
                mTouchRegionHandler = findTouchHandler(mInitX, mInitY);
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mInitX == INVALID_VALUE
                        || mInitY == INVALID_VALUE
                        || mTouchRegionHandler == null) {
                    return false;
                }
                float diffX = event.getX() - mInitX;
                float diffY = event.getY() - mInitY;
                mInitX = event.getX();
                mInitY = event.getY();
                mTouchRegionHandler.handleTouch(diffX, diffY);
                updateBackgroundRectF();
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsInTouching = false;
                mInitX = INVALID_VALUE;
                mInitX = INVALID_VALUE;
                mTouchRegionHandler = null;
                postInvalidate();
                break;
        }
        return true;
    }

    public interface TouchRegionHandler {

        int LEFT   = 0;
        int TOP    = 1;
        int RIGHT  = 2;
        int BOTTOM = 3;
        int CENTER = 4;

        int LEFT_TOP     = 5;
        int LEFT_BOTTOM  = 6;
        int RIGHT_TOP    = 7;
        int RIGHT_BOTTOM = 8;

        void handleTouch(float diffX, float diffY);
    }


    private TouchRegionHandler findTouchHandler(float initX, float initY) {

        // left center
        if (hasNoAspectRatio() && createRectF(mCenterRectF.left - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top + (mCenterRectF.height() - mTriggerLength) / 2,
                mTouchEnlargeCheckRegion,
                mTriggerLength).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.LEFT);
            // bottom center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left + (mCenterRectF.width() - mTriggerLength) / 2,
                mCenterRectF.bottom - mTouchEnlargeCheckRegion / 2,
                mTriggerLength,
                mTouchEnlargeCheckRegion).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.BOTTOM);
            // right center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left + mCenterRectF.width() - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top + (mCenterRectF.height() - mTriggerLength) / 2,
                mTouchEnlargeCheckRegion,
                mTriggerLength).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.RIGHT);
            // top center
        } else if (hasNoAspectRatio() && createRectF(mCenterRectF.left + (mCenterRectF.width() - mTriggerLength) / 2,
                mCenterRectF.top - mTouchEnlargeCheckRegion / 2,
                mTriggerLength,
                mTouchEnlargeCheckRegion).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.TOP);
            // left top corner
        } else if (createRectF(mCenterRectF.left - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top - mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.LEFT_TOP);
            // left bottom
        } else if (createRectF(mCenterRectF.left - mTouchEnlargeCheckRegion / 2,
                mCenterRectF.bottom - mTriggerLength / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.LEFT_BOTTOM);
            // right bottom
        } else if (createRectF(mCenterRectF.left + mCenterRectF.width() - mTriggerLength / 2,
                mCenterRectF.bottom - mTriggerLength / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.RIGHT_BOTTOM);
            // right top
        } else if (createRectF(mCenterRectF.left + mCenterRectF.width() - mTriggerLength / 2,
                mCenterRectF.top - mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2,
                mTriggerLength / 2 + mTouchEnlargeCheckRegion / 2).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.RIGHT_TOP);
        } else if (createRectF(mCenterRectF.left + mTouchEnlargeCheckRegion / 2,
                mCenterRectF.top + mTouchEnlargeCheckRegion / 2,
                mCenterRectF.width() - mTouchEnlargeCheckRegion,
                mCenterRectF.height() - mTouchEnlargeCheckRegion).contains(initX, initY)) {
            return getTouchRegionHandler(TouchRegionHandler.CENTER);
        }
        return null;
    }


    private RectF createRectF(float left, float top, float width, float height) {
        return mTestRectF = new RectF(left, top, left + width, top + height);
    }


    // 获取触摸处理器
    private TouchRegionHandler getTouchRegionHandler(int touchRegion) {
        if (mHandlerMap == null) {
            mHandlerMap = new SparseArrayCompat<>();
        }
        TouchRegionHandler handler = mHandlerMap.get(touchRegion);
        if (handler == null) {
            handler = new TouchRegionHandlerImpl(touchRegion);
            mHandlerMap.put(touchRegion, handler);
        }
        return handler;
    }


    //  触摸处理器的实现
    public class TouchRegionHandlerImpl implements TouchRegionHandler {

        private int touchRegion;

        public TouchRegionHandlerImpl(int touchRegion) {
            this.touchRegion = touchRegion;
        }


        // 没有强制比例时的处理，此时会触发 edge trigger
        private void handleNoAspectRatioTriggerScale(float diffX, float diffY) {
            float newLeft = Math.min(mCenterRectF.right - mMinWidth, Math.max(0, mCenterRectF.left + diffX));
            float newTop = Math.min(mCenterRectF.bottom - mMinHeight, Math.max(0, mCenterRectF.top + diffY));
            float newRight = Math.max(mCenterRectF.left + mMinWidth, Math.min(mWidth, mCenterRectF.right + diffX));
            float newBottom = Math.max(mCenterRectF.top + mMinHeight, Math.min(mHeight, mCenterRectF.bottom + diffY));

            switch (touchRegion) {
                case LEFT:
                    mCenterRectF.left = newLeft;
                    break;
                case RIGHT:
                    mCenterRectF.right = newRight;
                    break;
                case TOP:
                    mCenterRectF.top = newTop;
                    break;
                case BOTTOM:
                    mCenterRectF.bottom = newBottom;
                    break;
                case LEFT_TOP:
                    mCenterRectF.top = newTop;
                    mCenterRectF.left = newLeft;
                    break;
                case LEFT_BOTTOM:
                    mCenterRectF.left = newLeft;
                    mCenterRectF.bottom = newBottom;
                    break;
                case RIGHT_TOP:
                    mCenterRectF.right = newRight;
                    mCenterRectF.top = newTop;
                    break;
                case RIGHT_BOTTOM:
                    mCenterRectF.right = newRight;
                    mCenterRectF.bottom = newBottom;
                    break;
            }
        }

        // 强制比例时的处理，此时不会触发 edge trigger
        private void handleAspectRatioTriggerScale(float diffX, float diffY) {

            float aspectNewTop;
            float aspectNewLeft;
            float aspectNewRight;
            float aspectNewBottom;

            switch (touchRegion) {
                case LEFT_TOP:
//                    aspectNewTop = Math.min(mCenterRectF.bottom - mMinHeight, mCenterRectF.top + (diffX + diffY) / 2);
//                    aspectNewLeft = Math.min(mCenterRectF.right - mMinWidth, mCenterRectF.left + ((diffX + diffY) / 2) * mAspectRatio);
                    aspectNewTop = mCenterRectF.top + ((diffX + diffY) / 2);
                    aspectNewLeft = mCenterRectF.left + ((diffX + diffY) / 2) * mAspectRatio;
                    if (aspectNewTop <= (mCenterRectF.bottom - mMinHeight)
                            && aspectNewLeft <= (mCenterRectF.right - mMinWidth)) {
                        LogUtils.e(TAG, aspectNewTop + "," + aspectNewLeft);
                        if (aspectNewTop >= 0 && aspectNewLeft >= 0) {
                            mCenterRectF.top = aspectNewTop;
                            mCenterRectF.left = aspectNewLeft;
                        }
                    }
                    break;
                case LEFT_BOTTOM:
                    aspectNewLeft = mCenterRectF.left + ((diffX - diffY) / 2) * mAspectRatio;
                    aspectNewBottom = mCenterRectF.bottom + (-diffX + diffY) / 2;
                    if (aspectNewLeft <= (mCenterRectF.right - mMinWidth)
                            && aspectNewBottom >= mCenterRectF.top + mMinHeight) {
                        if (aspectNewLeft >= 0 && aspectNewBottom <= mHeight) {
                            mCenterRectF.left = aspectNewLeft;
                            mCenterRectF.bottom = aspectNewBottom;
                        }
                    }
                    break;
                case RIGHT_TOP:
                    aspectNewRight = mCenterRectF.right + ((diffX - diffY) / 2) * mAspectRatio;
                    aspectNewTop = mCenterRectF.top + (-diffX + diffY) / 2;
                    if (aspectNewRight >= mCenterRectF.left + mMinWidth
                            && aspectNewTop <= mCenterRectF.bottom - mMinHeight) {
                        if (aspectNewRight <= mWidth && aspectNewTop >= 0) {
                            mCenterRectF.right = aspectNewRight;
                            mCenterRectF.top = aspectNewTop;
                        }
                    }
                    break;
                case RIGHT_BOTTOM:
                    aspectNewRight = mCenterRectF.right + ((diffX + diffY) / 2) * mAspectRatio;
                    aspectNewBottom = mCenterRectF.bottom + (diffX + diffY) / 2;
                    if (aspectNewRight >= mCenterRectF.left + mMinWidth
                            && aspectNewBottom >= (mCenterRectF.top + mMinHeight)) {
                        if (aspectNewRight <= mWidth && aspectNewBottom <= mHeight) {
                            mCenterRectF.right = aspectNewRight;
                            mCenterRectF.bottom = aspectNewBottom;
                        }
                    }
                    break;
            }
        }

        // 中间移动处理
        private void handleCenterTouchMove(float diffX, float diffY) {
            if (touchRegion == CENTER) {
                float newLeft = Math.max(0, mCenterRectF.left + diffX);
                float newTop = Math.max(0, mCenterRectF.top + diffY);

                if (newLeft + mCenterRectF.width() >= mWidth) {
                    newLeft = mCenterRectF.left;
                }
                if (newTop + mCenterRectF.height() >= mHeight) {
                    newTop = mCenterRectF.top;
                }
                RectF temp = new RectF(newLeft, newTop, newLeft + mCenterRectF.width(), newTop + mCenterRectF.height());
                mCenterRectF.set(temp);
            }
        }

        @Override
        public void handleTouch(float diffX, float diffY) {
            if (touchRegion == CENTER) {
                handleCenterTouchMove(diffX, diffY);
            } else if (hasNoAspectRatio()) {
                handleNoAspectRatioTriggerScale(diffX, diffY);
            } else {
                handleAspectRatioTriggerScale(diffX, diffY);
            }
        }
    }

    // 动画转为成目标矩形
    private void animToTargetRectF(final RectF targetRectF) {
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
        valueAnimator.setDuration(300);
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
        if (aspectRatio <= 0)
            return;
        mAspectRatio = aspectRatio;
        // w/h
        int width;
        int height;
        if (mAspectRatio > 1) {
            width = (int) (mWidth * ORIGIN_SCALE_FACTOR);
            height = (int) (width / mAspectRatio);
        } else {
            height = (int) (mHeight * ORIGIN_SCALE_FACTOR);
            width = (int) (height * mAspectRatio);
        }
        int left = (mWidth - width) / 2;
        int top = (mHeight - height) / 2;
        RectF target = new RectF(left, top, left + width, top + height);
        animToTargetRectF(target);
    }

    /**
     * @param showGridIndicator 是否显示网格指示器
     */
    public void setShowGridIndicator(boolean showGridIndicator) {
        mIsShowGridIndicator = showGridIndicator;
        postInvalidate();
    }
}
