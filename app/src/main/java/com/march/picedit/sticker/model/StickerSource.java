package com.march.picedit.sticker.model;

/**
 * CreateAt : 7/22/17
 * Describe :
 *
 * @author chendong
 */
public class StickerSource {

    private String  sourceUrl;
    private boolean withColor;

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public boolean isWithColor() {
        return withColor;
    }

    public void setWithColor(boolean withColor) {
        this.withColor = withColor;
    }
}
