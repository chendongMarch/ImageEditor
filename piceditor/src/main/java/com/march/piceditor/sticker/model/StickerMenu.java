package com.march.piceditor.sticker.model;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.march.dev.utils.BitmapUtils;
import com.march.piceditor.common.model.Point;
import com.march.piceditor.sticker.listener.StickerMenuHandler;

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

//    public enum Handler implements StickerMenuHandler {
//        // 删除贴纸
//        DELETE_MENU() {
//            @Override
//            public void onMenuClick(Sticker sticker, StickerMenu menu) {
//                sticker.setDelete(true);
//            }
//        },
//        // 垂直翻转
//        FLIP_VERTICAL() {
//            @Override
//            public void onMenuClick(Sticker sticker, StickerMenu menu) {
//                flip(1, -1, sticker);
//            }
//        },
//        // 水平翻转
//        FLIP_HORIZONTAL() {
//            @Override
//            public void onMenuClick(Sticker sticker, StickerMenu menu) {
//                flip(-1, 1, sticker);
//            }
//        },
//        // 水平翻转
//        FLIP_SKEW() {
//            @Override
//            public void onMenuClick(Sticker sticker, StickerMenu menu) {
//                flip(-1, -1, sticker);
//            }
//        };
//
//        private static void flip(int sx, int sy, Sticker sticker) {
//            Bitmap bitmap = sticker.getBitmap();
//            Matrix matrix = new Matrix();
//            matrix.postScale(sx, sy);
//            Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//            sticker.setBitmap(bitmap1);
//            BitmapUtils.recycleBitmaps(bitmap);
//        }
//    }

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

        Point p = mAttachSticker.getPointMap().get(mPositionType);

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
