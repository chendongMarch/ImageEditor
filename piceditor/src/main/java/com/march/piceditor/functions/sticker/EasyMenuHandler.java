package com.march.piceditor.functions.sticker;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.march.piceditor.utils.BitmapUtils;
import com.march.piceditor.functions.sticker.listener.StickerMenuHandler;
import com.march.piceditor.functions.sticker.model.Sticker;
import com.march.piceditor.functions.sticker.model.StickerMenu;

/**
 * CreateAt : 7/22/17
 * Describe : 内置简单版本 menu
 *
 * @author chendong
 */
public enum EasyMenuHandler implements StickerMenuHandler {

    // 隐藏贴纸
    HIDE_STICKER() {
        @Override
        public void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu) {
            sticker.setHidden(true);
        }
    },
    // 删除贴纸
    DELETE_STICKER() {
        @Override
        public void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu) {
            overlay.removeSticker(sticker);
        }
    },
    // 垂直翻转
    FLIP_VERTICAL() {
        @Override
        public void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu) {
            flip(1, -1, sticker);
        }
    },
    // 水平翻转
    FLIP_HORIZONTAL() {
        @Override
        public void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu) {
            flip(-1, 1, sticker);
        }
    },

    // 双向翻转
    FLIP_SKEW() {
        @Override
        public void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu) {
            flip(-1, -1, sticker);
        }
    };

    private static void flip(int sx, int sy, Sticker sticker) {
        Bitmap bitmap = sticker.getStickerImage();
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        sticker.setStickerImage(bitmap1);
        BitmapUtils.recycleBitmaps(bitmap);
    }
}
