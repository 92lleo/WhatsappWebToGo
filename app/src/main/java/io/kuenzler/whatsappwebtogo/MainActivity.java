package io.kuenzler.whatsappwebtogo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //private static final String js = "function wrapFunc(name) {" +
    //        "if(typeof window[name] == 'function') {" +
    //        "var original = window['__' + name] = window[name];" +
    //       "window[name] = function() {  // and replace with wrapper" +
    //       "var result = original.apply(this, arguments);" +
    //       "Interceptor.reportCall(name, result.toString());" +
    //      "return result;" +
    //       "} alert('hallo'); } else {alert('hallo')}" +
    //      "}";
    private static final String js = "function wrapFunc(name) {alert('works!); if (typeof window[name] == 'function') {var original = window['__' + name] = window[name]; window[name] = function() { var result = original.apply(this, arguments); Interceptor.reportCall(name, result.toString()); return result; } alert('yes'); } else { alert('no'); }}";

    private static final String osxYosemity = "Macintosh; Intel Mac OS X 10_10_1";
    private static final String windows10 = "Windows 10";
    private static final String windows95 = "Windows 95";
    private static final String windows30 = "Windows 3.0";
    private static final String android8 = "Linux; U; Android 8.0.0; ko-kr; LG-L160L Build/IML74K";
    private static final String androidCurrent = "Linux; U; Android " + Build.VERSION.RELEASE;
    private static final String toGo = "Linux; U; Android WhatsappWebToGo";

    private static final String chrome = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    private static final String safari = "AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.1.3 Safari/7046A194A";
    private static final String firefox = "Gecko/20100101 Firefox/40.1";
    private static final String edge = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 Edge/15.15063";

    private static final String browser = chrome;
    private static final String device = androidCurrent;
    private static final String userAgent = "Mozilla/5.0 (" + device + ") " + browser;

    private static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    private static final String AUDIO_PERMISSION = "android.permission.RECORD_AUDIO";

    private static final String WHATSAPP_WEB_URL = "https://web.whatsapp.com";

    private static final int FILECHOOSER_RESULTCODE = 200;
    private static final int CAMERA_PERMISSION_RESULTCODE = 201;
    private static final int AUDIO_PERMISSION_RESULTCODE = 202;

    private static final String DEBUG_TAG = "WAWEBTOGO";

    private WebView webView;
    private NavigationView navigationView;
    private final Activity activity = this;

    private ValueCallback<Uri[]> mUploadMessage;
    private PermissionRequest currentPermissionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true); //for wa web

        webView.getSettings().setMediaPlaybackRequiresUserGesture(false); //for audio messages
        webView.getSettings().setDomStorageEnabled(true); //for html5 app
        webView.getSettings().setAppCacheEnabled(true); // app cache
        webView.getSettings().setAppCachePath(getCacheDir().getAbsolutePath()); //app cache
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); //app cache
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (request.getResources()[0].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    }
                } else if (request.getResources()[0].equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    }
                }
            }

            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(DEBUG_TAG, "WebView console message: " + cm.message());
                return false;
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

                // inject wrapper
                // don't forget to remove newline chars
                webView.loadUrl("javascript:" + js);

                // wrap all the functions needed
                //String[] funcToWrap = new String[]{"parseMsgNotification", "func2"};
                //for (String f : funcToWrap) {
                //    webView.loadUrl("javascript:wrapFunc('" + f + "');");
                //}

                webView.loadUrl("javascript:wrapFunc('parseMsgNotification')");
                webView.loadUrl("javascript:wrapFunc('alert')");
                webView.loadUrl("javascript:Interceptor.test('hallo')");
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                String msg = String.format("Error: %s - %s", error.getErrorCode(), error.getDescription());
                Log.d(DEBUG_TAG, msg);
                showToast(msg);
            }

            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                //do stuff
            }

            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //do stuff
                return true;
            }
        });

        // webView.addJavascriptInterface(new NotificationInterface(this), "Android");
        webView.addJavascriptInterface(new FunctionCallInterceptor(), "Interceptor");

        webView.getSettings().setUserAgentString(userAgent);
        webView.loadUrl(WHATSAPP_WEB_URL);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_RESULTCODE:
            case AUDIO_PERMISSION_RESULTCODE:
                //same same
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentPermissionRequest.grant(currentPermissionRequest.getResources());
                } else {
                    Snackbar.make(webView, "Permission not granted, can't use " + (requestCode == CAMERA_PERMISSION_RESULTCODE ? "camera" : "microphone"), Snackbar.LENGTH_LONG).show();
                    currentPermissionRequest.deny();
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got permission result with unknown request code " + requestCode + " - " + Arrays.asList(permissions).toString());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILECHOOSER_RESULTCODE:
                if (resultCode == RESULT_CANCELED || data.getData() == null) {
                    mUploadMessage.onReceiveValue(null);
                } else {
                    Uri test = data.getData();
                    Uri[] result = new Uri[1];
                    result[0] = test;
                    mUploadMessage.onReceiveValue(result);
                }
                break;
            default:
                Log.d(DEBUG_TAG, "Got activity result with unknown request code " + requestCode + " - " + data.toString());
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            try {
                getActionBar().hide();
            } catch (NullPointerException e) {
                getSupportActionBar().hide();
            }
        } else if (id == R.id.nav_gallery) {
            try {
                getActionBar().show();
            } catch (NullPointerException e) {
                getSupportActionBar().show();
            }
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class NotificationInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        NotificationInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
        }
    }

    class FunctionCallInterceptor {
        @JavascriptInterface
        public void reportCall(String functionName, String result) {
            showToast(functionName);
        }

        @JavascriptInterface
        public void test(String toShow) {
            showToast(toShow);
        }
    }
}
