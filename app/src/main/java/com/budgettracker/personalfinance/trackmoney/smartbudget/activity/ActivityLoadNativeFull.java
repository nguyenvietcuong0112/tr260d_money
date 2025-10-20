package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.BaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.CountdownView;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemConfiguration;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityNativeFullBinding;

public class ActivityLoadNativeFull extends BaseActivity {
    ActivityNativeFullBinding binding;
    private SharePreferenceUtils sharePreferenceUtils;
    private CountDownTimer countDownTimer;
    private ValueAnimator animator;
    private boolean isAdClicked = false;

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityNativeFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadNativeFull();
    }

    private void loadNativeFull() {
        Admob.getInstance().loadNativeAds(this, getString(R.string.native_full_language), 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAdsFull.setVisibility(View.VISIBLE);
                startActivity(new Intent(ActivityLoadNativeFull.this, IntroActivity.class));
                finish();

            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(ActivityLoadNativeFull.this)
                        .inflate(R.layout.native_full_language, null);

                ImageView closeButton = adView.findViewById(R.id.close);
                closeButton.setVisibility(View.VISIBLE);
                @SuppressLint("CutPasteId") TextView tvCountdown = adView.findViewById(R.id.tvCountdown);
                @SuppressLint("CutPasteId") CountdownView progressStroke = adView.findViewById(R.id.progressStroke);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                FrameLayout frCountDown = adView.findViewById(R.id.frCountdown);


                View.OnClickListener adClickListener = v -> {
                    isAdClicked = true;
                    stopCountdown();
                };
                mediaView.setOnClickListener(adClickListener);


                closeButton.setOnClickListener(v -> {
                    mediaView.performClick();

                    sharePreferenceUtils = new SharePreferenceUtils(getApplicationContext());
                    int counterValue = sharePreferenceUtils.getCurrentValue();
                    if (counterValue == 0) {
                        startActivity(new Intent(ActivityLoadNativeFull.this, IntroActivity.class));
                    } else {
                        startActivity(new Intent(ActivityLoadNativeFull.this, IntroActivity.class));
                    }

                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeButton.setVisibility(View.VISIBLE);
                    }
                }, 2000);
                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }
    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (animator != null) {
            animator.end();
        }
    }
}
