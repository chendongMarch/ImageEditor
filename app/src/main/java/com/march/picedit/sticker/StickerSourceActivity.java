package com.march.picedit.sticker;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.march.common.utils.ActivityAnimUtils;
import com.march.common.utils.DimensUtils;
import com.march.common.utils.LogUtils;
import com.march.lightadapter.LightAdapter;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.LightInjector;
import com.march.lightadapter.helper.LightManager;
import com.march.lightadapter.inject.AdapterLayout;
import com.march.lightadapter.listener.SimpleItemListener;
import com.march.picedit.PicEditActivity;
import com.march.picedit.R;
import com.march.picedit.sticker.model.StickerSource;
import com.march.uikit.annotation.UILayout;
import com.march.uikit.annotation.UITitle;
import com.march.uikit.widget.TitleView;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
/**
 * CreateAt : 7/22/17
 * Describe :
 *
 * @author chendong
 */
@UILayout(R.layout.sticker_source_activity)
@UITitle(titleText = "贴纸一览")
public class StickerSourceActivity extends PicEditActivity {

    public static final String TAG = StickerSourceActivity.class.getSimpleName();


    @BindView(R.id.rv_sticker) RecyclerView                mStickerRv;

    @AdapterLayout(R.layout.sticker_item)
    private LightAdapter<StickerSource> mAdapter;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, StickerSourceActivity.class);
        activity.startActivity(intent);
        ActivityAnimUtils.translateStart(activity);
    }


    public static class StickerSourceEvent {
        public StickerSource mStickerSource;

        public StickerSourceEvent(StickerSource stickerSource) {
            mStickerSource = stickerSource;
        }

        public void post() {
            EventBus.getDefault().post(this);
        }
    }

    @Override
    public void initAfterViewCreated() {
        super.initAfterViewCreated();
        mViewDelegate.setTitleText(TitleView.LEFT, "返回");
        mViewDelegate.getTitleView().setLeftBackListener(getActivity());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("sticker.json")));
            Type type = new TypeToken<List<StickerSource>>() {
            }.getType();
            List<StickerSource> mStickerSources = new Gson().fromJson(reader, type);
            createAdapter(mStickerSources);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createAdapter(List<StickerSource> list) {
        LogUtils.e(TAG, Thread.currentThread().getName());
        mAdapter = new LightAdapter<StickerSource>(getContext(), list) {
            int spanCount = 3;
            int size = (int) (DimensUtils.WIDTH * 1f / spanCount);

            @Override
            public void onBindView(LightHolder holder, final StickerSource data, int pos, int type) {
                holder
                        .setLayoutParams(size, size)
                        .setCallback(R.id.iv_image, new LightHolder.Callback<ImageView>() {
                            @Override
                            public void bind(LightHolder holder, ImageView view, int pos) {
                                Glide.with(getContext()).load(data.getSourceUrl()).into(view);
                            }
                        });
            }
        };
        mAdapter.setOnItemListener(new SimpleItemListener<StickerSource>() {
            @Override
            public void onClick(int pos, LightHolder holder, StickerSource data) {
                new StickerSourceEvent(data).post();
                onBackPressed();
            }
        });
        LightInjector.initAdapter(mAdapter,this,mStickerRv, LightManager.vGrid(getContext(),3));
    }
}
