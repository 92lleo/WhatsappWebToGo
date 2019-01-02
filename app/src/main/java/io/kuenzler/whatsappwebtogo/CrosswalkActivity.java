package io.kuenzler.whatsappwebtogo;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;


public class CrosswalkActivity extends XWalkActivity {
    XWalkView mXWalkView;

    private static final String uaChrome = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    private static final String device = "Linux; U; Android " + Build.VERSION.RELEASE;
    private static final String browser = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    private static final String userAgent = "Mozilla/5.0 (" + device + ") " + browser;

    private static final String   CAMERA_PERMISSION  = Manifest.permission.CAMERA;                 // "android.permission.CAMERA";
    private static final String   AUDIO_PERMISSION   = Manifest.permission.RECORD_AUDIO;           // "android.permission.RECORD_AUDIO";
    private static final String   STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE; //android.permission.WRITE_EXTERNAL_STORAGE
    private static final String[] VIDEO_PERMISSION   = {CAMERA_PERMISSION, AUDIO_PERMISSION};

    private static final String WHATSAPP_WEB_URL = "https://web.whatsapp.com";
    private final Activity ACTIVITY = this;

    private static final int FILECHOOSER_RESULTCODE = 200;
    private static final int CAMERA_PERMISSION_RESULTCODE = 201;
    private static final int AUDIO_PERMISSION_RESULTCODE = 202;
    private static final int VIDEO_PERMISSION_RESULTCODE = 203;
    private static final int STORAGE_PERMISSION_RESULTCODE = 204;

    private static final String DEBUG_TAG = "WAWEBTOGO";

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mXWalkView.restoreState(savedInstanceState);
        }

        bundle = savedInstanceState;

        // Until onXWalkReady() is invoked, you should do nothing with the
        // embedding API except the following:
        // 1. Instanciate the XWalkView object
        // 2. Call XWalkPreferences.setValue()
        // 3. Call XWalkView.setUIClient()
        // 4. Call XWalkView.setResourceClient()
        setContentView(R.layout.activity_crosswalk);
        mXWalkView = findViewById(R.id.xwalkview);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mXWalkView.restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mXWalkView.saveState(outState);
    }

    @Override
    public void onXWalkReady() {
        // Do anyting with the embedding API
        //mXWalkView.load("https://web.whatsapp.com/", null);
        mXWalkView.setDrawingCacheEnabled(true);
        XWalkSettings settings = mXWalkView.getSettings();
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(XWalkSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(userAgent);

        mXWalkView.setUserAgentString(userAgent);
        if(bundle != null && !bundle.isEmpty()) {
            mXWalkView.restoreState(bundle);
        } else {
            mXWalkView.loadUrl(WHATSAPP_WEB_URL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(this, "permissionrequest", Toast.LENGTH_LONG).show();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onBackPressed() {
        //close drawer if open and impl. press back again to leave
        // DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // if (drawer.isDrawerOpen(GravityCompat.START)) {
        //     drawer.closeDrawer(GravityCompat.START);
        // } else if (System.currentTimeMillis() - lastBackClick < 1100) {
        finishAffinity();
        super.onBackPressed();
        // } else {
        //     showToast("Click back again to close");
        //     lastBackClick = System.currentTimeMillis();
        // }
    }

}
