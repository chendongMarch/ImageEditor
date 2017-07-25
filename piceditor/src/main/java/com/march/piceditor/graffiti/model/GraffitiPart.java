package com.march.piceditor.graffiti.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
public class GraffitiPart {

    private Path                      mPath; // 当前绘制的路径
    private int                       mPathWidth; // 路径的宽度
    private RectF                     mRectF;
    private GraffitiOverlay.TouchMode mTouchMode;


    public GraffitiPart(GraffitiOverlay.TouchMode touchMode) {
        mTouchMode = touchMode;
    }


    // down 时初始化
    public void onTouchDown(MotionEvent event, int width) {
        if (mTouchMode == GraffitiOverlay.TouchMode.PATH) {
            mPath = new Path();
            mPath.moveTo(event.getX(), event.getY());
            mPathWidth = width;
        } else if (mTouchMode == GraffitiOverlay.TouchMode.RECT) {
            mRectF = new RectF(event.getX(), event.getY(), event.getX(), event.getY());
        }
    }

    // 移动时记录
    public void onTouchMove(MotionEvent event) {
        if (mTouchMode == GraffitiOverlay.TouchMode.PATH) {
            mPath.lineTo(event.getX(), event.getY());
        } else if (mTouchMode == GraffitiOverlay.TouchMode.RECT) {
            mRectF.right = event.getX();
            mRectF.bottom = event.getY();
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
}
