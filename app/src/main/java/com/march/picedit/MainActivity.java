package com.march.picedit;

import android.os.Bundle;
import android.view.View;
import android.widget.SimpleAdapter;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.model.ImageInfo;
import com.march.dev.uikit.selectimg.SelectImageActivity;
import com.march.dev.utils.FileUtils;
import com.march.picedit.edit.EditCropRotateActivity;
import com.march.picedit.graffiti.GraffitiActivity;
import com.march.picedit.sticker.StickerImageActivity;
import com.march.picedit.test.TestCompressImageActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.home_activity;
    }


    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        // StickerImageActivity.start(mActivity, FileUtils.newRootFile("2.jpg").getAbsolutePath());
    }

    @OnClick({R.id.btn_choose_pic, R.id.btn_test, R.id.btn_sticker_test,R.id.btn_graffiti})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_choose_pic:
                SelectImageActivity.start(mActivity, 1, 1);
                break;
            case R.id.btn_test:
                startActivity(TestCompressImageActivity.class);
                break;
            case R.id.btn_sticker_test:
                SelectImageActivity.start(mActivity, 1, 2);
                break;
            case R.id.btn_graffiti:
                GraffitiActivity.start(mActivity);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SelectImageActivity.SelectImageEvent event) {
        switch (event.getMessage()) {
            case SelectImageActivity.SelectImageEvent.ON_SUCCESS:
                ImageInfo imageInfo = event.mImageInfos.get(0);
                if(event.getTag() == 1){
                    EditCropRotateActivity.start(mActivity, imageInfo.getPath());
                }else if(event.getTag() ==2 ){
                    StickerImageActivity.start(mActivity,imageInfo.getPath());
                }
                break;
        }
    }
}
