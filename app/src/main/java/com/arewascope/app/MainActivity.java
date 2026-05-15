package com.arewascope.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends Activity {

    public static final String SITE_URL = "https://arewascope.com.ng/";
    public static final String SITE_HOST = "arewascope.com.ng";
    public static final String NOTIFICATION_URL_EXTRA = "open_url";

    private static final String FCM_TOPIC = "news";
    private static final int FILE_CHOOSER_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2001;

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout offlineView;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBarsAndCutout();
        buildLayout();
        setupWebView();
        requestNotificationPermissionIfNeeded();
        setupFirebaseMessaging();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            loadUrl(getLaunchUrl(getIntent()));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = getLaunchUrl(intent);
        if (url != null && webView != null) {
            loadUrl(url);
        }
    }

    private void setupSystemBarsAndCutout() {
        Window window = getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.parseColor("#0B1220"));
            window.setNavigationBarColor(Color.parseColor("#0B1220"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            window.setAttributes(params);
        }
    }

    private void buildLayout() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        applySafeAreaInsets(root);

        webView = new WebView(this);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(webView, webParams);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(4)
        );
        progressParams.gravity = Gravity.TOP;
        root.addView(progressBar, progressParams);

        offlineView = new LinearLayout(this);
        offlineView.setOrientation(LinearLayout.VERTICAL);
        offlineView.setGravity(Gravity.CENTER);
        offlineView.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
        offlineView.setBackgroundColor(Color.WHITE);
        offlineView.setVisibility(View.GONE);

        TextView title = new TextView(this);
        title.setText("No internet connection");
        title.setTextSize(22);
        title.setTextColor(Color.parseColor("#0B1220"));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setPadding(0, 0, 0, dpToPx(10));
        offlineView.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Please check your connection and try again.");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.parseColor("#334155"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dpToPx(18));
        offlineView.addView(subtitle);

        Button retryButton = new Button(this);
        retryButton.setText("Retry");
        retryButton.setAllCaps(false);
        retryButton.setOnClickListener(v -> loadUrl(webView.getUrl() == null ? SITE_URL : webView.getUrl()));
        offlineView.addView(retryButton);

        FrameLayout.LayoutParams offlineParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(offlineView, offlineParams);

        setContentView(root);
    }

    private void applySafeAreaInsets(View root) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                int left;
                int top;
                int right;
                int bottom;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Insets systemInsets = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    left = systemInsets.left;
                    top = systemInsets.top;
                    right = systemInsets.right;
                    bottom = systemInsets.bottom;
                } else {
                    left = insets.getSystemWindowInsetLeft();
                    top = insets.getSystemWindowInsetTop();
                    right = insets.getSystemWindowInsetRight();
                    bottom = insets.getSystemWindowInsetBottom();
                }

                view.setPadding(left, top, right, bottom);
                return insets;
            });
            root.requestApplyInsets();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setTextZoom(100);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(isNetworkAvailable() ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setUserAgentString(settings.getUserAgentString() + " ArewaScopeAndroidApp/2.0");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        webView.setInitialScale(100);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request.isForMainFrame() && !isNetworkAvailable()) {
                    showOfflineView();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent;
                try {
                    intent = fileChooserParams.createIntent();
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                }

                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    MainActivity.this.filePathCallback = null;
                    Toast.makeText(MainActivity.this, "No file picker found", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
    }

    private boolean handleUrl(String url) {
        if (url == null || url.trim().isEmpty()) return true;

        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();

        if (scheme.equals("http") || scheme.equals("https")) {
            if (isSameSiteHost(host)) {
                return false;
            }
            openExternal(url);
            return true;
        }

        if (scheme.equals("tel") || scheme.equals("mailto") || scheme.equals("whatsapp") || scheme.equals("intent")) {
            openExternal(url);
            return true;
        }

        openExternal(url);
        return true;
    }

    private boolean isSameSiteHost(String host) {
        return host.equals(SITE_HOST) || host.equals("www." + SITE_HOST);
    }

    private void openExternal(String url) {
        try {
            Intent intent;
            if (url.startsWith("intent:")) {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            startActivity(intent);
        } catch (Exception ignored) {
            Toast.makeText(this, "Unable to open this link", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            url = SITE_URL;
        }

        if (isNetworkAvailable()) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            offlineView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.loadUrl(url);
            showOfflineView();
        }
    }

    private void showOfflineView() {
        webView.setVisibility(View.GONE);
        offlineView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private String getLaunchUrl(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra(NOTIFICATION_URL_EXTRA);
            if (url != null && isSafeWebsiteUrl(url)) {
                return url;
            }

            Uri data = intent.getData();
            if (data != null && isSafeWebsiteUrl(data.toString())) {
                return data.toString();
            }
        }
        return SITE_URL;
    }

    private boolean isSafeWebsiteUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            return (scheme.equals("http") || scheme.equals("https")) && isSameSiteHost(host);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setupFirebaseMessaging() {
        try {
            MyFirebaseMessagingService.createNotificationChannel(this);

            FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            android.util.Log.w("ArewaScopeFCM", "Topic subscription failed", task.getException());
                        }
                    });

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            android.util.Log.w("ArewaScopeFCM", "Fetching FCM token failed", task.getException());
                        } else {
                            android.util.Log.d("ArewaScopeFCM", "FCM token ready");
                        }
                    });
        } catch (Exception e) {
            android.util.Log.w("ArewaScopeFCM", "Firebase setup skipped. Add real google-services.json.", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
