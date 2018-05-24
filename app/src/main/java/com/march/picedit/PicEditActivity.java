package com.march.picedit;

import android.graphics.BitmapFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.march.picedit.sticker.StickerSourceActivity;
import com.march.uikit.app.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.ButterKnife;

/**
 * CreateAt : 2018/5/23
 * Describe :
 *
 * @author chendong
 */

public class PicEditActivity extends BaseActivity{

    @Override
    public void initAfterViewCreated() {
        super.initAfterViewCreated();
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PicEditActivity event) {

    }
}
