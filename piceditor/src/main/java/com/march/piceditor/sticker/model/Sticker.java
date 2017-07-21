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

import java.util.HashMap;
import java.util.Map;
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
    private long    mPriority;

    private Point mTopLeftPoint;
    private Point mTopRightPoint;
    private Point mBottomLeftPoint;
    private Point mBottomRightPoint;

    private StickerMenu mTopLeftMenu;
    private StickerMenu mTopRightMenu;
    private StickerMenu mBottomLeftMenu;
    private StickerMenu mBottomRightMenu;

    private Map<Integer, Point>      mPointMap;
    private SparseArray<StickerMenu> mMenuMap;


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


    public void addStickerMenu(StickerMenu stickerMenu) {
        stickerMenu.attachSticker(this);
        mMenuMap.put(stickerMenu.getPositionType(), stickerMenu);
        switch (stickerMenu.getPositionType()) {
            case Position.TOP_LEFT:
                mTopLeftMenu = stickerMenu;
                break;
            case Position.TOP_RIGHT:
                mTopRightMenu = stickerMenu;
                break;
            case Position.BOTTOM_RIGHT:
                mBottomRightMenu = stickerMenu;
                break;
            case Position.BOTTOM_LEFT:
                mBottomLeftMenu = stickerMenu;
                break;
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
        return new Point[]{mTopLeftPoint, mTopRightPoint, mBottomRightPoint, mBottomLeftPoint};
    }

    public Sticker(Context context) {
        mBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.mipmap.sym_def_app_icon);
        init();
    }


    private void init() {
        mMatrix = new Matrix();
        mMatrix.postTranslate(new Random().nextInt(450), new Random().nextInt(450));
        mMatrix.postScale(1.5f, 1.5f);
        mRectF = new RectF();
        mPriority = System.currentTimeMillis();
        mTopLeftPoint = new Point();
        mTopRightPoint = new Point();
        mBottomLeftPoint = new Point();
        mBottomRightPoint = new Point();
        mPointMap = new HashMap<>();
        mMenuMap = new SparseArray<>();
    }

    public void updatePriority() {
        mPriority = System.currentTimeMillis();
    }

    public RectF getRectF() {
        mRectF.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mMatrix.mapRect(mRectF);
        return mRectF;
    }

    public void mapPoints() {
        float[] dst = new float[8];
        float[] src = new float[]{
                0, 0, mBitmap.getWidth(),
                0, 0, mBitmap.getHeight(),
                mBitmap.getWidth(), mBitmap.getHeight()};
        mMatrix.mapPoints(dst, src);
        mTopLeftPoint.set(dst[0], dst[1]);
        mTopRightPoint.set(dst[2], dst[3]);
        mBottomLeftPoint.set(dst[4], dst[5]);
        mBottomRightPoint.set(dst[6], dst[7]);
    }

    public Point getTopLeftPoint() {
        return mTopLeftPoint;
    }

    public Point getTopRightPoint() {
        return mTopRightPoint;
    }

    public Point getBottomLeftPoint() {
        return mBottomLeftPoint;
    }

    public Point getBottomRightPoint() {
        return mBottomRightPoint;
    }

    public StickerMenu getBottomRightMenu() {
        return mBottomRightMenu;
    }

    public boolean isTouchIn(float x, float y) {
        mapPoints();
        return CalculateUtils.isRectContainsPoint(mTopLeftPoint,
                mTopRightPoint, mBottomRightPoint,
                mBottomLeftPoint, new Point(x, y));
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
}
