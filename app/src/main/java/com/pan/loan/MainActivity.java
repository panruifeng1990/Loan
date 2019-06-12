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
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_READ_PHONE_STATE = 1;
    private static final long DELAYMILLIS = 1500;
    private WebView webview;
    private ImageView iv_loading;
    private TextView tv_loading;
    private ProgressBar progressbar;
    private static final String LOAN_URL = "https://www.pesomarket.com";
    //    private static final String LOAN_URL = "http://172.17.1.168:8000";
    private String imei;
    private boolean isFirst = true;
    private String firstUrl = "";
    Handler handler = new Handler();
    private final String TAG = MainActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        imei = getDeviceId(this);
        iv_loading = (ImageView) findViewById(R.id.iv_loading);
        tv_loading = (TextView) findViewById(R.id.tv_loading);
        webview = (WebView) findViewById(R.id.webview);
        webview.setVisibility(View.VISIBLE);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        initWebSetting();
        loadData();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iv_loading.setVisibility(View.GONE);
            }
        }, DELAYMILLIS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                    imei = getDeviceId(this);
                } else {
                    imei = getUniquePsuedoID();
                    Toast.makeText(this, "权限已被用户拒绝", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }


    private void loadData() {
        webview.loadUrl(LOAN_URL);
//        webview.loadUrl("file:///android_asset/index.html");
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
                    tv_loading.setVisibility(View.GONE);
                } else {
                    tv_loading.setVisibility(View.VISIBLE);
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
                view.loadUrl("javascript:function launchBrowser(str){window.android" +
                        ".launchBrowser(str);}");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (isFirst) {
                    firstUrl = url;
                    isFirst = false;
                }

                if (!TextUtils.isEmpty(imei)) {
                    view.loadUrl("javascript:callId('" + imei + "')");

                }
                view.loadUrl("javascript:function launchBrowser(str){window.android" +
                        ".launchBrowser(str);}");
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack() && !webview.getOriginalUrl().equals(firstUrl)) {
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
        public void launchBrowser(final String str) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (str != null && str.startsWith("http")) {
                        Log.d(TAG, str);
                        if (str.contains("https://play.google.com/store/apps"))
                            rateNow(mContext, str);
                        else {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(str);
                            intent.setData(content_url);
                            mContext.startActivity(intent);
                        }
                    }
                }
            });


        }


    }

    final String GOOGLE_PLAY = "com.android.vending";//这里对应的是谷歌商店，跳转别的商店改成对应的即可

    public void rateNow(final Context context, String googleUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Log.d(TAG, "rateNow: "+googleUrl);
            intent.setData(Uri.parse(googleUrl.replace("https://play.google.com/store/apps", "market:/")));
            intent.setPackage(GOOGLE_PLAY);//这里对应的是谷歌商店，跳转别的商店改成对应的即可
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                Log.d(TAG, "rateNow google play: ");

                context.startActivity(intent);
            } else {//没有应用市场，通过浏览器跳转到Google Play
                Log.d(TAG, "rateNow 浏览器: ");
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(googleUrl));
                if (intent2.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent2);
                } else {
                    //没有Google Play 也没有浏览器
                }
            }
        } catch (ActivityNotFoundException activityNotFoundException1) {
            Log.e(MainActivity.class.getSimpleName(), "GoogleMarket Intent not found");
        }
    }

    public String getUniquePsuedoID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    @SuppressLint("HardwareIds")
    public String getDeviceId(Context context) {
        String deviceId = "";
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            //8.0动态权限
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            } else {
                if (tm.getDeviceId() != null) {
                    deviceId = tm.getDeviceId();
                } else {
                    deviceId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            }
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = getUniquePsuedoID();
        } else {
            if (!TextUtils.isEmpty(tm.getLine1Number()))
                deviceId = tm.getLine1Number() + "&&" + deviceId;
        }
        return deviceId;
    }

}
