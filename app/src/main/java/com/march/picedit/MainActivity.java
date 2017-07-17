package com.march.picedit;

import android.view.View;

import com.march.dev.app.activity.BaseActivity;
import com.march.picedit.ui.CropActivity;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @OnClick({R.id.btn_crop})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_crop:
                CropActivity.start(mActivity);
                break;
        }
    }
}
