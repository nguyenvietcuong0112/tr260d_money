package com.budgettracker.personalfinance.trackmoney.smartbudget.ads;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.AbsBaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityNativeFullBinding;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemConfiguration;

public class ActivityLoadNativeFullV2 extends AbsBaseActivity {
    ActivityNativeFullBinding binding;
    public static final String NATIVE_FUll_AD_ID = "native_full_ad_id";

    private static ActivityFullCallback callback;

    public static void open(Context context, String id, ActivityFullCallback cb) {
        callback = cb;
        Intent intent = new Intent(context, ActivityLoadNativeFullV2.class);
        intent.putExtra(NATIVE_FUll_AD_ID,id);
        context.startActivity(intent);
    }

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String adId;
        if (getIntent().hasExtra(NATIVE_FUll_AD_ID)) {
            adId = getIntent().getStringExtra(NATIVE_FUll_AD_ID);
        } else {
            adId = getString(Integer.parseInt(""));
        }

        loadNativeFull(adId);
    }

    private void loadNativeFull(String adId) {
        Admob.getInstance().loadNativeAds(this, adId, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAdsFull.setVisibility(View.GONE);
                if (callback != null) {
                    callback.onResultFromActivityFull();
                }
                finish();
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFullV2.this)
                        .inflate(R.layout.layout_native_full_new, null);
                ImageView closeButton = adView.findViewById(R.id.close);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                closeButton.setOnClickListener(v -> mediaView.performClick());
                new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        closeButton.setOnClickListener(v -> {
                            if (callback != null) {
                                callback.onResultFromActivityFull();
                            }
                            finish();
                        });
                    }
                }.start();
                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }
    int count = 0;

    @Override
    protected void onResume() {
        super.onResume();
        count++;
        if (count >= 2) {
            if (callback != null) {
                callback.onResultFromActivityFull();
            }
            finish();
        }
    }

}