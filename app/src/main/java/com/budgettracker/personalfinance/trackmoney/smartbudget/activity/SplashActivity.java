package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.ads.ActivityFullCallback;
import com.budgettracker.personalfinance.trackmoney.smartbudget.ads.ActivityLoadNativeFullV2;
import com.budgettracker.personalfinance.trackmoney.smartbudget.ads.ActivityLoadNativeFullV5;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.BaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivitySplashBinding;
import com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial.WakeService;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharedClass;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemUtil;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.Utils;
import com.google.android.gms.ads.LoadAdError;
import com.mallegan.ads.callback.AdCallback;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.AppOpenManager;
import com.mallegan.ads.util.ConsentHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class SplashActivity extends BaseActivity {
    private InterCallback interCallback;
    SharedPreferences.Editor editor;
    SharedPreferences spref;

    private boolean isProgressDone = false;
    private boolean isAdDone = false;

    private SharePreferenceUtils sharePreferenceUtils;
    ActivitySplashBinding activitySplashBinding;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        activitySplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        getWindow().setFlags(1024, 1024);

        setContentView(activitySplashBinding.getRoot());
//        check();
        if (Utils.checkPermissionNoty(this)) {
            startWakeService();
        }

        new Thread(() -> {
            for (int progress = 0; progress <= 99; progress++) {
                final int currentProgress = progress;
                runOnUiThread(() -> {
                    activitySplashBinding.progressBar.setProgress(currentProgress);
                    activitySplashBinding.tvLoading.setText(currentProgress + "%");
                });
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();


        SharedPreferences sharedPreferences = getSharedPreferences("pref_ads", 0);
        this.spref = sharedPreferences;
        this.editor = sharedPreferences.edit();

        String fromShortcut = getIntent().getStringExtra("shortcut");
        if (("uninstall_fake".equals(fromShortcut))) {
            fakeProgress2();
        } else {
            loadAndShowOpenSplash();
        }
    }

    private void fakeProgress2() {
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                final int progress = i;
                runOnUiThread(() -> activitySplashBinding.tvLoading.setText(progress + "%"));

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // progress kết thúc
            isProgressDone = true;
            checkAndGoNext();
        }).start();

        // load ad song song
        loadAndShowInterSplashUninstall();
    }

    private void loadAndShowOpenSplash() {
        startFakeProgressLoading();
        Uri uri = getIntent().getData();
        interCallback = new InterCallback() {

            @Override
            public void onAdClosedByUser() {
                super.onAdClosedByUser();
                if (Admob.getInstance().isLoadFullAds()) {
                    ActivityLoadNativeFullV5.open(SplashActivity.this, getString(R.string.native_full_splash_high), getString(R.string.native_full_splash), () -> {
                        openApp();
                    });
                } else {
                    openApp();
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                if (Admob.getInstance().isLoadFullAds()) {
                    ActivityLoadNativeFullV5.open(SplashActivity.this, getString(R.string.native_full_splash_high), getString(R.string.native_full_splash), () -> {
                        openApp();
                    });
                } else {
                    openApp();
                }
            }
        };
        ConsentHelper consentHelper = ConsentHelper.getInstance(this);
        if (!consentHelper.canLoadAndShowAds()) {
            consentHelper.reset();
        }
        consentHelper.obtainConsentAndShow(this, () -> {
            Admob.getInstance().loadSplashInterAdsFloor(SplashActivity.this,
                    new ArrayList<>(Arrays.asList(getString(R.string.inter_splash_high), getString(R.string.inter_splash))),
                    3000, interCallback);
        });


    }

    private void openApp() {

        startActivity(new Intent(SplashActivity.this, LanguageActivity.class));
        finish();

    }

//    private void check() {
//        if (SharePreferenceUtils.isOrganic(this)) {
//            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {
//                @Override
//                public void onConversionDataSuccess(Map<String, Object> conversionData) {
//                    String mediaSource = (String) conversionData.get("media_source");
//                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || "organic".equals(mediaSource));
//                }
//
//                @Override
//                public void onConversionDataFail(String s) {
//                }
//
//                @Override
//                public void onAppOpenAttribution(Map<String, String> map) {
//                }
//
//                @Override
//                public void onAttributionFailure(String s) {
//                }
//            });
//        }
//    }

    private void startFakeProgressLoading() {
        new Thread(() -> {
            for (int i = 0; i <= 99; i++) {
                int progress = i;
                runOnUiThread(() -> activitySplashBinding.tvLoading.setText(progress + "%"));
                try {
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadAndShowInterSplashUninstall() {
        AppOpenManager.getInstance().loadOpenAppAdSplash(
                SplashActivity.this,
                getString(R.string.open_splash_uninstall),
                1000,
                60000,
                true,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        isAdDone = true;
                        Intent intent = new Intent(SplashActivity.this, UninstallActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }


    private void checkAndGoNext() {
        if (isProgressDone && isAdDone) {
            goToUninstallActivity();
        }
    }

    private void goToUninstallActivity() {
        if (!isFinishing() && !isDestroyed()) {
            startActivity(new Intent(SplashActivity.this, UninstallActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ExitApp();
    }

    public void ExitApp() {
        moveTaskToBack(true);
        finish();
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    private void startWakeService() {
        try {
            Intent serviceIntent = new Intent(this, WakeService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            // Silently handle any exceptions
        }

    }
}
