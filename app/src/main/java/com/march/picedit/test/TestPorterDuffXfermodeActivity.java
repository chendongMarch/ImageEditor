package com.march.picedit.test;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.march.lightadapter.LightAdapter;
import com.march.lightadapter.LightHolder;
import com.march.lightadapter.LightInjector;
import com.march.lightadapter.helper.LightManager;
import com.march.picedit.R;
import com.march.uikit.annotation.UILayout;
import com.march.uikit.app.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * CreateAt : 2017/12/6
 * Describe :
 *
 * @author chendong
 */
@UILayout(R.layout.xfermode_activity)
public class TestPorterDuffXfermodeActivity extends BaseActivity {


    @BindView(R.id.rv)
    RecyclerView mRecyclerView;

    private List<Integer> mIntegers;

    @Override
    public void initBeforeViewCreated() {
        super.initBeforeViewCreated();
        mIntegers = new ArrayList<>();
        for (int i = 0; i < PorterDuffXfermodeView.mPorterDuffXfermodeArray.length; i++) {
            mIntegers.add(i);
        }
    }

    @Override
    public void initAfterViewCreated() {
        super.initAfterViewCreated();
        LightAdapter<Integer> adapter = new LightAdapter<Integer>(getActivity(), mIntegers, R.layout.xfermode_item) {
            @Override
            public void onBindView(LightHolder holder, Integer data, int pos, int type) {
                PorterDuffXfermodeView porterDuffXfermodeView = holder.getView(R.id.pdxv);
                holder.setText(R.id.name_tv, PorterDuffXfermodeView.mPorterDuffXfermodeArray[data].name());
                porterDuffXfermodeView.setPorterDuffXfermodeIndex(data);
                holder.setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        LightInjector.initAdapter(adapter, this, mRecyclerView, LightManager.vGrid(getContext(), 3));
    }
}
