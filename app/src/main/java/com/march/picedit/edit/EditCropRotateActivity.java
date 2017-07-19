package com.march.picedit.edit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.march.dev.utils.DrawableUtils;
import com.march.dev.utils.FileUtils;
import com.march.dev.utils.PermissionUtils;
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
import com.march.piceditor.crop.CropOverlay;
import com.march.piceditor.rotate.RotateFrameLayout;
import com.march.turbojpeg.TurboJpegUtils;

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
 * Describe : 裁剪旋转界面
 *
 * @author chendong
 */
public class EditCropRotateActivity extends BaseActivity {

    public static final String KEY_PATH = "KEY_PATH";
    public static final String TAG      = MainActivity.class.getSimpleName();

    private String                 mCurrentPicturePath;
    private String                 mOriginPicturePath;
    private LightAdapter<CropMode> mCropModeAdapter;
    private int                    mEnsureColor;
    private int                    mUnsureColor;

    private Bitmap mCurrentBitmap;

    public static void start(Activity activity, String path) {
        Intent intent = new Intent(activity, EditCropRotateActivity.class);
        intent.putExtra(KEY_PATH, path);
        activity.startActivity(intent);

        ActivityAnimUtils.translateStart(activity);
    }

    @BindView(R.id.fl_parent) FrameLayout mParentFl;

    @BindView(R.id.fl_crop_ly)    FrameLayout mCropFrameLayout;
    @BindView(R.id.iv_crop_image) ImageView   mCropImageView;
    @BindView(R.id.col_crop)      CropOverlay mCropOverlay;

    @BindView(R.id.rfl_rotate_ly)   RotateFrameLayout mRotateFrameLayout;
    @BindView(R.id.iv_rotate_image) ImageView         mRotateImageView;

    @BindView(R.id.rv_crop_mode)   RecyclerView mCropModeRv;
    @BindView(R.id.rv_rotate_mode) RecyclerView mRotateRv;

    @BindView(R.id.tv_crop_confirm) View      mConfirmTv;
    @BindView(R.id.tv_crop_reset)   View      mResetTv;
    @BindView(R.id.iv_tab_crop)     ImageView mCropTabView;
    @BindView(R.id.iv_tab_rotate)   ImageView mRotateTabView;
    @BindView(R.id.ll_crop_ly)      View      mCropLy;

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

        mCropTabView.setImageDrawable(DrawableUtils.newSelectStateDrawable(mContext, R.drawable.img_edit_tab_clip_b, R.drawable.img_edit_tab_clip_a));
        mRotateTabView.setImageDrawable(DrawableUtils.newSelectStateDrawable(mContext, R.drawable.img_edit_tab_rotate_b, R.drawable.img_edit_tab_rotate_a));
        mCropTabView.setSelected(true);

        mEnsureColor = ContextCompat.getColor(mContext, R.color.ensureColor);
        mUnsureColor = ContextCompat.getColor(mContext, R.color.unsureColor);

        ViewUtils.setBackground(mConfirmTv, DrawableUtils.newRoundRectDrawable(mContext, mEnsureColor, 20));
        ViewUtils.setBackground(mResetTv, DrawableUtils.newRoundRectDrawable(mContext, mUnsureColor, 20));

        mCurrentBitmap = BitmapFactory.decodeFile(mOriginPicturePath);

        initCropAndRotateShow(mCurrentBitmap);

        createCropModeAdapter();
        createRotateModeAdapter();
    }


    private int mParentHeight;

    // 初始化裁剪旋转显示
    private void initCropAndRotateShow(final Bitmap bitmap) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                mParentHeight = mParentFl.getMeasuredHeight();
                mCropOverlay.attachImage(bitmap,
                        DimensUtils.getScreenWidth(mContext),
                        mParentHeight, .9f, mCropImageView);
                mCropImageView.setImageBitmap(bitmap);
                mRotateImageView.setImageBitmap(bitmap);
                ViewUtils.setLayoutParam((int) (DimensUtils.getScreenWidth(mContext) * .9f), (int) (mParentHeight * .9f), mRotateImageView);
            }
        };
        if (mParentHeight == 0)
            mParentFl.post(action);
        else action.run();
    }

    @OnClick({R.id.tv_crop_reset, R.id.tv_crop_confirm,
                     R.id.iv_tab_crop, R.id.iv_tab_rotate,
                     R.id.tv_complete, R.id.tv_close})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.tv_crop_confirm:
                // 裁剪生成新图，保存
                Observable.create(new ObservableOnSubscribe<Bitmap>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Bitmap> e) throws Exception {
                        Rect cropRect = mCropOverlay.getCropRect(mCurrentBitmap.getWidth(), mCurrentBitmap.getHeight());
                        Bitmap bitmap = Bitmap.createBitmap(mCurrentBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());

                        e.onNext(bitmap);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(@NonNull Bitmap bitmap) throws Exception {
                                ToastUtils.show("裁剪成功");
                                initCropAndRotateShow(bitmap);
                                BitmapUtils.recycleBitmaps(mCurrentBitmap);
                                mCurrentBitmap = bitmap;
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                ToastUtils.show("裁剪失败");
                            }
                        });
                break;
            case R.id.tv_crop_reset:
                mRotateFrameLayout.setVisibility(View.GONE);
                mRotateFrameLayout.reset();
                Bitmap bitmap = BitmapFactory.decodeFile(mOriginPicturePath);
                initCropAndRotateShow(bitmap);
                BitmapUtils.recycleBitmaps(mCurrentBitmap);
                mCurrentBitmap = bitmap;
                break;
            case R.id.iv_tab_crop:

                // 保存旋转新图
                if (mRotateFrameLayout.isNotOperate()) {
                    mRotateTabView.setSelected(false);
                    mCropTabView.setSelected(true);

                    mRotateRv.setVisibility(View.GONE);
                    mCropLy.setVisibility(View.VISIBLE);

                    mCropFrameLayout.setVisibility(View.VISIBLE);
                    mRotateFrameLayout.setVisibility(View.GONE);
                    mRotateFrameLayout.reset();
                    return;
                }
                Observable.create(new ObservableOnSubscribe<Bitmap>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Bitmap> e) throws Exception {
                        Matrix matrix = new Matrix();
                        matrix.postScale(mRotateFrameLayout.getScaleX(), mRotateFrameLayout.getScaleY());
                        matrix.postRotate(mRotateFrameLayout.getRotate());
                        Bitmap newBitmap = Bitmap.createBitmap(mCurrentBitmap, 0, 0,
                                mCurrentBitmap.getWidth(), mCurrentBitmap.getHeight(), matrix, true);
                        e.onNext(newBitmap);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(@NonNull Bitmap bitmap) throws Exception {
                                ToastUtils.show("旋转成功");

                                initCropAndRotateShow(bitmap);

                                mRotateTabView.setSelected(false);
                                mCropTabView.setSelected(true);

                                mCropFrameLayout.setVisibility(View.VISIBLE);
                                mRotateFrameLayout.setVisibility(View.GONE);

                                mCropLy.setVisibility(View.VISIBLE);
                                mRotateRv.setVisibility(View.GONE);
                                mRotateFrameLayout.reset();


                                BitmapUtils.recycleBitmaps(mCurrentBitmap);
                                mCurrentBitmap = bitmap;

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                ToastUtils.show("旋转失败");
                            }
                        });
                break;
            case R.id.iv_tab_rotate:
                mRotateTabView.setSelected(true);
                mCropTabView.setSelected(false);

                mCropLy.setVisibility(View.INVISIBLE);

                mRotateRv.setVisibility(View.VISIBLE);

                mRotateFrameLayout.setVisibility(View.VISIBLE);
                mCropFrameLayout.setVisibility(View.GONE);
                break;
            case R.id.tv_complete:
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                        TurboJpegUtils.compressBitmap(mCurrentBitmap, 100, FileUtils.newRootFile("result.jpg").getAbsolutePath(), true);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                ToastUtils.show("图片保存成功 - " + s);
                                onBackPressed();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                ToastUtils.show("图片保存失败");
                            }
                        });
                break;
            case R.id.tv_close:
                onBackPressed();
                break;
        }
    }


    // 旋转模式
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


    // 创建旋转菜单 list
    private void createRotateModeAdapter() {
        List<RotateMode> list = new ArrayList<>();
        list.add(new RotateMode(RotateMode.RESET, DrawableUtils.newRoundRectDrawable(mContext, mUnsureColor, 20)));
        list.add(new RotateMode(RotateMode.LEFT, DrawableUtils.newPressedDrawable(mContext, R.drawable.edit_rotate_left_pressed, R.drawable.edit_rotate_left_released)));
        list.add(new RotateMode(RotateMode.RIGHT, DrawableUtils.newPressedDrawable(mContext, R.drawable.edit_rotate_right_pressed, R.drawable.edit_rotate_right_released)));
        list.add(new RotateMode(RotateMode.FLIPX, DrawableUtils.newPressedDrawable(mContext, R.drawable.edit_rotate_flipx_pressed, R.drawable.edit_rotate_flipx_released)));
        list.add(new RotateMode(RotateMode.FLIPY, DrawableUtils.newPressedDrawable(mContext, R.drawable.edit_rotate_flipy_pressed, R.drawable.edit_rotate_flipy_released)));
        LightAdapter<RotateMode> lightAdapter = new LightAdapter<RotateMode>(mContext, list, R.layout.eidt_rotate_mode_item) {
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

    // 裁剪模式列表
    private void createCropModeAdapter() {
        List<CropMode> list = new ArrayList<>();
        list.add(new CropMode(CropOverlay.NO_ASPECT_RATIO, "free", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_freedom_b, R.drawable.edit_cut_crop_freedom_a)));
        list.add(new CropMode(1f, "1:1", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_1_1_b, R.drawable.edit_cut_crop_1_1_a)));
        list.add(new CropMode(2f / 3, "2:3", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_2_3_b, R.drawable.edit_cut_crop_2_3_a)));
        list.add(new CropMode(3f / 2, "3:2", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_3_2_b, R.drawable.edit_cut_crop_3_2_a)));
        list.add(new CropMode(3f / 4, "3:4", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_3_4_b, R.drawable.edit_cut_crop_3_4_a)));
        list.add(new CropMode(4f / 3, "4:3", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_4_3_b, R.drawable.edit_cut_crop_4_3_a)));
        list.add(new CropMode(9f / 16, "9:16", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_9_16_b, R.drawable.edit_cut_crop_9_16_a)));
        list.add(new CropMode(16f / 9, "16:9", DrawableUtils.newSelectStateDrawable(mContext, R.drawable.edit_cut_crop_16_9_b, R.drawable.edit_cut_crop_16_9_a)));

        mCropModeAdapter = new LightAdapter<CropMode>(mContext, list, R.layout.edit_crop_mode_item) {
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
        return R.layout.edit_activity;
    }

    @Override
    public void finish() {
        super.finish();
        ActivityAnimUtils.translateFinish(mActivity);
    }

}

