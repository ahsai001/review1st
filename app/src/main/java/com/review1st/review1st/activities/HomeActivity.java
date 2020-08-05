package com.review1st.review1st.activities;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.review1st.review1st.R;
import com.review1st.review1st.configs.AppConfig;
import com.zaitunlabs.zlcore.activities.AppListActivity;
import com.zaitunlabs.zlcore.activities.BookmarkListActivity;
import com.zaitunlabs.zlcore.activities.MessageListActivity;
import com.zaitunlabs.zlcore.activities.StoreActivity;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.events.GeneralWebviewEvent;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.events.InfoCounterEvent;
import com.zaitunlabs.zlcore.events.ShowBookmarkInfoEvent;
import com.zaitunlabs.zlcore.fragments.GeneralWebViewFragment;
import com.zaitunlabs.zlcore.modules.about.AboutUs;
import com.zaitunlabs.zlcore.services.*;
import com.zaitunlabs.zlcore.tables.InformationModel;
import com.zaitunlabs.zlcore.utils.CommonUtil;
import com.zaitunlabs.zlcore.utils.EventsUtil;
import com.zaitunlabs.zlcore.utils.LinkUtil;
import com.zaitunlabs.zlcore.utils.PermissionUtil;
import com.zaitunlabs.zlcore.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private WebViewFragment newFragment;
    private TextView messageItemView;
    private PermissionUtil permissionUtils;
    private String titleWebPage;
    private String descWebPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(null);

        permissionUtils = PermissionUtil.checkPermissionAndGo(this, 1122, false, null, null,
                new Runnable() {
                    @Override
                    public void run() {
                        WebViewFragment oldFragment = (WebViewFragment) getSupportFragmentManager().findFragmentByTag(GeneralWebViewFragment.FRAGMENT_TAG);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        if (oldFragment != null) {
                            transaction.remove(oldFragment);
                        }
                        transaction.commit();
                        transaction = null;


                        transaction = getSupportFragmentManager().beginTransaction();
                        newFragment = new WebViewFragment();
                        ArrayList<String> whiteListDomains = new ArrayList<String>();
                        whiteListDomains.add("www.review1st.id");
                        whiteListDomains.add("review1st.id");
                        whiteListDomains.add("review1st.com");
                        whiteListDomains.add("www.review1st.com");
                        newFragment.setArg(HomeActivity.this, 0, null,AppConfig.mainURL,"Oops, ada masalah, ulangi kembali",-1,false,null, whiteListDomains);
                        transaction.replace(R.id.home_container, newFragment, WebViewFragment.FRAGMENT_TAG);
                        transaction.commit();

                    }
                }, new Runnable() {
                    @Override
                    public void run() {

                    }
                }, Manifest.permission.READ_PHONE_STATE);

        messageItemView = (TextView) navigationView.getMenu().
                findItem(R.id.nav_message).getActionView();

        EventsUtil.register(this);
        reCountMessage();

        handleIntentFromAppLink(getIntent());

        MobileAds.initialize(this, "ca-app-pub-3647411985348830~9863330893");
        AdView mAdView = findViewById(R.id.home_addview);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("3AF746CE6613CC66CCB427F060752FDE")
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FCMIntentService.startSending(this, APIConstant.API_APPID, false,false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Subscribe
    public void onEvent(InfoCounterEvent event){
        reCountMessage();
    }

    @Subscribe
    public void onEvent(ShowBookmarkInfoEvent event){
        newFragment.openNewLinkOrContent(event.getLink());
    }

    @Subscribe
    public void onEvent(GeneralWebviewEvent event){
        if(event.getEventType() == GeneralWebviewEvent.LOAD_PAGE_FINISHED){
            newFragment.runJavascript("(function() { " +
                    "document.getElementById('masthead').style.display='none';})()");
            newFragment.runJavascript("(function() { " +
                    "document.getElementById('colophon').style.display='none';})()");

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventsUtil.unregister(this);
    }

    private void reCountMessage(){
        if(messageItemView != null) {
            messageItemView.setGravity(Gravity.CENTER_VERTICAL);
            messageItemView.setTypeface(null, Typeface.BOLD);
            messageItemView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            messageItemView.setText(""+ InformationModel.unreadInfoCount());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntentFromAppLink(intent);
    }

    private void handleIntentFromAppLink(Intent intent){
        String action = intent.getAction();
        Uri data = intent.getData();

        if(action != null && action.equalsIgnoreCase(Intent.ACTION_VIEW)){
            newFragment.openNewLinkOrContent(LinkUtil.getUrlFromUri(data));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();

        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Enter keywords");

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.requestFocus();
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
                        toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                InputMethodManager.HIDE_IMPLICIT_ONLY);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.clearFocus();
                CommonUtil.hideKeyboard(HomeActivity.this);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(newFragment != null) {
                    try {
                        newFragment.openNewLinkOrContent("https://www.review1st.com/?s="+CommonUtil.urlEncode(query));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                searchView.clearFocus();
                CommonUtil.hideKeyboard(HomeActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_browser) {
            CommonUtil.openBrowser(this,AppConfig.mainBrowserURL);
            return true;
        } else if(id == android.R.id.home){
            if(newFragment != null && newFragment.navigateBack()){
                return true;
            }
        } else if(id == R.id.action_home_share){
            if(newFragment != null && newFragment.isVisible()
                    && !TextUtils.isEmpty(titleWebPage) && !TextUtils.isEmpty(descWebPage)){
                CommonUtil.shareContent(HomeActivity.this, "share via",
                        titleWebPage, descWebPage+"\n\n"+"for more info\n\n"+newFragment.getCurrentUrl());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(newFragment != null && newFragment.onKeyDown(keyCode,event)){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            AboutUs.start(this,R.mipmap.ic_launcher,0,R.string.share_title,R.string.share_body_template,
                    0,R.string.feedback_mail_to, R.string.feedback_title, R.string.feedback_body_template,
                    0,R.raw.version_change_history, true, AppConfig.appLandingURL,
                    false, "Review1st", AppConfig.mainURL,getString(R.string.feedback_mail_to),R.mipmap.ic_launcher,"2018\nAll right reserved",
                    R.color.colorPrimary,ContextCompat.getColor(this,android.R.color.white),ContextCompat.getColor(this,android.R.color.white),AppConfig.aboutAppURL,false);
        } else if (id == R.id.nav_app_list) {
            AppListActivity.start(this,false);
        } else if (id == R.id.nav_store) {
            StoreActivity.start(this,false);
        } else if (id == R.id.nav_message) {
            MessageListActivity.start(this,false);
        } else if (id == R.id.nav_bookmark_list){
            BookmarkListActivity.start(this);
        } else if (id == R.id.nav_home){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com?clearhistory");
        } else if (id == R.id.nav_ig){
            CommonUtil.openUrlWithPackageName(this,"https://www.instagram.com/review1st","com.instagram.android");
        } else if (id == R.id.nav_youtube){
            CommonUtil.openUrlWithPackageName(this, "https://www.youtube.com/c/review1stdotcom", "com.google.android.youtube");
        } else if (id == R.id.nav_reviews){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/tes?clearhistory");
        } else if (id == R.id.nav_news_teknologi){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/news/teknologi?clearhistory");
        } else if (id == R.id.nav_news_infografik){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/news/infografik?clearhistory");
        } else if (id == R.id.nav_news_telekomunikasi){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/news/telekomunikasi?clearhistory");
        } else if (id == R.id.nav_news_rumor){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/news/rumor?clearhistory");
        } else if (id == R.id.nav_aplikasi){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/game-aplikasi?clearhistory");
        } else if (id == R.id.nav_buyers_guide) {
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/harga-spesifikasi?clearhistory");
        } else if (id == R.id.nav_hp_baru) {
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/hp?clearhistory");
        } else if (id == R.id.nav_gadget) {
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/gadget?clearhistory");
        } else if (id == R.id.nav_kontak){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/contact-us?clearhistory");
        } else if (id == R.id.nav_tipstrik){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/category/tipstrik?clearhistory");
        } else if (id == R.id.nav_tentang_kami){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.com/about?clearhistory");
        } else if (id == R.id.nav_spesifikasi_mobil){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.id/spesifikasi/mobil?clearhistory");
        } else if (id == R.id.nav_spesifikasi_smartphone){
            if(newFragment != null) newFragment.openNewLinkOrContent("https://www.review1st.id/spesifikasi/smartphone?clearhistory");
        }

        if(newFragment == null){
            CommonUtil.showDialog1Option(HomeActivity.this, "Informasi", "Mohon untuk memberikan permission read phone state", null, null);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void start(Context context){
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    public void onClickDrawerItem(MenuItem menuItem){

    }


    public static class WebViewFragment extends GeneralWebViewFragment{
        @Override
        protected View getCustomProgressBar() {
            return null;
        }

        @Override
        protected View getCustomInfoView() {
            return null;
        }

        @Override
        protected int getCustomInfoTextView() {
            return 0;
        }


        @Override
        public void setupWebview(WebView webView) {
            super.setupWebview(webView);
            webView.addJavascriptInterface(new WebAppInterface(this.getActivity()), "review1st");
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected boolean handleCustomLink(WebView view, WebResourceRequest request) {
            handleCustomLink(view, request.getUrl().toString());
            return true;
        }

        @Override
        protected boolean handleCustomLink(WebView view, String url) {
            if(url.contains("https://www.instagram.com")){
                CommonUtil.openUrlWithPackageName(view.getContext(),url,"com.instagram.android");
            } else if(url.contains("https://www.youtube.com")){
                CommonUtil.openUrlWithPackageName(view.getContext(), url, "com.google.android.youtube");
            }
            return true;
        }

        private class WebAppInterface {
            Activity activity;

            /** Instantiate the interface and set the context */
            WebAppInterface(Activity c) {
                activity = c;
            }

            /** Show a toast from the web page */
            @JavascriptInterface
            public void showToast(String toast) {
                CommonUtil.showToast(activity,toast);
            }


            @JavascriptInterface
            public void showInfo(String title, String info) {
                CommonUtil.showInfo(activity,title,info);
            }

            @JavascriptInterface
            public void giveInfo(String title, String info) {
                ((HomeActivity)activity).titleWebPage = title;
                ((HomeActivity)activity).descWebPage = info;
            }

            @JavascriptInterface
            public void openBrowser(String link) {
                CommonUtil.openBrowser(activity, link);
            }

            @JavascriptInterface
            public void webDescription(String desc) {
                //Toast.makeText(activity.getBaseContext(), desc, Toast.LENGTH_SHORT).show();
            }

            @JavascriptInterface
            public void showActionBar(String title) {
                if(!TextUtils.isEmpty(title)){
                    ((HomeActivity)activity).getSupportActionBar().setTitle(title);
                }
                ((HomeActivity)activity).getSupportActionBar().show();
            }

            @JavascriptInterface
            public void reload(){
            }

            @JavascriptInterface
            public String getVersionName(){
                return CommonUtil.getVersionName(activity);
            }


            @JavascriptInterface
            public void openMaps(final String latLong){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.navigateGMaps(activity,latLong);

                    }
                });

            }

        }

    }
}
