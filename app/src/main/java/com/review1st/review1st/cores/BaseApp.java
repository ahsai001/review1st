package com.review1st.review1st.cores;

import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.tables.AppListDataModel;
import com.zaitunlabs.zlcore.tables.AppListModel;
import com.zaitunlabs.zlcore.tables.AppListPagingModel;
import com.zaitunlabs.zlcore.tables.BookmarkModel;
import com.zaitunlabs.zlcore.tables.InformationModel;
import com.zaitunlabs.zlcore.tables.StoreDataModel;
import com.zaitunlabs.zlcore.tables.StoreModel;
import com.zaitunlabs.zlcore.tables.StorePagingModel;
import com.zaitunlabs.zlcore.core.BaseApplication;



/**
 * Created by ahsai on 4/20/2018.
 */

public class BaseApp extends BaseApplication {
    @Override
    public void onCreate() {
        APIConstant.setApiAppid("2");
        APIConstant.setApiKey("532ugfuhffjfgh788ghj");
        APIConstant.setApiVersion("v1");
        super.onCreate();
    }
}
