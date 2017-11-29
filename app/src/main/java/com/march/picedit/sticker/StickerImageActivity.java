package com.march.picedit.sticker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.ActivityAnimUtils;
import com.march.dev.utils.GlideUtils;
import com.march.picedit.R;
import com.march.piceditor.functions.sticker.EasyMenuHandler;
import com.march.piceditor.functions.sticker.StickerDrawOverlay;
import com.march.piceditor.functions.sticker.model.Position;
import com.march.piceditor.functions.sticker.model.Sticker;
import com.march.piceditor.functions.sticker.model.StickerMenu;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * CreateAt : 7/22/17
 * Describe :
 *
 * @author chendong
 */
public class StickerImageActivity extends BaseActivity {

    public static final String KEY_PATH = "KEY_PATH";

    @Override
    protected int getLayoutId() {
        return R.layout.sticker_image_activity;
    }

    @BindView(R.id.iv_image) ImageView          mImageView;
    @BindView(R.id.sdo)      StickerDrawOverlay mStickerDrawOverlay;

    private String          mImagePath;
    private ResourceFactory mResourceFactory;

    public static void start(Activity activity, String path) {
        Intent intent = new Intent(activity, StickerImageActivity.class);
        intent.putExtra(KEY_PATH, path);
        activity.startActivity(intent);
        ActivityAnimUtils.translateStart(activity);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StickerSourceActivity.StickerSourceEvent event) {
        Glide.with(mContext).load(event.mStickerSource.getSourceUrl())
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        addSticker(BitmapFactory.decodeFile(resource.getAbsolutePath()));
                    }
                });
    }

    @OnClick({R.id.tv_sticker, R.id.tv_sticker_big, R.id.tv_sticker_small})
    public void clickView(View view) {
        switch (view.getId()) {
            case R.id.tv_sticker:
                StickerSourceActivity.start(mActivity);
                break;
            case R.id.tv_sticker_big:
                mStickerDrawOverlay.getActiveSticker().postMatrixScale(1.1f, 1.1f);
                mStickerDrawOverlay.invalidate();
                break;
            case R.id.tv_sticker_small:
                mStickerDrawOverlay.getActiveSticker().postMatrixScale(1f, 1f);
                mStickerDrawOverlay.invalidate();
                break;
        }
    }


    @Override
    public void onReceiveData() {
        super.onReceiveData();
        mImagePath = getIntent().getStringExtra(KEY_PATH);
    }

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mTitleBarView.setText("返回", "贴纸", null);
        mTitleBarView.setLeftBackListener(mActivity);
        mResourceFactory = new ResourceFactory(mContext);
        GlideUtils.with(mContext, mImagePath)
                .into(mImageView);
    }


    private void addSticker(Bitmap bitmap) {
        Sticker sticker = new Sticker(bitmap);

        // delete handler & tag
        StickerMenu topLeftMenu = new StickerMenu(Position.TOP_LEFT, mResourceFactory.decodeDrawable(R.drawable.sticker_edit_del));
        topLeftMenu.setTag(100);
        topLeftMenu.setStickerMenuHandler(EasyMenuHandler.DELETE_STICKER);

        // flip vertical handler
        StickerMenu topRightMenu = new StickerMenu(Position.TOP_RIGHT, mResourceFactory.decodeDrawable(R.drawable.sticker_edit_symmetry));
        topRightMenu.setStickerMenuHandler(EasyMenuHandler.FLIP_HORIZONTAL);

        StickerMenu bottomLeftMenu = new StickerMenu(Position.BOTTOM_LEFT, mResourceFactory.decodeDrawable(R.drawable.sticker_edit_color_white));
        StickerMenu bottomRightMenu = new StickerMenu(Position.BOTTOM_RIGHT, mResourceFactory.decodeDrawable(R.drawable.sticker_edit_control));

        // add menu
        sticker.addStickerMenu(topLeftMenu, topRightMenu, bottomLeftMenu, bottomRightMenu);

        // sticker color filter
        // sticker.setColorFilter(new Random().nextInt(225), new Random().nextInt(225), new Random().nextInt(225));
        // init position & init scale
        sticker.setInitTranslate(250 + new Random().nextInt(350), 250 + new Random().nextInt(350));
        sticker.setInitScale(1f);
        // min size & max size
        sticker.setMinSize(100);
        // sticker.setMaxSize(1000);

        sticker.setAutoLifting(true);
        mStickerDrawOverlay.addSticker(sticker, true);
    }
}
