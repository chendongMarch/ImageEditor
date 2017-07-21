package com.march.picedit.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.model.ImageInfo;
import com.march.dev.uikit.selectimg.SelectImageActivity;
import com.march.dev.utils.BitmapUtils;
import com.march.dev.utils.FileUtils;
import com.march.dev.utils.GlideUtils;
import com.march.dev.utils.PermissionUtils;
import com.march.dev.utils.ToastUtils;
import com.march.picedit.R;
import com.march.piceditor.mosaic.DrawMosaicView;
import com.march.piceditor.mosaic.MosaicUtil;
import com.march.piceditor.sticker.StickerDrawOverlay;
import com.march.piceditor.sticker.listener.OnStickerMenuClickListener;
import com.march.piceditor.sticker.model.Position;
import com.march.piceditor.sticker.model.Sticker;
import com.march.piceditor.sticker.model.StickerMenu;
import com.march.turbojpeg.TurboJpegUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private String mPath2;

    @BindView(R.id.iv_image)  ImageView          mImageView;
    @BindView(R.id.iv_image2) ImageView          mImageView2;
    @BindView(R.id.dmv)       DrawMosaicView     mDrawMosaicView;
    @BindView(R.id.sdo)       StickerDrawOverlay mStickerDrawOverlay;


    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);

        for (int i = 0; i < 5; i++) {
            Sticker sticker = new Sticker(mContext);
            StickerMenu topLeftMenu = new StickerMenu(Position.TOP_LEFT, mContext, R.drawable.sticker_edit_del);
            StickerMenu topRightMenu = new StickerMenu(Position.TOP_RIGHT, mContext, R.drawable.sticker_edit_symmetry);
            StickerMenu bottomLeftMenu = new StickerMenu(Position.BOTTOM_LEFT, mContext, R.drawable.sticker_edit_color_white);
            StickerMenu bottomRightMenu = new StickerMenu(Position.BOTTOM_RIGHT, mContext, R.drawable.sticker_edit_control);
            sticker.addStickerMenu(topLeftMenu);
            sticker.addStickerMenu(topRightMenu);
            sticker.addStickerMenu(bottomLeftMenu);
            sticker.addStickerMenu(bottomRightMenu);
            mStickerDrawOverlay.addSticker(sticker);
        }

        mStickerDrawOverlay.setOnStickerMenuClickListener(new OnStickerMenuClickListener() {
            @Override
            public void onMenuClick(Sticker sticker, StickerMenu menu) {
                ToastUtils.show("click menu " + menu.getPositionType());
            }
        });
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
                                GlideUtils.with(mContext, mPath).into(mImageView);
                                GlideUtils.with(mContext, mPath2).into(mImageView2);
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
//                mPath = mPath2 = imageInfo.getPath();
//                GlideUtils.with(mContext, mPath).into(mImageView);
//                GlideUtils.with(mContext, mPath2).into(mImageView2);

                mDrawMosaicView.setMosaicBackgroundResource(imageInfo.getPath());
                mDrawMosaicView.setMosaicResource(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
                mDrawMosaicView.setMosaicBrushWidth(10);
                mDrawMosaicView.setMosaicType(MosaicUtil.MosaicType.MOSAIC);
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

}
