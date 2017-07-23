package com.march.piceditor.sticker.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.march.dev.utils.BitmapUtils;
import com.march.piceditor.common.model.Point;
import com.march.piceditor.utils.CalculateUtils;
import com.march.piceditor.utils.Utils;

/**
 * CreateAt : 7/20/17
 * Describe : 贴纸类
 *
 * @author chendong
 */
public class Sticker implements Comparable<Sticker> {

    private static int sStaticId = 0;

    private Bitmap mStickerImage; // 贴纸资源
    private Matrix mMatrix; // 贴纸变换
    private RectF  mRectF; // 贴纸对应区域，注意，这里是水平矩形，不会旋转

    private boolean mIsActive; // 当前贴纸是否激活
    // 当前贴纸是否删除，
    // 标记为删除的贴纸为假删，只是不尽兴绘制了而已，
    // 如果希望真的删除，使用 StickerDrawOverlay 删除贴纸的方法
    private boolean mIsHidden;
    // 自动提升，如果允许自动提升，
    // 点击时会将当前贴纸下面的贴纸提升上来，
    // 避免因为遮挡无法点击下层贴纸
    private boolean mIsAutoLifting;
    private int    mId;
    private long    mPriority; // 当前贴纸绘制优先级别
    private int     mMinSize; // 最小限制，不设置时表示不限制
    private int     mMaxSize; // 最大限制，不设置时表示不限制\

    private float mInitScale = 1;
    private float mInitTranslateX, mInitTranslateY;

    private ColorMatrixColorFilter   mColorFilter; // 颜色过滤器，将贴纸过滤成纯色
    private SparseArray<Point>       mCornerPointMap; // 四角点的位置，可倾斜矩形
    private SparseArray<StickerMenu> mMenuMap; // 四个菜单键的存储


    public Sticker(Context context) {
        mStickerImage = BitmapFactory.decodeResource(context.getResources(), android.R.mipmap.sym_def_app_icon);
        init();
    }

    public Sticker(Bitmap source) {
        mStickerImage = source;
        init();
    }


    private void init() {
        mId = sStaticId++;
        mMatrix = new Matrix();
        mRectF = new RectF();
        mPriority = System.currentTimeMillis();
        mCornerPointMap = new SparseArray<>();
        mMenuMap = new SparseArray<>();
        mCornerPointMap.put(Position.TOP_LEFT, new Point());
        mCornerPointMap.put(Position.TOP_RIGHT, new Point());
        mCornerPointMap.put(Position.BOTTOM_RIGHT, new Point());
        mCornerPointMap.put(Position.BOTTOM_LEFT, new Point());
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean active) {
        mIsActive = active;
    }

    public Bitmap getStickerImage() {
        return mStickerImage;
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

    public Point[] getCornerPoints() {
        mapPoints();
        return new Point[]{
                mCornerPointMap.get(Position.TOP_LEFT),
                mCornerPointMap.get(Position.TOP_RIGHT),
                mCornerPointMap.get(Position.BOTTOM_RIGHT),
                mCornerPointMap.get(Position.BOTTOM_LEFT)
        };
    }

    public boolean isHidden() {
        return mIsHidden;
    }

    public void setHidden(boolean hidden) {
        mIsHidden = hidden;
    }


    public float getInitScale() {
        return mInitScale;
    }

    public float getInitTranslateX() {
        return mInitTranslateX;
    }

    public float getInitTranslateY() {
        return mInitTranslateY;
    }

    public void setStickerImage(Bitmap stickerImage) {
        mStickerImage = stickerImage;
    }

    public void bringTopLayer() {
        mPriority = System.currentTimeMillis();
    }

    public void bringBottomLayer() {
        mPriority = -System.currentTimeMillis();
    }

    public RectF getRectF() {
        mRectF.set(0, 0, mStickerImage.getWidth(), mStickerImage.getHeight());
        mMatrix.mapRect(mRectF);
        return mRectF;
    }

    private void mapPoints() {

        float[] dst = new float[8];

        float[] src = new float[]{
                0, 0, mStickerImage.getWidth(),
                0, 0, mStickerImage.getHeight(),
                mStickerImage.getWidth(), mStickerImage.getHeight()};

        mMatrix.mapPoints(dst, src);

        mCornerPointMap.get(Position.TOP_LEFT).set(dst[0], dst[1]);
        mCornerPointMap.get(Position.TOP_RIGHT).set(dst[2], dst[3]);
        mCornerPointMap.get(Position.BOTTOM_LEFT).set(dst[4], dst[5]);
        mCornerPointMap.get(Position.BOTTOM_RIGHT).set(dst[6], dst[7]);
    }

    public SparseArray<Point> getCornerPointMap() {
        return mCornerPointMap;
    }

    public SparseArray<StickerMenu> getMenuMap() {
        return mMenuMap;
    }

    public boolean isTouchIn(float x, float y) {
        Point[] points = getCornerPoints();
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

    public void setColorFilter(int red, int green, int blue) {
        if (red == -1 || green == -1 || blue == -1) {
            mColorFilter = null;
        } else
            mColorFilter = Utils.buildColorFilter(red, green, blue);
    }

    public void setNoColorFilter() {
        mColorFilter = null;
    }

    public ColorMatrixColorFilter getColorFilter() {
        return mColorFilter;
    }

    public void setInitScale(float initScale) {
        mInitScale = initScale;
    }

    public void setInitTranslate(float tx, float ty) {
        mInitTranslateX = tx;
        mInitTranslateY = ty;
    }

    public void setMinSize(int minSize) {
        mMinSize = minSize;
    }

    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
    }

    public boolean isAutoLifting() {
        return mIsAutoLifting;
    }

    public void setAutoLifting(boolean autoLifting) {
        mIsAutoLifting = autoLifting;
    }

    // matrix 辅助
    public void postMatrixScale(float sx, float sy) {
        RectF rectF = getRectF();
        float cy = rectF.centerY();
        float cx = rectF.centerX();
        getMatrix().postScale(sx, sy, cx, cy);
    }

    public void postMatrixRotate(float rotation) {
        RectF rectF = getRectF();
        float cy = rectF.centerY();
        float cx = rectF.centerX();
        getMatrix().postRotate(rotation, cx, cy);
    }

    // 两边中的较大值小于最大值，两边中最小值大于最小值
    public boolean isCanScale(float scale) {
        float length1 = CalculateUtils.calculateDistance(mCornerPointMap.get(Position.TOP_LEFT), mCornerPointMap.get(Position.TOP_RIGHT));
        float length2 = CalculateUtils.calculateDistance(mCornerPointMap.get(Position.TOP_RIGHT), mCornerPointMap.get(Position.BOTTOM_RIGHT));
        if (scale > 1) {
            // 放大时，没设置限制 || 没到达最大值
            return mMaxSize <= 0 || Math.max(length1, length2) < mMaxSize;
        } else if (scale < 1) {
            // 缩小时，没设置限制 || 没到达最小值
            return mMinSize <= 0 || Math.min(length1, length2) > mMinSize;
        } else return true;
    }


    public void destroy() {
        System.gc();
        mIsActive = false;
        mIsHidden = true;
        mColorFilter = null;
        mCornerPointMap.clear();
        mMenuMap.clear();
        BitmapUtils.recycleBitmaps(mStickerImage);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Sticker sticker = (Sticker) o;
        return mId == sticker.mId;
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
