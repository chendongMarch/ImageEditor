package com.march.picedit;

import android.view.View;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.model.ImageInfo;
import com.march.dev.uikit.selectimg.SelectImageActivity;
import com.march.picedit.edit.EditCropRotateActivity;
import com.march.picedit.sticker.StickerActivity;
import com.march.picedit.test.TestCompressImageActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.home_activity;
    }

    @OnClick({R.id.btn_choose_pic, R.id.btn_test, R.id.btn_sticker_source})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_choose_pic:
                SelectImageActivity.start(mActivity, 1, hashCode());
                break;
            case R.id.btn_test:
                startActivity(TestCompressImageActivity.class);
                break;
            case R.id.btn_sticker_source:
                startActivity(StickerActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SelectImageActivity.SelectImageEvent event) {
        if (hashCode() != event.getTag())
            return;
        switch (event.getMessage()) {
            case SelectImageActivity.SelectImageEvent.ON_SUCCESS:
                ImageInfo imageInfo = event.mImageInfos.get(0);
                EditCropRotateActivity.start(mActivity, imageInfo.getPath());
                break;
        }
    }
}
