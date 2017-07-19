package com.march.picedit;

import android.view.View;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.model.ImageInfo;
import com.march.dev.uikit.selectimg.SelectImageActivity;
import com.march.picedit.edit.CropRotateActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.home_activity;
    }

    @OnClick({R.id.btn_choose_pic})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_choose_pic:
                SelectImageActivity.start(mActivity, 1);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SelectImageActivity.SelectImageEvent event) {
        switch (event.getMessage()) {
            case SelectImageActivity.SelectImageEvent.ON_SUCCESS:
                ImageInfo imageInfo = event.mImageInfos.get(0);
                CropRotateActivity.start(mActivity, imageInfo.getPath());
                break;
        }
    }
}
