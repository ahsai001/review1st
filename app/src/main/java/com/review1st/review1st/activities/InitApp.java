package com.review1st.review1st.activities;

import android.os.Bundle;

import com.review1st.review1st.R;
import com.zaitunlabs.zlcore.activities.BaseSplashActivity;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.utils.CommonUtils;

/**
 * Created by ahsai on 4/20/2018.
 */

public class InitApp extends BaseSplashActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundPaneColor(R.color.colorPrimaryDark);
        setImageIcon(R.mipmap.ic_launcher);
        setBottomTextView(getString(R.string.app_name)+" versi "+ CommonUtils.getVersionName(this), android.R.color.white);
    }

    @Override
    protected String getCheckVersionUrl() {
        return APIConstant.API_CHECK_VERSION;
    }

    @Override
    protected void doNextAction() {
        HomeActivity.start(this);
    }

    @Override
    protected int getMinimumSplashTimeInMS() {
        return 3000;
    }
}
