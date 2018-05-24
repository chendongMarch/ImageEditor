package com.march.picedit.test;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.march.common.model.ImageInfo;
import com.march.common.utils.BitmapUtils;
import com.march.common.utils.FileUtils;
import com.march.picedit.PicEditActivity;
import com.march.picedit.R;
import com.march.picedit.SelectImageActivity;
import com.march.picedit.sticker.ResourceFactory;
import com.march.piceditor.functions.graffiti.GraffitiOverlayView;
import com.march.turbojpeg.TurboJpegUtils;
import com.march.uikit.annotation.UILayout;

import java.io.File;
import java.util.ArrayList;

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
 * CreateAt : 7/19/17
 * Describe :
 *
 * @author chendong
 */
@UILayout(R.layout.test_compress_activity)
public class TestCompressImageActivity extends PicEditActivity {

    private String mPath;
    private String mPath2;

    @BindView(R.id.iv_image)       ImageView          mImageView;
    @BindView(R.id.iv_image2)      ImageView          mImageView2;
    @BindView(R.id.mosaic_overlay) GraffitiOverlayView mMosaicOverlay;

    private ResourceFactory mResourceFactory;

    @Override
    public void initBeforeViewCreated() {
        super.initBeforeViewCreated();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    @OnClick({R.id.btn_action, R.id.btn_choose_pic})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_action:
                if (mPath == null) return;
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                        FileUtils.newRootFile("/ztemp1/").mkdirs();
                        FileUtils.newRootFile("/ztemp2/").mkdirs();

                        Bitmap bitmap = BitmapFactory.decodeFile(mPath);

                        // so保存
                        File file = FileUtils.newRootFile("/ztemp1/" + System.currentTimeMillis() + ".jpg");
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        TurboJpegUtils.compressBitmap(bitmap, 100, file.getAbsolutePath(), true);
                        mPath = file.getAbsolutePath();

                        // android 算法
                        Bitmap bitmap2 = BitmapFactory.decodeFile(mPath2);
                        File file2 = FileUtils.newRootFile("/ztemp2/" + System.currentTimeMillis() + ".jpg");
                        BitmapUtils.compressImage(bitmap2, file2, Bitmap.CompressFormat.JPEG, 100, true);
                        mPath2 = file2.getAbsolutePath();
                        e.onNext(mPath);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                Glide.with(getContext()).load(mPath).into(mImageView);
                                Glide.with(getContext()).load(mPath2).into(mImageView2);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
                break;
            case R.id.btn_choose_pic:
                SelectImageActivity.startActivity(getActivity());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<ImageInfo> extra = data.getParcelableArrayListExtra(SelectImageActivity.KEY_DATA);
        if (extra.size() != 1) {
            return;
        }
        ImageInfo imageInfo = extra.get(0);
        mMosaicOverlay.setSrc(imageInfo.getPath());
    }


}
