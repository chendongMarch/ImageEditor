package com.march.picedit.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.ActivityAnimUtils;
import com.march.dev.utils.BitmapUtils;
import com.march.dev.utils.DimensUtils;
import com.march.dev.utils.FileUtils;
import com.march.dev.utils.GlideUtils;
import com.march.dev.utils.PermissionUtils;
import com.march.dev.utils.ShapeUtils;
import com.march.dev.utils.ToastUtils;
import com.march.dev.utils.ViewUtils;
import com.march.dev.widget.TitleBarView;
import com.march.lightadapter.core.LightAdapter;
import com.march.lightadapter.core.ViewHolder;
import com.march.lightadapter.listener.OnHolderUpdateListener;
import com.march.lightadapter.listener.SimpleItemListener;
import com.march.lightadapter.module.SelectorModule;
import com.march.picedit.MainActivity;
import com.march.picedit.R;
import com.march.picedit.Util;
import com.march.piceditor.crop.CropOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

    private String                 mCurrentPicturePath;
    private String                 mOriginPicturePath;
    private LightAdapter<CropMode> mCropModeAdapter;
    private int                    mEnsureColor;
    private int                    mUnsureColor;

    public static void start(Activity activity, String path) {
        Intent intent = new Intent(activity, CropActivity.class);
        intent.putExtra(KEY_PATH, path);
        activity.startActivity(intent);

        ActivityAnimUtils.translateStart(activity);
    }

    @BindView(R.id.iv_image)     ImageView    mImageView;
    @BindView(R.id.fl_parent)    View         mParentFl;
    @BindView(R.id.col_crop)     CropOverlay  mCropOverlay;
    @BindView(R.id.rv_crop_mode) RecyclerView mCropModeRv;
    @BindView(R.id.tv_confirm)   View         mConfirmTv;
    @BindView(R.id.tv_reset)     View         mResetTv;

    @BindView(R.id.iv_tab_crop)   ImageView mCropTabView;
    @BindView(R.id.iv_tab_rotate) ImageView mRotateTabView;

    @Override
    public void onReceiveData() {
        super.onReceiveData();
        mOriginPicturePath = getIntent().getStringExtra(KEY_PATH);
    }

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mTitleBarView.setText(TitleBarView.CENTER, "裁剪");
        mTitleBarView.setText(TitleBarView.LEFT, "返回");
        mTitleBarView.setLeftBackListener(mActivity);

        mCropTabView.setImageDrawable(Util.newSelectDrawable(mContext, R.drawable.img_edit_tab_clip_b, R.drawable.img_edit_tab_clip_a));
        mRotateTabView.setImageDrawable(Util.newSelectDrawable(mContext, R.drawable.img_edit_tab_rotate_b, R.drawable.img_edit_tab_rotate_a));
        mCropTabView.setSelected(true);

        mEnsureColor = ContextCompat.getColor(mContext, R.color.ensureColor);
        mUnsureColor = ContextCompat.getColor(mContext, R.color.unsureColor);

        ViewUtils.setBackground(mConfirmTv, ShapeUtils.getShape(mContext, mEnsureColor, 20));
        ViewUtils.setBackground(mResetTv, ShapeUtils.getShape(mContext, mUnsureColor, 20));

        initCropShow(mOriginPicturePath);

        createCropModeAdapter();
    }


    private void initCropShow(final String path) {
        mCurrentPicturePath = path;
        mParentFl.post(new Runnable() {
            @Override
            public void run() {
                mCropOverlay.reset();
                int width;
                int height;
                int picHeight = mParentFl.getMeasuredHeight();
                BitmapFactory.Options bitmapSize = BitmapUtils.getBitmapSize(mCurrentPicturePath);
                if (bitmapSize.outWidth > bitmapSize.outHeight) {
                    width = (int) (DimensUtils.getScreenWidth(mContext) * 0.9f);
                    height = (int) (width * (bitmapSize.outHeight * 1f / bitmapSize.outWidth));
                } else {
                    height = (int) (picHeight * 0.9f);
                    width = (int) (height * (bitmapSize.outWidth * 1f / bitmapSize.outHeight));
                }
                ViewUtils.setLayoutParam(width, height, mImageView, mCropOverlay);
                GlideUtils.with(mContext, path).into(mImageView);
            }
        });
    }

    @OnClick({R.id.tv_reset, R.id.tv_confirm,
                     R.id.iv_tab_crop, R.id.iv_tab_rotate,
                     R.id.tv_complete, R.id.tv_close})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                Observable.create(new ObservableOnSubscribe<Bitmap>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Bitmap> e) throws Exception {
                        e.onNext(mCropOverlay.crop(mCurrentPicturePath, Bitmap.Config.RGB_565));
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(@NonNull Bitmap bitmap) throws Exception {
                                String absolutePath = FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath();
                                BitmapUtils.compressImage(bitmap, new File(absolutePath), Bitmap.CompressFormat.JPEG, 100, true);
                                initCropShow(absolutePath);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                ToastUtils.show("失败");
                            }
                        });
                break;
            case R.id.tv_reset:
                initCropShow(mOriginPicturePath);
                break;
            case R.id.iv_tab_crop:
                break;
            case R.id.iv_tab_rotate:
                break;
            case R.id.tv_complete:
                break;
            case R.id.tv_close:
                onBackPressed();
                break;
        }
    }

    public class CropMode {
        Drawable drawable;
        String   text;
        float    ratio;

        public CropMode(float ratio, String text, Drawable drawable) {
            this.drawable = drawable;
            this.text = text;
            this.ratio = ratio;
        }
    }

    private void createCropModeAdapter() {
        List<CropMode> list = new ArrayList<>();
        list.add(new CropMode(CropOverlay.NO_ASPECT_RATIO, "free", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_freedom_b, R.drawable.edit_cut_crop_freedom_a)));
        list.add(new CropMode(1f, "1:1", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_1_1_b, R.drawable.edit_cut_crop_1_1_a)));
        list.add(new CropMode(2f / 3, "2:3", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_2_3_b, R.drawable.edit_cut_crop_2_3_a)));
        list.add(new CropMode(3f / 2, "3:2", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_3_2_b, R.drawable.edit_cut_crop_3_2_a)));
        list.add(new CropMode(3f / 4, "3:4", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_3_4_b, R.drawable.edit_cut_crop_3_4_a)));
        list.add(new CropMode(4f / 3, "4:3", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_4_3_b, R.drawable.edit_cut_crop_4_3_a)));
        list.add(new CropMode(9f / 16, "9:16", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_9_16_b, R.drawable.edit_cut_crop_9_16_a)));
        list.add(new CropMode(16f / 9, "16:9", Util.newSelectDrawable(mContext, R.drawable.edit_cut_crop_16_9_b, R.drawable.edit_cut_crop_16_9_a)));

        mCropModeAdapter = new LightAdapter<CropMode>(mContext, list, R.layout.crop_mode_item) {
            @Override
            public void onBindView(ViewHolder<CropMode> holder, CropMode data, int pos, int type) {
                ImageView iv = holder.getView(R.id.iv_icon);
                iv.setImageDrawable(data.drawable);
                holder.text(R.id.tv_desc, data.text);
            }
        };
        mCropModeAdapter.addSelectorModule(new SelectorModule<>(SelectorModule.TYPE_SINGLE, 0, new OnHolderUpdateListener<CropMode>() {
            @Override
            public void onChanged(ViewHolder<CropMode> holder, CropMode data, int pos, boolean isSelect) {
                holder.selectAll(isSelect, R.id.iv_icon);
                holder.textColor(R.id.tv_desc, isSelect ? mEnsureColor : mUnsureColor);
            }
        }));
        mCropModeAdapter.setOnItemListener(new SimpleItemListener<CropMode>() {
            @Override
            public void onClick(int pos, ViewHolder holder, CropMode data) {
                if (!mCropModeAdapter.getSelectorModule().isSelect(data)) {
                    mCropOverlay.setAspectRatio(data.ratio);
                }
                mCropModeAdapter.getSelectorModule().toggle(pos);
            }
        });
        mCropModeRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mCropModeRv.setAdapter(mCropModeAdapter);
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

