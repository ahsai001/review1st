package com.review1st.review1st.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.zaitunlabs.zlcore.utils.PrefsData;

/**
 * Created by ahsai on 8/22/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        PrefsData.setPushyToken(refreshedToken);
        PrefsData.setPushyTokenSent(false);
    }
}
