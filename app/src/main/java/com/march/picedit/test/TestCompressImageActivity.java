package com.march.picedit.test;

import android.widget.ImageView;

import com.march.dev.app.activity.BaseActivity;
import com.march.picedit.R;

import butterknife.BindView;

/**
 * CreateAt : 7/19/17
 * Describe :
 *
 * @author chendong
 */
public class TestCompressImageActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.test_compress_activity;
    }


    @BindView(R.id.iv_image) ImageView mImageView;


}
