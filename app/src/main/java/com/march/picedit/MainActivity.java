package com.march.picedit;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.FileUtils;
import com.march.dev.utils.GlideUtils;
import com.march.dev.utils.LogUtils;
import com.march.dev.widget.TitleBarView;

import java.io.File;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.iv_image) ImageView mImageView;

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mTitleBarView.setText(TitleBarView.CENTER, "裁剪");
        File file = FileUtils.newRootFile("1.jpg");
        LogUtils.e(TAG, file.getAbsolutePath());
        GlideUtils.with(mContext, file.getAbsolutePath())
                .into(mImageView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }
}
