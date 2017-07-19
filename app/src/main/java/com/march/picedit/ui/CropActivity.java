package com.march.picedit.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
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
import com.march.piceditor.rotate.RotateFrameLayout;

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
import io.reactivex.functions.Function;
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

    @BindView(R.id.iv_image)       ImageView         mImageView;
    @BindView(R.id.fl_parent)      FrameLayout       mParentFl;
    @BindView(R.id.rfl_image)      RotateFrameLayout mRotateFrameLayout;
    @BindView(R.id.col_crop)       CropOverlay       mCropOverlay;
    @BindView(R.id.rv_crop_mode)   RecyclerView      mCropModeRv;
    @BindView(R.id.rv_rotate_mode) RecyclerView      mRotateRv;
    @BindView(R.id.tv_confirm)     View              mConfirmTv;
    @BindView(R.id.tv_reset)       View              mResetTv;

    @BindView(R.id.iv_tab_crop)   ImageView mCropTabView;
    @BindView(R.id.iv_tab_rotate) ImageView mRotateTabView;
    @BindView(R.id.ll_crop_ly)    View      mCropLy;

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

        mCropTabView.setImageDrawable(Util.newSelectedDrawable(mContext, R.drawable.img_edit_tab_clip_b, R.drawable.img_edit_tab_clip_a));
        mRotateTabView.setImageDrawable(Util.newSelectedDrawable(mContext, R.drawable.img_edit_tab_rotate_b, R.drawable.img_edit_tab_rotate_a));
        mCropTabView.setSelected(true);

        mEnsureColor = ContextCompat.getColor(mContext, R.color.ensureColor);
        mUnsureColor = ContextCompat.getColor(mContext, R.color.unsureColor);

        ViewUtils.setBackground(mConfirmTv, ShapeUtils.getShape(mContext, mEnsureColor, 20));
        ViewUtils.setBackground(mResetTv, ShapeUtils.getShape(mContext, mUnsureColor, 20));

        initCropShow(mOriginPicturePath);

        createCropModeAdapter();
        createRotateModeAdapter();
    }


    private void initCropShow(final String path) {
        mCurrentPicturePath = path;
        mParentFl.post(new Runnable() {
            @Override
            public void run() {
                mCropOverlay.attachImage(path, mImageView,
                        DimensUtils.getScreenWidth(mContext),
                        mParentFl.getHeight(), .9f);
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
                saveImage();
//                Observable.create(new ObservableOnSubscribe<Bitmap>() {
//                    @Override
//                    public void subscribe(@NonNull ObservableEmitter<Bitmap> e) throws Exception {
//                        e.onNext(mCropOverlay.crop(mCurrentPicturePath, Bitmap.Config.RGB_565));
//                    }
//                }).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Consumer<Bitmap>() {
//                            @Override
//                            public void accept(@NonNull Bitmap bitmap) throws Exception {
//                                String absolutePath = FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath();
//                                BitmapUtils.compressImage(bitmap, new File(absolutePath), Bitmap.CompressFormat.JPEG, 100, true);
//                                initCropShow(absolutePath);
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(@NonNull Throwable throwable) throws Exception {
//                                ToastUtils.show("失败");
//                            }
//                        });
                break;
            case R.id.tv_reset:
                mRotateFrameLayout.resetWithAnimation();
                mCropOverlay.setVisibility(View.GONE);
                mImageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCropOverlay.setVisibility(View.VISIBLE);
                        initCropShow(mOriginPicturePath);
                    }
                }, 300);
                break;
            case R.id.iv_tab_crop:
                mRotateTabView.setSelected(false);
                mCropTabView.setSelected(true);
                mRotateRv.setVisibility(View.GONE);
                mCropLy.setVisibility(View.VISIBLE);
                mImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRotateFrameLayout.getRotate() % 180 == 0) {
                            ViewUtils.setLayoutParam(mImageView.getMeasuredWidth(), mImageView.getMeasuredHeight(), mCropOverlay);
                        } else {
                            ViewUtils.setLayoutParam(mImageView.getMeasuredHeight(), mImageView.getMeasuredWidth(), mCropOverlay);
                        }
                        mCropOverlay.reset();
                        mCropOverlay.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case R.id.iv_tab_rotate:
                mRotateTabView.setSelected(true);
                mCropTabView.setSelected(false);
                mRotateRv.setVisibility(View.VISIBLE);
                mCropLy.setVisibility(View.INVISIBLE);
                mCropOverlay.setVisibility(View.GONE);
                break;
            case R.id.tv_complete:
                onBackPressed();
                break;
            case R.id.tv_close:
                onBackPressed();
                break;
        }
    }


    private class RotateMode {
        public static final int RESET = 0;
        public static final int LEFT  = 1;
        public static final int RIGHT = 2;
        public static final int FLIPX = 3;
        public static final int FLIPY = 4;

        Drawable drawable;
        int      type;

        public RotateMode(int type, Drawable drawable) {
            this.drawable = drawable;
            this.type = type;
        }
    }

    private void createRotateModeAdapter() {
        List<RotateMode> list = new ArrayList<>();
        list.add(new RotateMode(RotateMode.RESET, ShapeUtils.getShape(mContext, mUnsureColor, 20)));
        list.add(new RotateMode(RotateMode.LEFT, Util.newPressedDrawable(mContext, R.drawable.edit_rotate_left_pressed, R.drawable.edit_rotate_left_released)));
        list.add(new RotateMode(RotateMode.RIGHT, Util.newPressedDrawable(mContext, R.drawable.edit_rotate_right_pressed, R.drawable.edit_rotate_right_released)));
        list.add(new RotateMode(RotateMode.FLIPX, Util.newPressedDrawable(mContext, R.drawable.edit_rotate_flipx_pressed, R.drawable.edit_rotate_flipx_released)));
        list.add(new RotateMode(RotateMode.FLIPY, Util.newPressedDrawable(mContext, R.drawable.edit_rotate_flipy_pressed, R.drawable.edit_rotate_flipy_released)));
        LightAdapter<RotateMode> lightAdapter = new LightAdapter<RotateMode>(mContext, list, R.layout.rotate_item) {
            @Override
            public void onBindView(ViewHolder<RotateMode> holder, RotateMode data, int pos, int type) {
                holder.layoutParams(DimensUtils.getScreenWidth(mContext) / 5, ViewHolder.UNSET);
                if (data.type == RotateMode.RESET) {
                    holder.gone(R.id.iv_icon)
                            .visible(R.id.tv_desc);
                    ViewUtils.setBackground(holder.getView(R.id.tv_desc), data.drawable);
                } else {
                    holder.visible(R.id.iv_icon)
                            .gone(R.id.tv_desc);
                    holder.<ImageView>getView(R.id.iv_icon).setImageDrawable(data.drawable);
                }
            }
        };
        lightAdapter.setOnItemListener(new SimpleItemListener<RotateMode>() {
            @Override
            public void onClick(int pos, ViewHolder holder, RotateMode data) {
                switch (data.type) {
                    case RotateMode.RESET:
                        mRotateFrameLayout.resetWithAnimation();
                        break;
                    case RotateMode.LEFT:
                        mRotateFrameLayout.rotateLeft();
                        break;
                    case RotateMode.RIGHT:
                        mRotateFrameLayout.rotateRight();
                        break;
                    case RotateMode.FLIPX:
                        mRotateFrameLayout.flipX();
                        break;
                    case RotateMode.FLIPY:
                        mRotateFrameLayout.flipY();
                        break;
                }
            }
        });
        mRotateRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRotateRv.setAdapter(lightAdapter);
    }

    private class CropMode {
        Drawable drawable;
        String   text;
        float    ratio;

        CropMode(float ratio, String text, Drawable drawable) {
            this.drawable = drawable;
            this.text = text;
            this.ratio = ratio;
        }
    }

    private void createCropModeAdapter() {
        List<CropMode> list = new ArrayList<>();
        list.add(new CropMode(CropOverlay.NO_ASPECT_RATIO, "free", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_freedom_b, R.drawable.edit_cut_crop_freedom_a)));
        list.add(new CropMode(1f, "1:1", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_1_1_b, R.drawable.edit_cut_crop_1_1_a)));
        list.add(new CropMode(2f / 3, "2:3", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_2_3_b, R.drawable.edit_cut_crop_2_3_a)));
        list.add(new CropMode(3f / 2, "3:2", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_3_2_b, R.drawable.edit_cut_crop_3_2_a)));
        list.add(new CropMode(3f / 4, "3:4", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_3_4_b, R.drawable.edit_cut_crop_3_4_a)));
        list.add(new CropMode(4f / 3, "4:3", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_4_3_b, R.drawable.edit_cut_crop_4_3_a)));
        list.add(new CropMode(9f / 16, "9:16", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_9_16_b, R.drawable.edit_cut_crop_9_16_a)));
        list.add(new CropMode(16f / 9, "16:9", Util.newSelectedDrawable(mContext, R.drawable.edit_cut_crop_16_9_b, R.drawable.edit_cut_crop_16_9_a)));

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

    private void saveImage() {
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Bitmap> e) throws Exception {
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPicturePath);
                Matrix matrix = new Matrix();
                matrix.postScale(mRotateFrameLayout.getScaleX(), mRotateFrameLayout.getScaleY());
                matrix.postRotate(mRotateFrameLayout.getRotation());
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                BitmapUtils.recycleBitmaps(bitmap);
                e.onNext(newBitmap);
            }
        }).map(new Function<Bitmap, String>() {
            @Override
            public String apply(@NonNull Bitmap bitmap) throws Exception {
                String absolutePath = FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath();
                BitmapUtils.compressImage(bitmap, new File(absolutePath), Bitmap.CompressFormat.JPEG, 100, true);
                return absolutePath;
            }
        }).map(new Function<String, Bitmap>() {
            @Override
            public Bitmap apply(@NonNull String path) throws Exception {
                // 生成decoder对象
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(path, true);
                final int imgWidth = decoder.getWidth();
                final int imgHeight = decoder.getHeight();
                Rect rect = mCropOverlay.getCropRect(imgWidth, imgHeight);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                // 为了内存考虑，将图片格式转化为RGB_565
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                // 将矩形区域解码生成要加载的Bitmap对象
                return decoder.decodeRegion(rect, opts);
            }
        }).map(new Function<Bitmap, String>() {
            @Override
            public String apply(@NonNull Bitmap bitmap) throws Exception {
                String absolutePath = FileUtils.newRootFile(System.currentTimeMillis() + ".jpg").getAbsolutePath();
                BitmapUtils.compressImage(bitmap, new File(absolutePath), Bitmap.CompressFormat.JPEG, 100, true);
                return absolutePath;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        initCropShow(s);
                        ToastUtils.show("成功");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
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

