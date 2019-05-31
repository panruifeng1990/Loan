package com.pan.loan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_READ_PHONE_STATE = 1;
    private WebView webview;
    private ProgressBar progressbar;
    private static final String LOAN_URL = "http://34.92.201.8/#/";
    private String imei;
    private HashSet<String> urls;
    private WebView imeiWebview;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        urls = new HashSet<>();
        imei = getDeviceId(this);
        Log.d("tag", "imei===" + imei);
        webview = (WebView) findViewById(R.id.webview);
        webview.setVisibility(View.VISIBLE);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        //8.0动态权限

        initWebSetting();
        loadData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                    imei = getDeviceId(this);
                    Log.d("tag", "imei===" + imei);
                } else {
                    Toast.makeText(this, "权限已被用户拒绝", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        String deviceId = "";
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            } else {
                if (tm.getDeviceId() != null) {
                    deviceId = tm.getDeviceId();
                } else {
                    deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            }
            Log.d("tag", deviceId);
        }
        return deviceId;
    }


    private void loadData() {
//        webview.loadUrl(LOAN_URL);
        webview.loadUrl("https://www.pesomarket.com");
//        webview.loadUrl("http://10.1.3.93:8000/%E4%BA%94%E6%9C%88%E7%88%B1%E5%AE%B6%E8%8A%82.htm");
//        webview.loadUrl("file:///android_asset/index.html");
//        webview.loadUrl("http://10.1.3.93:8000/index.html");
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initWebSetting() {

        WebSettings webSettings = webview.getSettings();
        //设置允许js弹框
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(false);
        webSettings.setDomStorageEnabled(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置可以支持缩放
        webSettings.setSupportZoom(true);
        // 设置默认缩放方式尺寸是far
        // 设置出现缩放工具
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.addJavascriptInterface(new AndroidJs(this), "android");
        webview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressbar.setVisibility(View.INVISIBLE);
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    progressbar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

        });

        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webview.getSettings().setAppCachePath(appCachePath);
        webview.getSettings().setAllowFileAccess(true);

        webview.getSettings().setAppCacheEnabled(false);
        //注释掉 贴吧链接自动跳转问题 加载完成后再set 但微博会显示不出 加是否是http/https链接判断即可
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("tag", "start =" + url);
                //wakeUpLogin注入
                view.loadUrl("javascript:function launchBrowser(str){window.android" +
                        ".launchBrowser(str);}");
//                view.loadUrl("javascript:function launchBack(){window.android" +
//                        ".launchBack();}");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("tag", "finish =" + url);

//                Log.d("tag", "boolean =" + urls.add(url));
//                view.loadUrl("javascript:callId(" + imei + ")");
                //wakeUpLogin注入
                view.loadUrl("javascript:function launchBrowser(str){window.android" +
                        ".launchBrowser(str);}");
                //wakeUpLogin注入
//                view.loadUrl("javascript:function launchBack(){window.android" +
//                        ".launchBack();}");
                imeiWebview = view;


                if (urls.add(url)) {
                    Log.d("tag", "super urls ---=" + urls.toString());
                    super.onPageFinished(view, url);
                } else {
                    Log.d("tag", "urls ---=" + urls.toString());

                    view.goBack();
                    urls.remove(url);
                    Log.d("tag", "urls remove ---=" + urls.toString());
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.d("tag", "shouldOverrideUrlLoading =" + request.getUrl());
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("tag", "shouldOverrideUrlLoading =" + url);
                if (url != null && url.contains("http") && !url.contains("play.google.com")) {
                    view.loadUrl(url);   //在当前的webview中跳转到新的url
                }
                if (url != null && url.startsWith("tel:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

                // *** NEVER DO THIS!!! ***
                // super.onReceivedSslError(view, handler, error);

                // let's ignore ssl error
                handler.proceed();
            }
        });


        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public class AndroidJs {
        public Context mContext;

        public AndroidJs(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void launchBack() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @JavascriptInterface
        public void launchBrowser(final String str) {
            Log.d("tag", str);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (str != null && str.startsWith("http")) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(str);
                        intent.setData(content_url);
                        mContext.startActivity(intent);
                    }
                    webview.loadUrl("javascript:callId(" + imei + ")");
                    Log.d("tag", imei);
                }
            });


        }


    }
}
