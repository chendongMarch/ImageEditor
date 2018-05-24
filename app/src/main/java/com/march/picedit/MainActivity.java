package com.march.picedit;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import com.march.common.model.ImageInfo;
import com.march.picedit.edit.EditCropRotateActivity;
import com.march.picedit.graffiti.GraffitiActivity;
import com.march.picedit.sticker.StickerImageActivity;
import com.march.picedit.test.TestCompressImageActivity;
import com.march.picedit.test.TestPorterDuffXfermodeActivity;
import com.march.uikit.annotation.UILayout;
import com.march.uikit.annotation.UITitle;

import java.util.ArrayList;

import butterknife.OnClick;

@UILayout(R.layout.home_activity)
@UITitle(titleText = "首页")
public class MainActivity extends PicEditActivity {

    @Override
    public void initBeforeViewCreated() {
        super.initBeforeViewCreated();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    private int launchViewId = -1;

    @OnClick({R.id.btn_xfermode, R.id.btn_choose_pic, R.id.btn_test, R.id.btn_sticker_test, R.id.btn_graffiti})
    public void clickView(View view) {
        launchViewId = view.getId();
        switch (view.getId()) {
            case R.id.btn_choose_pic:
                SelectImageActivity.startActivity(getActivity());
                break;
            case R.id.btn_test:
                startActivity(TestCompressImageActivity.class);
                break;
            case R.id.btn_sticker_test:
                SelectImageActivity.startActivity(getActivity());
                break;
            case R.id.btn_graffiti:
                GraffitiActivity.start(getActivity());
                break;
            case R.id.btn_xfermode:
                startActivity(TestPorterDuffXfermodeActivity.class);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        if (launchViewId == -1) {
            return;
        }
        ArrayList<ImageInfo> extra = data.getParcelableArrayListExtra(SelectImageActivity.KEY_DATA);
        if (extra.size() != 1) {
            return;
        }
        switch (launchViewId) {
            case R.id.btn_choose_pic:
                EditCropRotateActivity.start(getActivity(), extra.get(0).getPath());
                break;
            case R.id.btn_sticker_test:
                StickerImageActivity.start(getActivity(), extra.get(0).getPath());
                break;
        }
    }
}
