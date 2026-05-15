package com.arewascope.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private static final int SPLASH_DELAY_MS = 650;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBars();
        buildSplashLayout();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }

    private void setupBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#0B1220"));
            getWindow().setNavigationBarColor(Color.parseColor("#0B1220"));
        }
    }

    private void buildSplashLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24));
        root.setBackgroundColor(Color.parseColor("#0B1220"));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dpToPx(96), dpToPx(96));
        logoParams.setMargins(0, 0, 0, dpToPx(18));
        root.addView(logo, logoParams);

        TextView appName = new TextView(this);
        appName.setText("Arewa Scope");
        appName.setTextColor(Color.WHITE);
        appName.setTextSize(26);
        appName.setGravity(Gravity.CENTER);
        appName.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(appName, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView tagline = new TextView(this);
        tagline.setText("Northern Nigeria News & Updates");
        tagline.setTextColor(Color.parseColor("#D1FAE5"));
        tagline.setTextSize(14);
        tagline.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tagParams.setMargins(0, dpToPx(6), 0, dpToPx(22));
        root.addView(tagline, tagParams);

        ProgressBar loader = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        root.addView(loader, new LinearLayout.LayoutParams(dpToPx(34), dpToPx(34)));

        setContentView(root);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
