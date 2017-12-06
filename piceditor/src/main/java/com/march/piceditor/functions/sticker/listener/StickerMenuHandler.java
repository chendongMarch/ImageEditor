package com.march.piceditor.functions.sticker.listener;

import com.march.piceditor.functions.sticker.StickerDrawOverlay;
import com.march.piceditor.functions.sticker.model.Sticker;
import com.march.piceditor.functions.sticker.model.StickerMenu;

/**
 * CreateAt : 7/21/17
 * Describe : 菜单点击之后的动作
 *
 * @author chendong
 */
public interface StickerMenuHandler {
    void onMenuClick(StickerDrawOverlay overlay, Sticker sticker, StickerMenu menu);
}
