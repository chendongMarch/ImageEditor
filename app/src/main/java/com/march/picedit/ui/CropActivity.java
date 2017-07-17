package com.march.picedit.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.ActivityAnimUtils;
import com.march.dev.utils.BitmapUtils;
import com.march.dev.utils.DimensUtils;
import com.march.dev.utils.FileUtils;
import com.march.dev.utils.GlideUtils;
import com.march.dev.utils.PermissionUtils;
import com.march.dev.utils.ToastUtils;
import com.march.dev.widget.TitleBarView;
import com.march.picedit.MainActivity;
import com.march.picedit.R;
import com.march.picedit.crop.CropOverlay;

import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * CreateAt : 7/17/17
 * Describe :
 *
 * @author chendong
 */
public class CropActivity extends BaseActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private String mPicFilePath;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, CropActivity.class);
        activity.startActivity(intent);
        ActivityAnimUtils.translateStart(activity);
    }

    @BindView(R.id.iv_image)  ImageView   mImageView;
    @BindView(R.id.fl_parent) View        mParentFl;
    @BindView(R.id.col_crop)  CropOverlay mCropOverlay;

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mTitleBarView.setText(TitleBarView.CENTER, "裁剪");
        mTitleBarView.setText(TitleBarView.LEFT, "back");
        mTitleBarView.setLeftBackListener(mActivity);
        mPicFilePath = FileUtils.newRootFile("2.jpg").getAbsolutePath();
        GlideUtils.with(mContext, mPicFilePath).into(mImageView);

        BitmapFactory.Options bitmapSize = BitmapUtils.getBitmapSize(mPicFilePath);

        int width = (int) (DimensUtils.getScreenWidth(mContext) * 0.8f);
        int height = (int) (width * (bitmapSize.outHeight * 1f / bitmapSize.outWidth));
        mParentFl.getLayoutParams().width = width;
        mParentFl.getLayoutParams().height = height;
    }


    private void decodeRegion(String filePath, String targetFilePath) {
        try {
            // 生成decoder对象
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filePath, true);
            final int imgWidth = decoder.getWidth();
            final int imgHeight = decoder.getHeight();
            Rect rect = mCropOverlay.getCropRect(imgWidth, imgHeight);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 为了内存考虑，将图片格式转化为RGB_565
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            // 将矩形区域解码生成要加载的Bitmap对象
            Bitmap bm = decoder.decodeRegion(rect, opts);
            BitmapUtils.compressImage(bm, FileUtils.newRootFile("crop.jpg"), Bitmap.CompressFormat.JPEG, 100, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.btn_crop})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_crop:
                decodeRegion(mPicFilePath, FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath());
                break;
        }
    }

    @Override
    protected String[] getPermission2Check() {
        return new String[]{PermissionUtils.PER_READ_EXTERNAL_STORAGE,
                PermissionUtils.PER_WRITE_EXTERNAL_STORAGE};
    }

    @Override
    protected boolean handlePermissionResult(Map<String, Integer> reqPermissionsAndResult) {
        ToastUtils.show("permission denied");
        return super.handlePermissionResult(reqPermissionsAndResult);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.crop_activity;
    }

    @Override
    public void finish() {
        super.finish();
        ActivityAnimUtils.translateFinish(mActivity);
    }
}

