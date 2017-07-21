package com.march.piceditor.sticker.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.march.piceditor.common.model.Point;
import com.march.piceditor.utils.CalculateUtils;

import java.util.Random;

/**
 * CreateAt : 7/20/17
 * Describe : 贴纸类
 *
 * @author chendong
 */
public class Sticker implements Comparable<Sticker> {

    private Bitmap  mBitmap;
    private Matrix  mMatrix;
    private RectF   mRectF;
    private boolean mIsActive;
    private boolean mDelete;
    private long    mPriority;

    private SparseArray<Point>       mPointMap;
    private SparseArray<StickerMenu> mMenuMap;


    public Sticker(Context context) {
        mBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.mipmap.sym_def_app_icon);
        init();
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean active) {
        mIsActive = active;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void addStickerMenu(StickerMenu... stickerMenu) {
        for (StickerMenu menu : stickerMenu) {
            menu.attachSticker(this);
            mMenuMap.put(menu.getPositionType(), menu);
        }
    }

    public StickerMenu[] getStickerMenus() {
        StickerMenu[] stickerMenus = new StickerMenu[mMenuMap.size()];
        for (int i = 0; i < mMenuMap.size(); i++) {
            stickerMenus[i] = mMenuMap.valueAt(i);
        }
        return stickerMenus;
    }

    public Point[] getPoints() {
        mapPoints();
        return new Point[]{
                mPointMap.get(Position.TOP_LEFT),
                mPointMap.get(Position.TOP_RIGHT),
                mPointMap.get(Position.BOTTOM_RIGHT),
                mPointMap.get(Position.BOTTOM_LEFT)
        };
    }

    public boolean isDelete() {
        return mDelete;
    }

    public void setDelete(boolean delete) {
        mDelete = delete;
    }

    private void init() {
        mMatrix = new Matrix();
        mMatrix.postTranslate(new Random().nextInt(450), new Random().nextInt(450));
        mMatrix.postScale(1.5f, 1.5f);
        mRectF = new RectF();
        mPriority = System.currentTimeMillis();
        mPointMap = new SparseArray<>();
        mMenuMap = new SparseArray<>();
        mPointMap.put(Position.TOP_LEFT, new Point());
        mPointMap.put(Position.TOP_RIGHT, new Point());
        mPointMap.put(Position.BOTTOM_RIGHT, new Point());
        mPointMap.put(Position.BOTTOM_LEFT, new Point());
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public void updatePriority() {
        mPriority = System.currentTimeMillis();
    }

    public RectF getRectF() {
        mRectF.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mMatrix.mapRect(mRectF);
        return mRectF;
    }

    private void mapPoints() {

        float[] dst = new float[8];

        float[] src = new float[]{
                0, 0, mBitmap.getWidth(),
                0, 0, mBitmap.getHeight(),
                mBitmap.getWidth(), mBitmap.getHeight()};

        mMatrix.mapPoints(dst, src);

        mPointMap.get(Position.TOP_LEFT).set(dst[0], dst[1]);
        mPointMap.get(Position.TOP_RIGHT).set(dst[2], dst[3]);
        mPointMap.get(Position.BOTTOM_LEFT).set(dst[4], dst[5]);
        mPointMap.get(Position.BOTTOM_RIGHT).set(dst[6], dst[7]);
    }

    public SparseArray<Point> getPointMap() {
        return mPointMap;
    }

    public SparseArray<StickerMenu> getMenuMap() {
        return mMenuMap;
    }

    public boolean isTouchIn(float x, float y) {
        Point[] points = getPoints();
        return CalculateUtils.isRectContainsPoint(points[0],
                points[1], points[2],
                points[3], new Point(x, y));
    }

    @Override
    public int compareTo(@NonNull Sticker o) {
        if (mPriority > o.mPriority) {
            return 1;
        } else if (mPriority < o.mPriority) {
            return -1;
        } else {
            return 0;
        }
    }


    // matrix 辅助
    public void postMatrixScale(float sx, float sy) {
        RectF rectF = getRectF();
        float cy = rectF.centerY();
        float cx = rectF.centerX();
        getMatrix().postScale(sx, sy, cx, cy);
    }

    public void postMatrixRotate(int rotation) {
        RectF rectF = getRectF();
        float cy = rectF.centerY();
        float cx = rectF.centerX();
        getMatrix().postRotate(rotation, cx, cy);

    }
}
