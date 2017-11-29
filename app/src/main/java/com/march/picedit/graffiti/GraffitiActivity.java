package com.march.picedit.graffiti;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.ActivityAnimUtils;
import com.march.dev.utils.FileUtils;
import com.march.picedit.R;
import com.march.piceditor.functions.graffiti.GraffitiLayerConfig;
import com.march.piceditor.functions.graffiti.GraffitiOverlayView;
import com.march.turbojpeg.TurboJpegUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * CreateAt : 7/25/17
 * Describe :
 *
 * @author chendong
 */
public class GraffitiActivity extends BaseActivity {

    @BindView(R.id.graffiti_overlay) GraffitiOverlayView mGraffitiOverlay;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, GraffitiActivity.class);
        activity.startActivity(intent);
        ActivityAnimUtils.translateStart(activity);

    }

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mGraffitiOverlay.setSrc(FileUtils.newRootFile("2.jpg").getAbsolutePath());

    }


    @Override
    protected int getLayoutId() {
        return R.layout.graffiti_activity;
    }


    @OnClick({R.id.tv_path, R.id.tv_rect, R.id.tv_erase, R.id.tv_draw, R.id.tv_save})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.tv_path:
                mGraffitiOverlay.setTouchMode(GraffitiOverlayView.TouchMode.PATH);
                break;
            case R.id.tv_rect:
                mGraffitiOverlay.setTouchMode(GraffitiOverlayView.TouchMode.RECT);
                break;
            case R.id.tv_erase:
                mGraffitiOverlay.setErase(true);
                break;
            case R.id.tv_draw:
                mGraffitiOverlay.setErase(false);
                break;
            case R.id.tv_save:
                Bitmap save = mGraffitiOverlay.save();
                TurboJpegUtils.compressBitmap(save, 100, FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath(), true);
                break;
        }
    }


    @OnClick({R.id.tv_effect_blur,
                     R.id.tv_effect_color,
                     R.id.tv_effect_poly,
                     R.id.tv_effect_mosaic,
                     R.id.tv_effect_image})
    public void clickView2(View view) {
        switch (view.getId()) {
            case R.id.tv_effect_blur:
                mGraffitiOverlay.addGraffitiLayer(GraffitiLayerConfig.newBlurLayer(GraffitiLayerConfig.DEF_VALUE));
                break;
            case R.id.tv_effect_color:
                mGraffitiOverlay.addGraffitiLayer(GraffitiLayerConfig.newColorLayer(Color.parseColor("#ff60ead5")));
                break;
            case R.id.tv_effect_poly:
                mGraffitiOverlay.addGraffitiLayer(GraffitiLayerConfig.newLowPolyLayer(GraffitiLayerConfig.DEF_VALUE));
                break;
            case R.id.tv_effect_mosaic:
                mGraffitiOverlay.addGraffitiLayer(GraffitiLayerConfig.newMosaicLayer(40));
                break;
            case R.id.tv_effect_image:
                Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.newRootFile("bg.jpg").getAbsolutePath());
                mGraffitiOverlay.addGraffitiLayer(GraffitiLayerConfig.newImageLayer(bitmap));
                break;
        }
    }
}
