package com.march.picedit.graffiti;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.ActivityAnimUtils;
import com.march.dev.utils.FileUtils;
import com.march.picedit.R;
import com.march.piceditor.graffiti.GraffitiLayer;
import com.march.piceditor.graffiti.GraffitiOverlay;
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

    @BindView(R.id.graffiti_overlay) GraffitiOverlay mGraffitiOverlay;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, GraffitiActivity.class);
        activity.startActivity(intent);
        ActivityAnimUtils.translateStart(activity);

    }

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mGraffitiOverlay.setSrc(FileUtils.newRootFile("2.jpg").getAbsolutePath());
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.newRootFile("bg.jpg").getAbsolutePath());
//        mGraffitiOverlay.setGraffitiLayer(GraffitiLayer.newImageLayer(bitmap));
//        mGraffitiOverlay.setGraffitiLayer(GraffitiLayer.newMosaicLayer(40));

        mGraffitiOverlay.setGraffitiLayer(GraffitiLayer.newLowPolyLayer(GraffitiLayer.DEF_VALUE));
    }


    @Override
    protected int getLayoutId() {
        return R.layout.graffiti_activity;
    }


    @OnClick({R.id.tv_path, R.id.tv_rect, R.id.tv_erase, R.id.tv_draw,R.id.tv_save})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.tv_path:
                mGraffitiOverlay.setTouchMode(GraffitiOverlay.TouchMode.PATH);
                break;
            case R.id.tv_rect:
                mGraffitiOverlay.setTouchMode(GraffitiOverlay.TouchMode.RECT);
                break;
            case R.id.tv_erase:
                mGraffitiOverlay.setErase(true);
                break;
            case R.id.tv_draw:
                mGraffitiOverlay.setErase(false);
                break;
            case R.id.tv_save:
                Bitmap save = mGraffitiOverlay.save();
                TurboJpegUtils.compressBitmap(save,100,FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath(),true);
                break;
        }
    }
}
