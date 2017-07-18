package com.march.picedit.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * CreateAt : 7/17/17
 * Describe :
 *
 * @author chendong
 */
public class CropActivity extends BaseActivity {

    public static final String KEY_PATH = "KEY_PATH";
    public static final String TAG      = MainActivity.class.getSimpleName();

    private String mPicFilePath;

    public static void start(Activity activity, String path) {
        Intent intent = new Intent(activity, CropActivity.class);
        intent.putExtra(KEY_PATH, path);
        activity.startActivity(intent);

        ActivityAnimUtils.translateStart(activity);
    }

    @BindView(R.id.iv_image)  ImageView   mImageView;
    @BindView(R.id.fl_parent) View        mParentFl;
    @BindView(R.id.col_crop)  CropOverlay mCropOverlay;

    @Override
    public void onReceiveData() {
        super.onReceiveData();
        mPicFilePath = getIntent().getStringExtra(KEY_PATH);
    }

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mTitleBarView.setText(TitleBarView.CENTER, "裁剪");
        mTitleBarView.setText(TitleBarView.LEFT, "back");
        mTitleBarView.setLeftBackListener(mActivity);

        GlideUtils.with(mContext, mPicFilePath).into(mImageView);

        BitmapFactory.Options bitmapSize = BitmapUtils.getBitmapSize(mPicFilePath);
        int width = (int) (DimensUtils.getScreenWidth(mContext) * 0.8f);
        int height = (int) (width * (bitmapSize.outHeight * 1f / bitmapSize.outWidth));
        mParentFl.getLayoutParams().width = width;
        mParentFl.getLayoutParams().height = height;
    }


    @OnClick({R.id.btn_crop, R.id.btn_size_916, R.id.btn_size_43, R.id.btn_size_11, R.id.btn_size_free})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_crop:
                Observable.create(new ObservableOnSubscribe<Bitmap>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Bitmap> e) throws Exception {
                        e.onNext(mCropOverlay.crop(mPicFilePath, Bitmap.Config.RGB_565));
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(@NonNull Bitmap bitmap) throws Exception {
                                String absolutePath = FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath();
                                BitmapUtils.compressImage(bitmap, new File(absolutePath), Bitmap.CompressFormat.JPEG, 100, true);
                                ToastUtils.show("成功- " + absolutePath);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                ToastUtils.show("失败- " + throwable.getMessage());
                            }
                        });
                break;
            case R.id.btn_size_916:
                mCropOverlay.setAspectRatio(9f / 16);
                break;
            case R.id.btn_size_43:
                mCropOverlay.setAspectRatio(4f / 3);
                break;
            case R.id.btn_size_free:
                mCropOverlay.setAspectRatio(CropOverlay.NO_ASPECT_RATIO);
                break;
            case R.id.btn_size_11:
                mCropOverlay.setAspectRatio(1f / 1);
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

