package com.march.piceditor.functions.sticker.model;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.march.piceditor.model.PointF;
import com.march.piceditor.functions.sticker.listener.StickerMenuHandler;

/**
 * CreateAt : 7/21/17
 * Describe : 贴纸菜单
 * 相同的icon可以引用同一个，避免创建大量的drawable
 *
 * @author chendong
 */
public class StickerMenu {

    private Drawable mMenuIcon;
    private Rect     mBoundRect;
    private Sticker  mAttachSticker;
    private Object   mTag;

    private int mScale;
    private int mPositionType;

    private StickerMenuHandler mStickerMenuHandler;


    public StickerMenu(int type, Drawable menuIcon) {
        init(type, menuIcon);
    }

    public void init(int type, Drawable menuIcon) {
        mMenuIcon = menuIcon;
        mPositionType = type;
        mScale = 2;
        mBoundRect = new Rect();
    }

    public void attachSticker(Sticker sticker) {
        mAttachSticker = sticker;
    }

    public Drawable getMenuIcon() {
        return mMenuIcon;
    }

    public void setMenuIcon(Drawable menuIcon) {
        mMenuIcon = menuIcon;
    }

    public int getMenuWidth() {
        return mMenuIcon.getIntrinsicWidth();
    }

    public int getMenuHeight() {
        return mMenuIcon.getIntrinsicHeight();
    }

    public int getPositionType() {
        return mPositionType;
    }

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public void updateBounds() {
        mMenuIcon.setBounds(getBounds());
    }

    public Rect getBounds() {

        PointF p = mAttachSticker.getCornerPointMap().get(mPositionType);

        mBoundRect.set((int) (p.x - getMenuWidth() / mScale / 2),
                (int) (p.y - getMenuHeight() / mScale / 2),
                (int) (p.x - getMenuWidth() / mScale / 2 + getMenuWidth() / mScale),
                (int) (p.y - getMenuHeight() / mScale / 2 + getMenuHeight() / mScale));

        return mBoundRect;
    }

    public StickerMenuHandler getStickerMenuHandler() {
        return mStickerMenuHandler;
    }

    public void setStickerMenuHandler(StickerMenuHandler stickerMenuHandler) {
        mStickerMenuHandler = stickerMenuHandler;
    }


    public boolean isTouchIn(float x, float y) {
        return getBounds().contains((int) x, (int) y);
    }
}
