package com.march.picedit;

import com.antfortune.freeline.FreelineCore;
import com.march.dev.app.BaseApplication;
import com.march.dev.utils.CrashUtils;

/**
 * CreateAt : 7/17/17
 * Describe :
 *
 * @author chendong
 */
public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FreelineCore.init(this);

        CrashUtils.init(new CrashUtils.OnCrashListener() {
            @Override
            public void onCrash(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
