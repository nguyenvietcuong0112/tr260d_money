package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.BaseActivityAds;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityUninstallBinding;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemConfiguration;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;

public class UninstallActivity extends BaseActivityAds {

    private ActivityUninstallBinding binding;

    @Override
    public void bind() {
        binding = ActivityUninstallBinding.inflate(getLayoutInflater());
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> {
            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();
        });
        binding.TryAgain.setOnClickListener(v -> {

            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();

        });
        binding.Explore.setOnClickListener(v -> {

            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();


        });
        binding.dont.setOnClickListener(v -> {

            startActivity(new Intent(UninstallActivity.this, MainActivity.class));
            finish();

        });
        binding.still.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                startActivity(new Intent(UninstallActivity.this, MainActivity.class));
                finish();
            }
        });
        loadAds();
    }

    private final String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_keep_user), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(UninstallActivity.this).inflate(R.layout.layout_native_home, null);
                    binding.frAds.setVisibility(View.VISIBLE);
                    binding.frAds.removeAllViews();
                    binding.frAds.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    binding.frAds.setVisibility(View.GONE);
                }
            });
        }
    }

    private final String[] permissions2 = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    private boolean checkSelfPermissionCameraAndStorage() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= 30) {
            permissions = this.permissions2;
        } else {
            permissions = this.permissions;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
