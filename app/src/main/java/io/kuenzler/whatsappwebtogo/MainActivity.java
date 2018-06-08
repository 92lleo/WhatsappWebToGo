package io.kuenzler.whatsappwebtogo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String androidCurrent = "Linux; U; Android " + Build.VERSION.RELEASE;
    private static final String chrome = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    //private static final String test = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36";

    /*
    private static final String osxYosemity = "Macintosh; Intel Mac OS X 10_10_1";
    private static final String windows10 = "Windows 10";
    private static final String windows95 = "Windows 95";
    private static final String windows30 = "Windows 3.0";
    private static final String android8 = "Linux; U; Android 8.0.0; ko-kr; LG-L160L Build/IML74K";
    private static final String toGo = "Linux; U; Android WhatsappWebToGo";
    private static final String safari = "AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.1.3 Safari/7046A194A";
    private static final String firefox = "Gecko/20100101 Firefox/40.1";
    private static final String edge = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063";
    */

    private static final String browser = chrome;
    private static final String device = androidCurrent;
    private static final String userAgent = "Mozilla/5.0 (" + device + ") " + browser;

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA; // "android.permission.CAMERA";
    private static final String AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO; // "android.permission.RECORD_AUDIO";
    private static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE; //android.permission.WRITE_EXTERNAL_STORAGE
    private static final String[] VIDEO_PERMISSION = {CAMERA_PERMISSION, AUDIO_PERMISSION};

    private static final String WHATSAPP_WEB_URL = "https://web.whatsapp.com/\uD83C\uDF10/"+ Locale.getDefault().getLanguage();

    private static final int FILECHOOSER_RESULTCODE = 200;
    private static final int CAMERA_PERMISSION_RESULTCODE = 201;
    private static final int AUDIO_PERMISSION_RESULTCODE = 202;
    private static final int VIDEO_PERMISSION_RESULTCODE = 203;
    private static final int STORAGE_PERMISSION_RESULTCODE = 204;

    private static final String DEBUG_TAG = "WAWEBTOGO";

    private WebView webView;
    private ViewGroup mainView;

    private long lastTouchClick = 0;
    private long lastBackClick = 0;
    private float lastXClick = 0;
    private float lastYClick = 0;

    boolean keyboardEnabled;
    Toast clickReminder = null;

    private SharedPreferences prefs;

    private final Activity activity = this;

    private ValueCallback<Uri[]> mUploadMessage;
    private PermissionRequest currentPermissionRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mainView = findViewById(R.id.layout);
        webView = findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true); //for wa web

        webView.getSettings().setAllowContentAccess(true); // for camera
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.getSettings().setMediaPlaybackRequiresUserGesture(false); //for audio messages

        webView.getSettings().setDomStorageEnabled(true); //for html5 app

        webView.getSettings().setAppCacheEnabled(true); // app cache
        webView.getSettings().setAppCachePath(getCacheDir().getAbsolutePath()); //app cache
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); //app cache

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (request.getResources()[0].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED
                            && ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION, AUDIO_PERMISSION}, VIDEO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else {
                        request.grant(request.getResources());
                    }
                } else if (request.getResources()[0].equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    }
                } else {
                    try {
                        request.grant(request.getResources());
                    } catch (RuntimeException e) {
                        Log.d(DEBUG_TAG, "Granting permissions failed", e);
                    }
                }
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(DEBUG_TAG, "WebView console message: " + cm.message());
                return super.onConsoleMessage(cm);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                Intent chooserIntent = fileChooserParams.createIntent();
                MainActivity.this.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                view.scrollTo(0, 0);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //whatsapp sound:
                // url.equals("https://web.whatsapp.com/assets/0a598282e94e87dea63e466d115e4a83.mp3"

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Log.d(DEBUG_TAG, request.getUrl().toString());
                    if (request.getUrl().toString().contains("web.whatsapp.com")) {
                        return false;
                    } else if(request.getUrl().toString().contains("www.whatsapp.com")){
                        loadWhatsapp();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                        startActivity(intent);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(DEBUG_TAG, url);
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (url.contains("web.whatsapp.com")) {
                        return false;
                    } else {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                String msg = String.format("Error: %s - %s", error.getErrorCode(), error.getDescription());
                Log.d(DEBUG_TAG, msg);
            }

            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                Log.d(DEBUG_TAG, "Unhandled key event: " + event.toString());
            }
        });

        webView.setDownloadListener((String url, String userAgent, String contentDisposition, String mimetype, long contentLength) -> {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            //for downloading directly through download manager

            showToast("Downloading is not supported yet.");
        });

        webView.getSettings().setUserAgentString(userAgent);
        if (savedInstanceState == null) {
            //webView.loadUrl(WHATSAPP_WEB_URL);
            loadWhatsapp();
        } else {
            Log.d(DEBUG_TAG, "savedInstanceState is present");
        }

        webView.setOnTouchListener((View v, MotionEvent event) -> {
            if(clickReminder!= null){
                clickReminder.cancel();
                clickReminder = null;
            }
            lastTouchClick = System.currentTimeMillis();
            lastXClick = event.getX();
            lastYClick = event.getY();
            //return false;

            if (mainView.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    && event.getAction() == MotionEvent.ACTION_DOWN
                    && Math.abs(event.getY() - webView.getHeight()) < 160) {
                if (System.currentTimeMillis() - lastTouchClick < 1300) {
                    if (Math.abs(lastXClick - event.getX()) < 180) {
                        showSnackbar("Use the keyboard button on top to type");
                        lastTouchClick = 0;
                    } else {
                        lastTouchClick = System.currentTimeMillis();
                        lastXClick = event.getX();
                        lastYClick = event.getY();
                    }
                } else {
                    lastTouchClick = System.currentTimeMillis();
                    lastXClick = event.getX();
                    lastYClick = event.getY();
                }
            }
            return false;
        });
    }

    private void loadWhatsapp(){
        webView.loadUrl(WHATSAPP_WEB_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        keyboardEnabled = prefs.getBoolean("keyboardEnabled",false);
        setNavbarEnabled(prefs.getBoolean("navbarEnabled",true));
        if(!keyboardEnabled){
            setKeyboardEnabled(false);
        }
        showIntroInfo();
        showVersionInfo();
        //showSnackbar("Unblock keyboard with the keyboard button on top");
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_keyboard:
                toggleKeyboard();
                break;
            case R.id.scroll_left:
                showToast("scroll left");
                runOnUiThread(() -> webView.scrollTo(0, 0));
                break;
            case R.id.scroll_right:
                showToast("scroll right");
                runOnUiThread(() -> webView.scrollTo(2000, 0));
                break;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case VIDEO_PERMISSION_RESULTCODE:
                if (permissions.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        currentPermissionRequest.grant(currentPermissionRequest.getResources());
                    } catch (RuntimeException e) {
                        Log.e(DEBUG_TAG, "Granting permissions failed", e);
                    }
                } else {
                    showSnackbar("Permission not granted, can't use video.");
                    currentPermissionRequest.deny();
                }
                break;
            case CAMERA_PERMISSION_RESULTCODE:
            case AUDIO_PERMISSION_RESULTCODE:
                //same same
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        currentPermissionRequest.grant(currentPermissionRequest.getResources());
                    } catch (RuntimeException e) {
                        Log.e(DEBUG_TAG, "Granting permissions failed", e);
                    }
                } else {
                    showSnackbar("Permission not granted, can't use " + (requestCode == CAMERA_PERMISSION_RESULTCODE ? "camera" : "microphone"));
                    currentPermissionRequest.deny();
                }
                break;
            case STORAGE_PERMISSION_RESULTCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: check for current download and enqueue it
                } else {
                    showSnackbar("Permission not granted, can't download to storage");
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got permission result with unknown request code " + requestCode + " - " + Arrays.asList(permissions).toString());
        }
        currentPermissionRequest = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILECHOOSER_RESULTCODE:
                if (resultCode == RESULT_CANCELED || data.getData() == null) {
                    mUploadMessage.onReceiveValue(null);
                } else {
                    Uri result = data.getData();
                    Uri[] results = new Uri[1];
                    results[0] = result;
                    mUploadMessage.onReceiveValue(results);
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got activity result with unknown request code " + requestCode + " - " + data.toString());
        }
    }

    private void showAbout() {
        showPopupDialog(R.string.about);
    }

    private void showVersionInfo() {
        int lastShownVersionCode = 0;
        int currentVersionCode = 0;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(DEBUG_TAG, "Error checking versioncode", e);
            return;
        }
        lastShownVersionCode = prefs.getInt("lastShownVersionCode", 0);
        if (lastShownVersionCode == 0) {
            prefs.edit().putInt("lastShownVersionCode", currentVersionCode).apply();
            return;
        }
        if (lastShownVersionCode < currentVersionCode) {
            showPopupDialog(R.string.versionInfo);
        } else {
            return;
        }
        prefs.edit().putInt("lastShownVersionCode", currentVersionCode).apply();
    }

    private void showIntroInfo() {
        if (!prefs.getBoolean("introShown", false)) {
            showPopupDialog(R.string.introInfo);
        } else {
            return;
        }
        prefs.edit().putBoolean("introShown", true).apply();
    }

    private void showPopupDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showPopupDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(id)
                .setCancelable(false)
                .setPositiveButton("Ok", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showToast(String msg) {
        this.runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    private void showSnackbar(String msg) {
        this.runOnUiThread(() -> {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, 900);
            snackbar.setAction("dismiss", (View view) -> snackbar.dismiss());
            snackbar.setActionTextColor(Color.parseColor("#075E54"));
            snackbar.show();
        });
    }

    private void toggleKeyboard() {
        setKeyboardEnabled(!keyboardEnabled);
    }

    private void setKeyboardEnabled(boolean enable){
        keyboardEnabled=enable;
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (enable && mainView.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
            mainView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            showSnackbar("Keyboard is unblocked.");
            //inputMethodManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
        } else if (!enable) {
            mainView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            webView.getRootView().requestFocus();
            showSnackbar("Keyboard is blocked.");
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
        prefs.edit().putBoolean("keyboardEnabled", enable).apply();
    }

    private void toggleNavbarEnabled() {
        ActionBar navbar = getSupportActionBar();
        if (navbar != null) {
            setNavbarEnabled(!getSupportActionBar().isShowing());
        }
    }

    private boolean isNavbarEnabled() {
        ActionBar navbar = getSupportActionBar();
        if (navbar != null) {
            return navbar.isShowing();
        }
        return true;
    }

    private void setNavbarEnabled(boolean enable) {
        ActionBar navbar = getSupportActionBar();
        if (navbar != null) {
            if (enable) {
                navbar.show();
            } else {
                navbar.hide();
            }
            prefs.edit().putBoolean("navbarEnabled", enable).apply();
        }
    }

    @Override
    public void onBackPressed() {
        //close drawer if open and impl. press back again to leave
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (System.currentTimeMillis() - lastBackClick < 1100) {
            finishAffinity();
        } else {
            showToast("Click back again to close");
            lastBackClick = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_hide) {
            toggleNavbarEnabled();
        } else if (id == R.id.nav_logout) {
            showSnackbar("logging out...");
            webView.loadUrl("javascript:localStorage.clear()");
            WebStorage.getInstance().deleteAllData();
            loadWhatsapp();
        } else if (id == R.id.nav_new) {
            //showToast("nav_new");
        } else if (id == R.id.nav_switch) {
            //showToast("nav_switch");
        } else if (id == R.id.nav_settings) {
            //showToast("nav_settings");
        } else if (id == R.id.nav_about) {
            showAbout();
        } else if (id == R.id.nav_reload) {
            showSnackbar("reloading...");
            //webView.loadUrl(WHATSAPP_WEB_URL);
            loadWhatsapp();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
