package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemConfiguration;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemUtil;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.BaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityInterestBinding;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InterestActivity  extends BaseActivity {
    private ActivityInterestBinding binding;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    boolean isNativeLanguageSelectLoaded = false;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityInterestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeCheckboxes();
        setupListeners();


        loadAdsNative();
    }

    public void loadAdsNativeLanguageSelect() {
        NativeAdView adView;
        if (!Admob.getInstance().isLoadFullAds()) {
            adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.layout_native_language, null);
        } else {
            adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.layout_native_language_non_organic, null);
        }
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(InterestActivity.this, getString(R.string.native_language_select), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                isNativeLanguageSelectLoaded = true;
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                checkNextButtonStatus(true);
            }

            @Override
            public void onAdFailedToLoad() {
                binding.frAds.removeAllViews();
                checkNextButtonStatus(true);
            }
        });
    }


    private void loadAdsNative() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(InterestActivity.this, getString(R.string.native_language), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(InterestActivity.this);
                if (Admob.getInstance().isLoadFullAds()) {
                    adView = (NativeAdView) LayoutInflater.from(InterestActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(InterestActivity.this).inflate(R.layout.layout_native_language, null);
                }
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                checkNextButtonStatus(true);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds.removeAllViews();
                checkNextButtonStatus(true);
            }

        });
    }

    private void initializeCheckboxes() {
        checkBoxes.add(binding.cbTrackExpenses);
        checkBoxes.add(binding.cbMonitorSavings);
        checkBoxes.add(binding.cbAnalyzeSpending);
        checkBoxes.add(binding.cbOptimizeSpending);
        checkBoxes.add(binding.cbPlanInvestments);
    }

    private void setupListeners() {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isNativeLanguageSelectLoaded) {
                    loadAdsNativeLanguageSelect();
                }
                updateContinueButtonState();
            });
        }

        binding.btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(InterestActivity.this, IntroActivity.class);
            startActivity(intent);
        });
    }

    private void updateContinueButtonState() {
        boolean hasSelection = false;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                hasSelection = true;
                break;
            }
        }
        binding.btnContinue.setEnabled(hasSelection);
    }

    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.btnContinue.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.btnContinue.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}