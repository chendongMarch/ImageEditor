package com.march.piceditor.graffiti.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.march.piceditor.graffiti.GraffitiOverlay;

/**
 * CreateAt : 7/25/17
 * Describe :
 * <p>
 * 触摸之后保存触摸的位置，有两种类型
 * 1. 路径绘制(Path + PathWidth)
 * 2. 矩形区域绘制(Rect)
 *
 * @author chendong
 */
public class GraffitiPart implements Comparable<GraffitiPart> {

    private Path                      mPath; // 当前绘制的路径
    private int                       mPathWidth; // 路径的宽度
    private RectF                     mRectF;
    private GraffitiOverlay.TouchMode mTouchMode;
    private boolean                   mIsErase;
    private long                      mCreateTime;


    private float mRectInitX;
    private float mRectInitY;

    public GraffitiPart(GraffitiOverlay.TouchMode touchMode) {
        mTouchMode = touchMode;
    }

    public GraffitiPart(boolean isErase, GraffitiOverlay.TouchMode touchMode) {
        mTouchMode = touchMode;
        mIsErase = isErase;
    }

    // down 时初始化
    public void onTouchDown(MotionEvent event, int width) {
        if (mTouchMode == GraffitiOverlay.TouchMode.PATH) {
            mPath = new Path();
            mPath.moveTo(event.getX(), event.getY());
            mPathWidth = width;
        } else if (mTouchMode == GraffitiOverlay.TouchMode.RECT) {
            // mRectF = new RectF(event.getX(), event.getY(), event.getX(), event.getY());
            mRectF = new RectF();
            mRectInitX = event.getX();
            mRectInitY = event.getY();
            mRectF.set(mRectInitX, mRectInitY, mRectInitX, mRectInitY);
        }
        mCreateTime = System.currentTimeMillis();
    }

    // 移动时记录
    public void onTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (mTouchMode == GraffitiOverlay.TouchMode.PATH) {
            mPath.lineTo(x, y);
        } else if (mTouchMode == GraffitiOverlay.TouchMode.RECT) {
//            mRectF.right = event.getX();
//            mRectF.bottom = event.getY();
            if (x <= mRectInitX) {
                if(y <= mRectInitY){
                    // 第1象限
                    mRectF.set(x,y,mRectInitX,mRectInitY);
                }else {
                    // 第3象限
                    mRectF.set(x,mRectInitY,mRectInitX,y);
                }
            } else{
                if(y <= mRectInitY){
                    // 第2象限
                    mRectF.set(mRectInitX,y,x,mRectInitY);
                }else {
                    // 第4象限
                    mRectF.set(mRectInitX,mRectInitY,x,y);
                }
            }
        }
    }

    // 绘制
    public void onDraw(Canvas canvas, Paint paint) {
        if (mPath != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(mPathWidth);
            canvas.drawPath(mPath, paint);
        } else if (mRectF != null) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(mRectF, paint);
        }
    }


    public Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public int getPathWidth() {
        return mPathWidth;
    }

    public void setPathWidth(int pathWidth) {
        mPathWidth = pathWidth;
    }

    public RectF getRectF() {
        return mRectF;
    }

    public void setRectF(RectF rectF) {
        mRectF = rectF;
    }

    public boolean isErase() {
        return mIsErase;
    }

    public void setErase(boolean erase) {
        mIsErase = erase;
    }

    @Override
    public String toString() {
        return "GraffitiPart{" +
                ", mTouchMode=" + mTouchMode +
                ", mIsErase=" + mIsErase +
                ", mCreateTime=" + mCreateTime +
                '}';
    }

    @Override
    public int compareTo(@NonNull GraffitiPart o) {
        if (mCreateTime > o.mCreateTime) {
            return 1;
        } else if (mCreateTime < o.mCreateTime) {
            return -1;
        } else return 0;
    }
}
