package com.march.piceditor.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.march.dev.utils.DrawUtils;
import com.march.dev.utils.LogUtils;
import com.march.piceditor.common.model.Point;
import com.march.piceditor.sticker.handler.StickerBaseTouchHandler;
import com.march.piceditor.sticker.handler.impl.BottomRightCornerHandler;
import com.march.piceditor.sticker.handler.impl.MoveHandler;
import com.march.piceditor.sticker.handler.impl.TwoFingersHandler;
import com.march.piceditor.sticker.model.Sticker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CreateAt : 7/20/17
 * Describe :
 *
 * @author chendong
 */
public class StickerDrawOverlay extends View {
    public static final String TAG = StickerDrawOverlay.class.getSimpleName();


    public StickerDrawOverlay(Context context) {
        this(context, null);
    }

    public StickerDrawOverlay(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerDrawOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStickerPaint = new Paint();
        DrawUtils.initAntiAliasPaint(mStickerPaint);
        mPaintLine = DrawUtils.newPaint(Color.WHITE, 2, Paint.Style.STROKE);
        mTouchHandlerMap = new SparseArrayCompat<>();
        mStickers = new ArrayList<>();

        mStickers.add(new Sticker(getContext()));
        mStickers.add(new Sticker(getContext()));
        mStickers.add(new Sticker(getContext()));
        mStickers.add(new Sticker(getContext()));
        mStickers.add(new Sticker(getContext()));
    }

    private int mWidth, mHeight;
    private Paint mStickerPaint;
    private Paint mPaintLine;

    private SparseArrayCompat<StickerBaseTouchHandler> mTouchHandlerMap;
    private StickerBaseTouchHandler                    mCurrentHandler;

    private List<Sticker> mStickers;

    private Sticker mActiveSticker;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {


            for (Sticker sticker : mStickers) {
                canvas.drawBitmap(sticker.getBitmap(), sticker.getMatrix(), mStickerPaint);

                if (mActiveSticker != null && mActiveSticker.equals(sticker)) {
//
                    sticker.mapPoints();
                    canvas.drawLine(sticker.getTopLeftPoint().x, sticker.getTopLeftPoint().y,
                            sticker.getTopRightPoint().x, sticker.getTopRightPoint().y, mPaintLine);
                    canvas.drawLine(sticker.getTopRightPoint().x, sticker.getTopRightPoint().y,
                            sticker.getBottomRightPoint().x, sticker.getBottomRightPoint().y, mPaintLine);
                    canvas.drawLine(sticker.getBottomRightPoint().x, sticker.getBottomRightPoint().y,
                            sticker.getBottomLeftPoint().x, sticker.getBottomLeftPoint().y, mPaintLine);
                    canvas.drawLine(sticker.getBottomLeftPoint().x, sticker.getBottomLeftPoint().y,
                            sticker.getTopLeftPoint().x, sticker.getTopLeftPoint().y, mPaintLine);

//          canvas.drawCircle(x2, y2, 40, mPaintLine);
                }
            }


//
//        for (int i = 0; i < mHeight / 10; i++) {
//            DrawUtils.drawHLine(canvas, mPaintLine, mHeight / 10 * i, 0, mWidth);
//        }
//
//        for (int i = 0; i < mWidth / 10; i++) {
//            DrawUtils.drawVLine(canvas, mPaintLine, mWidth / 10 * i, 0, mHeight);
//        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean mIsMoved = false;

    // 时间小于<400 移动距离<

    private long  mLastDownTime;
    private Point mLastDownPoint;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {

            int actionMasked = event.getActionMasked();

            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    mLastDownTime = System.currentTimeMillis();
                    mLastDownPoint = new Point(event.getX(), event.getY());
                    mIsMoved = false;
                    mCurrentHandler = findTouchHandlerOnTouchDown(event);
                    LogUtils.e(TAG, "action_down - " + (mCurrentHandler == null ? "没有处理者" : mCurrentHandler.getClass().getSimpleName()));
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mCurrentHandler != null) {
                        mCurrentHandler.onTouchMove(event);
                    }
                    // LogUtils.e(TAG, "action_move - " + (mCurrentHandler == null ? "没有处理者" : mCurrentHandler.getClass().getSimpleName()));
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mCurrentHandler = findTouchHandlerOnTouchDown(event);
                    LogUtils.e(TAG, "action_pointer_down - " + (mCurrentHandler == null ? "没有处理者" : mCurrentHandler.getClass().getSimpleName()));
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    // case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    LogUtils.e(TAG, "action_up - " + (mCurrentHandler == null ? "没有处理者" : mCurrentHandler.getClass().getSimpleName()));
                    // 没有事件处理，此时要查找触摸的地方是不是有新的贴纸
                    if (isClick(event)|| mCurrentHandler == null) {
                        findActiveSticker(event);
                    }
                    mCurrentHandler = null;
                    postInvalidate();
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isClick(MotionEvent event) {
        long diffTime = System.currentTimeMillis() - mLastDownTime;
        float diffX = event.getX() - mLastDownPoint.x;
        float diffY = event.getY() - mLastDownPoint.y;
        LogUtils.e(TAG, diffTime + " " + diffX + " " + diffY);
        return diffTime < 400 && Math.abs(diffX) < 50 && Math.abs(diffY) < 50;
    }

    // 便利查找应该激活的贴纸
    private void findActiveSticker(MotionEvent event) {
        if (mActiveSticker != null) {
            mActiveSticker.setActive(false);
            mActiveSticker = null;
        }
        for (Sticker sticker : mStickers) {
            if (sticker.isTouchIn(event.getX(), event.getY())) {
                mActiveSticker = sticker;
                mActiveSticker.setActive(true);
                mActiveSticker.updatePriority();
                Collections.sort(mStickers);
                break;
            }
        }
    }

    // mode1
    // 单指，中间位置，激活，移动
    // 双指，缩放激活的哪个
    // 右下角，旋转缩放

    // mode2
    // 单指，中间位置，激活
    // 单指任何位置，移动激活那个
    // 双指，缩放旋转激活的那个
    // 右下角，旋转缩放
    private StickerBaseTouchHandler findTouchHandlerOnTouchDown(MotionEvent event) {
        StickerBaseTouchHandler handler;
        if (mActiveSticker != null) {
            // 先检测右下角
            // 检测手指数
            int touchType = -1;
            if (event.getPointerCount() == 2) {
                touchType = StickerBaseTouchHandler.TWO_FINGER;
            } else {
                touchType = StickerBaseTouchHandler.MOVE;
            }
            handler = getTouchHandler(touchType);
            if (handler != null) {
                handler.init(mActiveSticker);
                handler.onTouchDown(event);
            }
            return handler;
        }
        return null;
    }

    private StickerBaseTouchHandler getTouchHandler(int touchType) {
        StickerBaseTouchHandler handler = mTouchHandlerMap.get(touchType);
        if (handler == null) {
            switch (touchType) {
                case StickerBaseTouchHandler.MOVE:
                    handler = new MoveHandler();
                    break;
                case StickerBaseTouchHandler.RIGHT_ANCHOR:
                    handler = new BottomRightCornerHandler();
                    break;
                case StickerBaseTouchHandler.TWO_FINGER:
                    handler = new TwoFingersHandler();
                    break;
            }
        }
        return handler;
    }

}
