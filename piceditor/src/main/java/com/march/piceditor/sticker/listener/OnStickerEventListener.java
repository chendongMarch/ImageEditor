package com.march.piceditor.sticker.listener;

import com.march.piceditor.sticker.model.Sticker;

/**
 * CreateAt : 7/21/17
 * Describe :
 *
 * @author chendong
 */
public interface OnStickerEventListener {

    /**
     * 贴纸选择事件
     *
     * @param preSticker     上一个贴纸，可能为空
     * @param currentSticker 当前选中的贴纸，不为空，可能与上一个贴纸相同
     */
    void OnStickerSelect(Sticker preSticker, Sticker currentSticker);

    void OnEmptyAreaClick();
}
