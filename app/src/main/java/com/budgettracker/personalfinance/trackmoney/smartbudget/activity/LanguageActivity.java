package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

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
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.language.ConstantLangage;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.language.UILanguageCustom;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.BaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityLanguageBinding;


import java.util.Locale;
import java.util.Map;


public class LanguageActivity extends BaseActivity implements UILanguageCustom.OnItemClickListener {

    String codeLang = "";
    ActivityLanguageBinding binding;

    private boolean itemSelected = false;

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        SystemUtil.setLocale(this);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean fromSettings = getIntent().getBooleanExtra("from_settings", false);

        if (SharePreferenceUtils.isOrganic(this)) {
            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {

                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");
                    String campaignName = (String) conversionData.get("campaign");

                    boolean isOrganic = mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic");
                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), isOrganic);

                  /*  String uuid = UserUidUtils.generateUserUuid(getApplicationContext());

                    if (!isOrganic) {
                        DatabaseReference refUsers = FirebaseDatabase.getInstance()
                                .getReference()
                                .child("usersnew")
                                .child(uuid);
                        Map<String, Object> userHashmap = new HashMap<>(conversionData);
                        refUsers.updateChildren(userHashmap);
                    }*/
                }

                @Override
                public void onConversionDataFail(String s) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> map) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String s) {
                    // Handle attribution failure
                }
            });
        }

        if (fromSettings) {
//            binding.ivBack.setVisibility(View.VISIBLE);
            binding.frAds.setVisibility(View.GONE);

        }
        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        setUpLayoutLanguage();

        binding.ivSelect.setOnClickListener(v -> {
            if (itemSelected) {
                SystemUtil.saveLocale(this, codeLang);
                if (fromSettings) {
                    finish();
                } else {
                    if (!SharePreferenceUtils.isOrganic(LanguageActivity.this)) {
                        startActivity(new Intent(LanguageActivity.this, ActivityLoadNativeFull.class));
                        finish();
                    } else {
//                        sharePreferenceUtils = new SharePreferenceUtils(this);
//                        int counterValue = sharePreferenceUtils.getCurrentValue();
//                        if (counterValue == 0) {
                        startActivity(new Intent(LanguageActivity.this, IntroActivity.class));
//                        } else {
//                            startActivity(new Intent(LanguageActivity.this, MainActivity.class));
//                        }
                    }

//                    sharePreferenceUtils = new SharePreferenceUtils(this);
//                    int counterValue = sharePreferenceUtils.getCurrentValue();
//                    if (counterValue == 0) {
//                        startActivity(new Intent(LanguageActivity.this, GuideActivity.class));
//                    } else {
//                        startActivity(new Intent(LanguageActivity.this, IntroActivity.class));
//                    }
                }
            } else {
                Toast.makeText(this, "Please choose a language to continue", Toast.LENGTH_LONG).show();

            }
        });
        binding.ivSelect.setVisibility(View.GONE);
        loadAds();
    }


    private void setUpLayoutLanguage() {
        binding.uiLanguage.upDateData(ConstantLangage.getLanguage1(this), ConstantLangage.getLanguage2(this), ConstantLangage.getLanguage3(this), ConstantLangage.getLanguage4(this));
        binding.uiLanguage.setOnItemClickListener(this);
    }

    private void loadAds() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(LanguageActivity.this, getString(R.string.native_language), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(LanguageActivity.this);
                if (!SharePreferenceUtils.isOrganic(LanguageActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(LanguageActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(LanguageActivity.this).inflate(R.layout.layout_native_language, null);
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

    public void loadAdsNativeLanguageSelect() {
        NativeAdView adView;
        if (SharePreferenceUtils.isOrganic(this)) {
            adView = (NativeAdView) LayoutInflater.from(this).inflate(R.layout.layout_native_language, null);
        } else {
            adView = (NativeAdView) LayoutInflater.from(this).inflate(R.layout.layout_native_language_non_organic, null);
        }
        checkNextButtonStatus(false);

        Admob.getInstance().loadNativeAd(LanguageActivity.this, getString(R.string.native_language_select), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
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

    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.ivSelect.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.ivSelect.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClickListener(int position, boolean itemseleted, String codeLang2) {
        if (!codeLang2.isEmpty()) {
            codeLang = codeLang2;
            SystemUtil.saveLocale(getBaseContext(), codeLang);
            updateLocale(codeLang);
        }
        this.itemSelected = itemseleted;
        if (itemseleted) {
            binding.ivSelect.setAlpha(1.0f);
        }
        loadAdsNativeLanguageSelect();
    }

    private void updateLocale(String langCode) {
        Locale newLocale = new Locale(langCode);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        binding.tvPleaseLanguage.setText(R.string.please_select_language_to_continue);
        binding.uiLanguage.upDateData(ConstantLangage.getLanguage1(this), ConstantLangage.getLanguage2(this), ConstantLangage.getLanguage3(this), ConstantLangage.getLanguage4(this));
    }

    @Override
    public void onPreviousPosition(int pos) {

    }


}