package com.march.picedit.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.model.ImageInfo;
import com.march.dev.uikit.selectimg.SelectImageActivity;
import com.march.dev.utils.BitmapUtils;
import com.march.dev.utils.FileUtils;
import com.march.dev.utils.GlideUtils;
import com.march.picedit.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

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
public class TestCompressImageActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.test_compress_activity;
    }

    private String mPath;

    @BindView(R.id.iv_image) ImageView mImageView;


    @OnClick({R.id.btn_action, R.id.btn_choose_pic})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.btn_action:
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                        Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                        File file = FileUtils.newRootFile(System.currentTimeMillis() + ".jpg");
                        BitmapUtils.compressImage(bitmap, file, Bitmap.CompressFormat.JPEG, 100, true);
                        mPath = file.getAbsolutePath();
                        e.onNext(mPath);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                GlideUtils.with(mContext, s).into(mImageView);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
                break;
            case R.id.btn_choose_pic:
                SelectImageActivity.start(mActivity, 1, hashCode());
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
                mPath = imageInfo.getPath();
                GlideUtils.with(mContext, mPath).into(mImageView);
                break;
        }
    }

}
