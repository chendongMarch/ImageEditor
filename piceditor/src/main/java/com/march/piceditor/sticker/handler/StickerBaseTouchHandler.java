package com.march.piceditor.sticker.handler;

import com.march.piceditor.common.TouchHandler;
import com.march.piceditor.sticker.model.Sticker;

/**
 * CreateAt : 7/20/17
 * Describe : 贴纸手势处理基类
 *
 * @author chendong
 */
public abstract class StickerBaseTouchHandler implements TouchHandler {

    public static final int MOVE                = 0;
    public static final int TWO_FINGER          = 1;
    public static final int BOTTOM_RIGHT_CORNER = 2;


    protected Sticker mActiveSticker;

    public void init(Sticker activeSticker) {
        mActiveSticker = activeSticker;
    }

    protected boolean isHadStickerActive() {
        return mActiveSticker != null;
    }
}