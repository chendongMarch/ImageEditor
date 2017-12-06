package com.march.picedit.test;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.march.dev.app.activity.BaseActivity;
import com.march.dev.utils.DimensUtils;
import com.march.lightadapter.core.LightAdapter;
import com.march.lightadapter.core.ViewHolder;
import com.march.picedit.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * CreateAt : 2017/12/6
 * Describe :
 *
 * @author chendong
 */
public class TestPorterDuffXfermodeActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.xfermode_activity;
    }

    @BindView(R.id.rv)
    RecyclerView mRecyclerView;

    private List<Integer> mIntegers;

    @Override
    public void onInitDatas() {
        super.onInitDatas();
        mIntegers = new ArrayList<>();
        for (int i = 0; i < PorterDuffXfermodeView.mPorterDuffXfermodeArray.length; i++) {
            mIntegers.add(i);
        }
    }

    @Override
    public void onInitViews(View view, Bundle saveData) {
        super.onInitViews(view, saveData);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new LightAdapter<Integer>(mActivity, mIntegers, R.layout.xfermode_item) {
            @Override
            public void onBindView(ViewHolder<Integer> holder, Integer data, int pos, int type) {
                PorterDuffXfermodeView porterDuffXfermodeView = holder.bindView().getView(R.id.pdxv);
                holder.bindView().setText(R.id.name_tv,PorterDuffXfermodeView.mPorterDuffXfermodeArray[data].name());
                porterDuffXfermodeView.setPorterDuffXfermodeIndex(data);
                holder.bindView().setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }
}
