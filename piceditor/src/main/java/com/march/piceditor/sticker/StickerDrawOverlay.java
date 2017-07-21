package com.march.piceditor.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
import com.march.piceditor.sticker.listener.OnStickerEventListener;
import com.march.piceditor.sticker.listener.StickerMenuHandler;
import com.march.piceditor.sticker.model.Position;
import com.march.piceditor.sticker.model.Sticker;
import com.march.piceditor.sticker.model.StickerMenu;

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


    private int mWidth, mHeight;
    private Paint mStickerPaint;
    private Paint mPaintLine;

    private SparseArrayCompat<StickerBaseTouchHandler> mTouchHandlerMap;
    private StickerBaseTouchHandler                    mCurrentHandler;
    private SparseArrayCompat<Drawable>                mMenuIconMap;
    private List<Sticker>                              mStickers;

    private Sticker mActiveSticker;


    private void init() {
        mStickerPaint = new Paint();
        DrawUtils.initAntiAliasPaint(mStickerPaint);
        mPaintLine = DrawUtils.newPaint(Color.WHITE, 2, Paint.Style.STROKE);
        mTouchHandlerMap = new SparseArrayCompat<>();
        mStickers = new ArrayList<>();
        mMenuIconMap = new SparseArrayCompat<>();
    }

    /**
     * 添加贴纸
     *
     * @param sticker 贴纸
     */
    public void addSticker(Sticker sticker) {
        mStickers.add(sticker);
    }


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
            for (int i = 0; i < mHeight / 10; i++) {
                DrawUtils.drawHLine(canvas, mPaintLine, mHeight / 10 * i, 0, mWidth);
            }
            for (int i = 0; i < mWidth / 10; i++) {
                DrawUtils.drawVLine(canvas, mPaintLine, mWidth / 10 * i, 0, mHeight);
            }

            // 绘制贴纸
            for (Sticker sticker : mStickers) {
                if (sticker.isDelete())
                    continue;
                if (sticker.getBitmap() != null && !sticker.getBitmap().isRecycled()) {
                    canvas.drawBitmap(sticker.getBitmap(), sticker.getMatrix(), mStickerPaint);
                }
                if (mActiveSticker != null && mActiveSticker.equals(sticker)) {
                    Point[] points = sticker.getPoints();
                    for (int j = 0, i; j < points.length; j++) {
                        i = j;
                        if (i == points.length - 1)
                            i = -1;
                        canvas.drawLine(points[j].x, points[j].y, points[i + 1].x, points[i + 1].y, mPaintLine);
                    }
                    for (StickerMenu stickerMenu : sticker.getStickerMenus()) {
                        if (stickerMenu != null) {
                            stickerMenu.updateBounds();
                            stickerMenu.getMenuIcon().draw(canvas);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    mCurrentHandler = findTouchHandlerOnTouchDown(event);
                    LogUtils.e(TAG, "action_down - " + (mCurrentHandler == null ? "没有处理者" : mCurrentHandler.getClass().getSimpleName()));
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mCurrentHandler != null) {
                        mCurrentHandler.onTouchMove(event);
                    }
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
                    // 如果是点击事件
                    if (isClick(event)) {
                        // 分发菜单点击事件
                        if (!dispatchMenuClick(event)) {
                            // 不是菜单点击的话， 查看是不是选择了新的贴纸
                            findActiveSticker(event);
                        }
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
        // 将当前贴纸置空
        Sticker preSticker = mActiveSticker;
        if (preSticker != null)
            preSticker.setActive(false);
        mActiveSticker = null;
        Sticker tempSticker;
        for (int i = mStickers.size() - 1; i >= 0; i--) {
            tempSticker = mStickers.get(i);
            if (tempSticker.isDelete())
                continue;
            if (tempSticker.isTouchIn(event.getX(), event.getY())) {
                mActiveSticker = tempSticker;
                mActiveSticker.updatePriority();
                mActiveSticker.setActive(true);
                Collections.sort(mStickers);
                if (mOnStickerEventListener != null) {
                    mOnStickerEventListener.OnStickerSelect(preSticker, mActiveSticker);
                }
                break;
            }
        }
        if (mActiveSticker == null) {
            if (mOnStickerEventListener != null) {
                mOnStickerEventListener.OnEmptyAreaClick();
            }
        }
    }

    private StickerMenuHandler     mStickerMenuHandler;
    private OnStickerEventListener mOnStickerEventListener;

    public void setStickerMenuHandler(StickerMenuHandler stickerMenuHandler) {
        mStickerMenuHandler = stickerMenuHandler;
    }

    public void setOnStickerEventListener(OnStickerEventListener onStickerEventListener) {
        mOnStickerEventListener = onStickerEventListener;
    }

    private boolean dispatchMenuClick(MotionEvent event) {
        if (mActiveSticker != null && isClick(event)) {
            for (StickerMenu menu : mActiveSticker.getStickerMenus()) {
                if (menu != null && menu.isTouchIn(event.getX(), event.getY())) {
                    if (menu.getStickerMenuHandler() != null)
                        menu.getStickerMenuHandler().onMenuClick(mActiveSticker, menu);
                    else if (mStickerMenuHandler != null)
                        mStickerMenuHandler.onMenuClick(mActiveSticker, menu);
                    return true;
                }
            }
        }
        return false;
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
            int touchType;
            if (event.getPointerCount() == 2) {
                // 双指缩放
                touchType = StickerBaseTouchHandler.TWO_FINGER;
            } else {
                StickerMenu menu = mActiveSticker.getMenuMap().get(Position.BOTTOM_RIGHT);
                if (menu != null && menu.isTouchIn(event.getX(), event.getY())) {
                    touchType = StickerBaseTouchHandler.BOTTOM_RIGHT_CORNER;
                } else {
                    touchType = StickerBaseTouchHandler.MOVE;
                }
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
                case StickerBaseTouchHandler.BOTTOM_RIGHT_CORNER:
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
