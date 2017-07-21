package com.march.piceditor.sticker;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.march.dev.utils.BitmapUtils;
import com.march.piceditor.sticker.listener.StickerMenuHandler;
import com.march.piceditor.sticker.model.Sticker;
import com.march.piceditor.sticker.model.StickerMenu;

/**
 * CreateAt : 7/22/17
 * Describe :
 *
 * @author chendong
 */
public enum EasyMenuHandler implements StickerMenuHandler {
    // 删除贴纸
    DELETE_MENU() {
        @Override
        public void onMenuClick(Sticker sticker, StickerMenu menu) {
            sticker.setDelete(true);
        }
    },
    // 垂直翻转
    FLIP_VERTICAL() {
        @Override
        public void onMenuClick(Sticker sticker, StickerMenu menu) {
            flip(1, -1, sticker);
        }
    },
    // 水平翻转
    FLIP_HORIZONTAL() {
        @Override
        public void onMenuClick(Sticker sticker, StickerMenu menu) {
            flip(-1, 1, sticker);
        }
    },
    // 水平翻转
    FLIP_SKEW() {
        @Override
        public void onMenuClick(Sticker sticker, StickerMenu menu) {
            flip(-1, -1, sticker);
        }
    };

    private static void flip(int sx, int sy, Sticker sticker) {
        Bitmap bitmap = sticker.getBitmap();
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        sticker.setBitmap(bitmap1);
        BitmapUtils.recycleBitmaps(bitmap);
    }
}
