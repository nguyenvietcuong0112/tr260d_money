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
    private CountDownTimer countDownTimer;
    private boolean isCase2 = false;
    private int count = 0;

    public static void open(Context context, String id, ActivityFullCallback cb) {
        callback = cb;
        Intent intent = new Intent(context, ActivityLoadNativeFullV2.class);
        intent.putExtra(NATIVE_FUll_AD_ID, id);
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
            // Xử lý trường hợp không có ad ID - đóng activity
            if (callback != null) {
                callback.onResultFromActivityFull();
            }
            finish();
            return;
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

                // Random 50-50 cho 2 trường hợp
                isCase2 = Math.random() < 0.5; // true = trường hợp 2, false = trường hợp 1

                if (isCase2) {
                    // TRƯỜNG HỢP 2: Phải đợi 2 giây
                    setupCase2CloseButton(closeButton, mediaView);
                } else {
                    // TRƯỜNG HỢP 1: Đóng ngay
                    setupCase1CloseButton(closeButton);
                }

                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }

    // Trường hợp 1: Ẩn button, sau 2s hiện ra và đóng ngay
    private void setupCase1CloseButton(ImageView closeButton) {
        // Ẩn button ban đầu
        closeButton.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Đợi 2 giây
            }

            public void onFinish() {
                // Sau 2 giây: hiện button và cho phép đóng ngay
                closeButton.setVisibility(View.VISIBLE);
                closeButton.setOnClickListener(v -> {
                    if (callback != null) {
                        callback.onResultFromActivityFull();
                    }
                    finish();
                });
            }
        }.start();
    }

    // Trường hợp 2: Hiện button ngay, nhưng phải đợi 2s mới đóng được
    private void setupCase2CloseButton(ImageView closeButton, MediaView mediaView) {
        // Hiện button ngay từ đầu
        closeButton.setVisibility(View.VISIBLE);

        // Trong 2 giây đầu: click = click vào ad
        closeButton.setOnClickListener(v -> mediaView.performClick());

        countDownTimer = new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Đếm ngược 2 giây
            }

            public void onFinish() {
                // Sau 2 giây: cho phép đóng thật
                closeButton.setOnClickListener(v -> {
                    if (callback != null) {
                        callback.onResultFromActivityFull();
                    }
                    finish();
                });
            }
        }.start();
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel timer để tránh memory leak
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        // Clear callback để tránh memory leak
        callback = null;
    }
}