package com.march.piceditor.functions.sticker.listener;

import com.march.piceditor.functions.sticker.StickerDrawOverlay;
import com.march.piceditor.functions.sticker.model.Sticker;
import com.march.piceditor.functions.sticker.model.StickerMenu;

/**
 * CreateAt : 7/21/17
 * Describe :
 *
 * @author chendong
 */
public interface StickerMenuHandler {
    void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu);
}
