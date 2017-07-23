package com.march.piceditor.sticker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.march.dev.utils.DrawUtils;
import com.march.dev.utils.LogUtils;
import com.march.piceditor.common.model.ClickChecker;
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
import java.util.Iterator;
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
    private Paint mLinePaint;

    private ClickChecker                               mClickChecker;
    private SparseArrayCompat<StickerBaseTouchHandler> mTouchHandlerMap;
    private StickerBaseTouchHandler                    mCurrentHandler;
    private List<Sticker>                              mStickers;

    private Sticker mActiveSticker;

    private StickerMenuHandler     mStickerMenuHandler;
    private OnStickerEventListener mOnStickerEventListener;


    private void init() {
        mStickerPaint = new Paint();
        DrawUtils.initAntiAliasPaint(mStickerPaint);
        mLinePaint = DrawUtils.newPaint(Color.WHITE, 4, Paint.Style.STROKE);
        mTouchHandlerMap = new SparseArrayCompat<>();
        mStickers = new ArrayList<>();
        mClickChecker = new ClickChecker();
    }

    /*
    scaleX skewX transX
    skewY scaleY transY
    persp0 persp1 persp2
     */
    private Matrix mAnimMatrix;

    /**
     * 添加贴纸
     *
     * @param sticker 贴纸
     */
    public void addSticker(Sticker sticker, boolean withAnim) {
        mStickers.add(sticker);
        if (withAnim)
            sticker.setInitScale(1);
        sticker.getMatrix().postScale(sticker.getInitScale(), sticker.getInitScale());
        if (sticker.getInitTranslateX() != 0 && sticker.getInitTranslateY() != 0)
            sticker.getMatrix().postTranslate(sticker.getInitTranslateX(), sticker.getInitTranslateY());
        activeOneSticker(mActiveSticker, sticker);
        postInvalidate();
        // 动画效果
        if (withAnim) {
            startAddStickerAnimation();
        }
    }


    // 添加贴纸时波动动画，未完成，现在只支持 scale = 1
    private void startAddStickerAnimation() {
        mAnimMatrix = new Matrix(mActiveSticker.getMatrix());
        final ValueAnimator scaleAnim = ValueAnimator.ofFloat(0, 0.15f, 0);
        scaleAnim.setDuration(700);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mActiveSticker != null && mAnimMatrix != null) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    mAnimMatrix.reset();
                    mAnimMatrix.setScale(mActiveSticker.getInitScale() + animatedValue,
                            mActiveSticker.getInitScale() + animatedValue,
                            mActiveSticker.getStickerImage().getWidth() / 2,
                            mActiveSticker.getStickerImage().getHeight() / 2);
                    mAnimMatrix.postTranslate(mActiveSticker.getInitTranslateX(),
                            mActiveSticker.getInitTranslateY());
                    postInvalidate();
                }
            }
        });
        scaleAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimMatrix = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimMatrix = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        scaleAnim.start();
    }


    /**
     * 真的删除贴纸
     *
     * @param sticker 贴纸
     */
    public void removeSticker(Sticker sticker) {
        sticker.destroy();
        Iterator<Sticker> iterator = mStickers.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(sticker)) {
                iterator.remove();
            }
        }
        postInvalidate();
    }

    public Sticker getActiveSticker() {
        return mActiveSticker;
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
//            for (int i = 0; i < mHeight / 10; i++) {
//                DrawUtils.drawHLine(canvas, mLinePaint, mHeight / 10 * i, 0, mWidth);
//            }
//            for (int i = 0; i < mWidth / 10; i++) {
//                DrawUtils.drawVLine(canvas, mLinePaint, mWidth / 10 * i, 0, mHeight);
//            }
            // 绘制贴纸
            for (Sticker sticker : mStickers) {
                if (sticker.isHidden())
                    continue;
                if (sticker.getStickerImage() != null && !sticker.getStickerImage().isRecycled()) {
                    mStickerPaint.setColorFilter(sticker.getColorFilter());
                    if (mAnimMatrix != null && mActiveSticker != null && mActiveSticker.equals(sticker)) {
                        canvas.drawBitmap(sticker.getStickerImage(), mAnimMatrix, mStickerPaint);
                    } else {
                        canvas.drawBitmap(sticker.getStickerImage(), sticker.getMatrix(), mStickerPaint);
                    }
                }
                if (mActiveSticker != null && mActiveSticker.equals(sticker)) {
                    Point[] points = sticker.getCornerPoints();
                    for (int j = 0, i; j < points.length; j++) {
                        i = j;
                        if (i == points.length - 1)
                            i = -1;
                        canvas.drawLine(points[j].x, points[j].y, points[i + 1].x, points[i + 1].y, mLinePaint);
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            int actionMasked = event.getActionMasked();
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    mClickChecker.onTouchDown(event);
                    mCurrentHandler = findTouchHandlerOnTouchDown(event);
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
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    // case MotionEvent.ACTION_POINTER_UP:
                    // case MotionEvent.ACTION_CANCEL:
                    // 如果是点击事件
                    if (mClickChecker.isClick(event)) {
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


    // 遍历查找应该激活的贴纸
    // isAutoLifting 怎么能让重叠的贴纸自动切换，检测时不检测当前贴纸，如果最后还是找到了，则将当前贴纸置于底层
    private void findActiveSticker(MotionEvent event) {
        // 将当前贴纸置空
        Sticker preSticker = mActiveSticker;
        if (preSticker != null)
            preSticker.setActive(false);
        boolean isAutoLifting = preSticker != null && preSticker.isAutoLifting();
        mActiveSticker = null;
        Sticker tempSticker;
        for (int i = mStickers.size() - 1; i >= 0; i--) {
            tempSticker = mStickers.get(i);
            if (tempSticker.isHidden())
                continue;
            boolean isCheckIn;
            if (isAutoLifting) {
                isCheckIn = !tempSticker.equals(preSticker) && tempSticker.isTouchIn(event.getX(), event.getY());
            } else {
                isCheckIn = tempSticker.isTouchIn(event.getX(), event.getY());
            }
            if (isCheckIn) {
                activeOneSticker(preSticker, tempSticker);
                break;
            }
        }
        // 自动提升时
        if (isAutoLifting) {
            // 找到了新的，将上一个置于底层
            if (mActiveSticker != null) {
                preSticker.bringBottomLayer();
                Collections.sort(mStickers);
            } else {
                // 如果没找到，检测是不是仍旧点击了当前，是的话重新激活当前
                if (preSticker.isTouchIn(event.getX(), event.getY())) {
                    activeOneSticker(preSticker, preSticker);
                }
            }
        }
        // 还是找不到，表示点击了空白位置
        if (mActiveSticker == null) {
            if (mOnStickerEventListener != null) {
                mOnStickerEventListener.OnEmptyAreaClick();
            }
        }
    }


    /**
     * 激活一张贴纸
     *
     * @param preSticker
     * @param sticker
     */
    private void activeOneSticker(Sticker preSticker, Sticker sticker) {
        if (preSticker != null) {
            preSticker.setActive(false);
        }
        mActiveSticker = sticker;
        mActiveSticker.bringTopLayer();
        mActiveSticker.setActive(true);
        Collections.sort(mStickers);
        if (mOnStickerEventListener != null) {
            mOnStickerEventListener.OnStickerSelect(preSticker, mActiveSticker);
        }
    }


    public void setStickerMenuHandler(StickerMenuHandler stickerMenuHandler) {
        mStickerMenuHandler = stickerMenuHandler;
    }

    public void setOnStickerEventListener(OnStickerEventListener onStickerEventListener) {
        mOnStickerEventListener = onStickerEventListener;
    }

    private boolean dispatchMenuClick(MotionEvent event) {
        if (mActiveSticker != null && mClickChecker.isClick(event)) {
            for (StickerMenu menu : mActiveSticker.getStickerMenus()) {
                if (menu != null && menu.isTouchIn(event.getX(), event.getY())) {
                    if (menu.getStickerMenuHandler() != null)
                        menu.getStickerMenuHandler().onMenuClick(this, mActiveSticker, menu);
                    else if (mStickerMenuHandler != null)
                        mStickerMenuHandler.onMenuClick(this, mActiveSticker, menu);
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
