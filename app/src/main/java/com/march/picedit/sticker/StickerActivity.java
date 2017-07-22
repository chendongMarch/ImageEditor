package com.march.picedit.sticker;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.DimensUtils;
import com.march.dev.utils.GlideUtils;
import com.march.dev.utils.LogUtils;
import com.march.lightadapter.core.LightAdapter;
import com.march.lightadapter.core.ViewHolder;
import com.march.lightadapter.listener.SimpleItemListener;
import com.march.picedit.R;
import com.march.picedit.sticker.model.StickerSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * CreateAt : 7/22/17
 * Describe :
 *
 * @author chendong
 */
public class StickerActivity extends BaseActivity {

    private LightAdapter<StickerSource> mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.sticker_activity;
    }

    @BindView(R.id.rv_sticker) RecyclerView mStickerRv;

    private List<StickerSource> mStickerSources;

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mTitleBarView.setText("返回", "贴纸一览", null);
        mTitleBarView.setLeftBackListener(mActivity);
        Observable.create(new ObservableOnSubscribe<List<StickerSource>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<StickerSource>> e) throws Exception {
                mStickerSources = new Gson().fromJson(
                        new BufferedReader(new InputStreamReader(
                                getAssets().open("sticker.json"))),
                        new TypeToken<List<StickerSource>>() {
                        }.getType());
                e.onNext(mStickerSources);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<StickerSource>>() {
                    @Override
                    public void accept(@NonNull List<StickerSource> stickerSources) throws Exception {
                        createAdapter(stickerSources);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }


    public static final String TAG = StickerActivity.class.getSimpleName();

    private void createAdapter(List<StickerSource> list) {
        LogUtils.e(TAG, Thread.currentThread().getName());
        mAdapter = new LightAdapter<StickerSource>(mContext, list, R.layout.sticker_item) {
            int spanCount = 3;
            int size = (int) (DimensUtils.getScreenWidth(mContext) * 1f / spanCount);

            @Override
            public void onBindView(ViewHolder<StickerSource> holder, StickerSource data, int pos, int type) {
                holder.layoutParams(size, size);
                GlideUtils.with(mContext, data.getSourceUrl())
                        .size(size)
                        .into((ImageView) holder.getView(R.id.iv_image));
            }
        };
        mAdapter.setOnItemListener(new SimpleItemListener<StickerSource>() {
            @Override
            public void onClick(int pos, ViewHolder holder, StickerSource data) {

            }
        });
        mStickerRv.setLayoutManager(new GridLayoutManager(mContext, 3, LinearLayoutManager.VERTICAL, false));
        mStickerRv.setAdapter(mAdapter);
    }
}
