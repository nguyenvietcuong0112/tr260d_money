package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.ads.ActivityLoadNativeFullV2;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemConfiguration;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemUtil;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.SlideAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.BaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityIntroBinding;


public class IntroActivity extends BaseActivity implements View.OnClickListener {
    private ImageView[] dots = null;
    private ActivityIntroBinding binding;

    // Flags để track trạng thái load ads
    private boolean isNative1Loaded = false;
    private boolean isNative2Loaded = false;
    private boolean isNative3Loaded = false;
    private boolean isNative4Loaded = false;

    // Flags để track ads đang loading (prevent duplicate)
    private boolean isLoadingNative1 = false;
    private boolean isLoadingNative2 = false;
    private boolean isLoadingNative3 = false;
    private boolean isLoadingNative4 = false;


    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_DARK);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (SystemUtil.isNetworkConnected(this)) {
            binding.frAds.setVisibility(View.VISIBLE);
        } else {
            binding.frAds.setVisibility(View.GONE);
        }

        dots = new ImageView[]{binding.cricle1, binding.cricle2, binding.cricle3, binding.cricle4};
        setUpSlideIntro();
        binding.btnNext.setOnClickListener(this);
        binding.btnBack.setOnClickListener(this);

        // ✨ Init UI cho trang đầu tiên TRƯỚC KHI load ads
        changeContentInit(0);

        // QUAN TRỌNG: Load native ads cho trang đầu tiên
        loadNative1();
    }


    @Override
    public void onClick(View v) {
        if (v == binding.btnNext) {
            if (binding.viewPager2.getCurrentItem() == 3) {
                goToHome();
                new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);

            } else if (binding.viewPager2.getCurrentItem() == 2) {
                handleInterstitialAndNavigate(R.string.inter_intro3);

            } else if (binding.viewPager2.getCurrentItem() == 1) {
                handleInterstitialAndNavigate(R.string.inter_intro2);

            } else if (binding.viewPager2.getCurrentItem() == 0) {
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
                if (Admob.getInstance().isLoadFullAds()) {
                    checkNextButtonStatus(false);
                    new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
                }
            } else {
                binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
            }
        } else if (v == binding.btnBack) {
            int currentItem = binding.viewPager2.getCurrentItem();
            if (currentItem > 0) {
                binding.viewPager2.setCurrentItem(currentItem - 1);
            }
        }
    }

    // Helper method để giảm code duplicate
    private void handleInterstitialAndNavigate(int interAdId) {
        if (Admob.getInstance().isLoadFullAds()) {
            Admob.getInstance().loadAndShowInter(IntroActivity.this, getString(interAdId), 0, 30000, new InterCallback() {
                @Override
                public void onAdFailedToLoad(LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    navigateToNext();
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    navigateToNext();
                }
            });
        } else {
            navigateToNext();
        }
    }

    private void navigateToNext() {
        binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem() + 1);
        if (Admob.getInstance().isLoadFullAds()) {
            checkNextButtonStatus(false);
            new Handler().postDelayed(() -> checkNextButtonStatus(true), 1500);
        }
    }

    public void goToHome() {
        String selectedCurrencyCode = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (Admob.getInstance().isLoadFullAds()) {
            Admob.getInstance().loadAndShowInter(IntroActivity.this, getString(R.string.inter_intro), 0, 30000, new InterCallback() {

                @Override
                public void onAdFailedToLoad(LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    showNativeFullAndNavigate(selectedCurrencyCode);
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    showNativeFullAndNavigate(selectedCurrencyCode);
                }
            });
        } else {
            navigateToNextScreen(selectedCurrencyCode);
        }
    }

    private void showNativeFullAndNavigate(String selectedCurrencyCode) {
        ActivityLoadNativeFullV2.open(IntroActivity.this, getString(R.string.native_full_intro), () -> {
            navigateToNextScreen(selectedCurrencyCode);
        });
    }

    private void navigateToNextScreen(String selectedCurrencyCode) {
        if (selectedCurrencyCode.isEmpty()) {
            startActivity(new Intent(IntroActivity.this, CurrencyUnitActivity.class));
        } else {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public static boolean isAccessibilitySettingsOn(Context r7) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(r7.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.i("TAG", e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(r7.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(r7.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        // Không gọi changeContentInit ở đây vì đã gọi trong bind()
    }

    private void setUpSlideIntro() {
        SlideAdapter adapter = new SlideAdapter(this);
        binding.viewPager2.setAdapter(adapter);

        binding.viewPager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeContentInit(position);

                // ✨ LAZY LOADING: Load native ads khi user scroll đến trang đó
                loadNativeAdForPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * ✨ LAZY LOADING LOGIC: Chỉ load ads khi user scroll đến trang
     */
    private void loadNativeAdForPage(int position) {
        switch (position) {
            case 0:
                // Trang 0 đã load trong bind(), không cần load lại
                break;

            case 1:
                if (!isNative2Loaded && !isLoadingNative2) {
                    loadNativeIntro2();
                }
                break;

            case 2:
                if (!isNative3Loaded && !isLoadingNative3) {
                    loadNative3();
                }
                break;

            case 3:
                if (!isNative4Loaded && !isLoadingNative4) {
                    loadNative4();
                }
                break;
        }
    }

    private void changeContentInit(int position) {
        // Update indicator dots
        for (int i = 0; i < 4; i++) {
            if (i == position)
                dots[i].setImageResource(R.drawable.bg_indicator_true);
            else
                dots[i].setImageResource(R.drawable.bg_indicator);
        }

        // ✨ Luôn ẩn tất cả ads containers trước
        binding.frAds1.setVisibility(View.GONE);
        binding.frAds2.setVisibility(View.GONE);
        binding.frAds3.setVisibility(View.GONE);
        binding.frAds4.setVisibility(View.GONE);

        // Show/hide native ads containers dựa vào trạng thái load
        if (position == 0) {
            updateAdsVisibility(binding.frAds1, isNative1Loaded);
            binding.btnBack.setAlpha(0.5f);

        } else if (position == 1) {
            updateAdsVisibility(binding.frAds2, isNative2Loaded);
            binding.btnBack.setAlpha(1f);

        } else if (position == 2) {
            updateAdsVisibility(binding.frAds3, isNative3Loaded);
            binding.btnBack.setAlpha(1f);

        } else if (position == 3) {
            updateAdsVisibility(binding.frAds4, isNative4Loaded);
            binding.btnBack.setAlpha(1f);
        }

        SystemUtil.setLocale(this);
        hideNavigationBar();
    }

    /**
     * Helper method để update visibility của ads container
     */
    private void updateAdsVisibility(View adsContainer, boolean isLoaded) {
        if (isLoaded) {
            binding.frAds.setVisibility(View.VISIBLE);
            adsContainer.setVisibility(View.VISIBLE);
        } else {
            binding.frAds.setVisibility(View.GONE);
            adsContainer.setVisibility(View.GONE);
        }
    }

    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Window window = getWindow();
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }


    private void loadNative1() {
        if (isLoadingNative1) return; // Prevent duplicate loading
        isLoadingNative1 = true;
        checkNextButtonStatus(false);

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding1), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                isLoadingNative1 = false;
                isNative1Loaded = false;

                // ✨ Update UI ngay khi load fail
                if (binding.viewPager2.getCurrentItem() == 0) {
                    binding.frAds1.setVisibility(View.GONE);
                    binding.frAds.setVisibility(View.GONE);
                }
                checkNextButtonStatus(true);
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                isLoadingNative1 = false;
                isNative1Loaded = true;

                int layoutRes = Admob.getInstance().isLoadFullAds()
                        ? R.layout.layout_native_language_non_organic
                        : R.layout.layout_native_language;

                NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                        .inflate(layoutRes, null);

                binding.frAds1.removeAllViews();
                binding.frAds1.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                // ✨ Update UI ngay khi load thành công
                if (binding.viewPager2.getCurrentItem() == 0) {
                    binding.frAds.setVisibility(View.VISIBLE);
                    binding.frAds1.setVisibility(View.VISIBLE);
                }
                checkNextButtonStatus(true);
            }
        });
    }


    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.btnNext.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }

    private void loadNative3() {
        if (isLoadingNative3) return; // Prevent duplicate loading
        isLoadingNative3 = true;
        checkNextButtonStatus(false);

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding3), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                isLoadingNative3 = false;
                isNative3Loaded = false;

                // ✨ Update UI ngay khi load fail
                if (binding.viewPager2.getCurrentItem() == 2) {
                    binding.frAds3.setVisibility(View.GONE);
                    binding.frAds.setVisibility(View.GONE);
                }
                checkNextButtonStatus(true);
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                isLoadingNative3 = false;
                isNative3Loaded = true;

                int layoutRes = Admob.getInstance().isLoadFullAds()
                        ? R.layout.layout_native_language_non_organic
                        : R.layout.layout_native_language;

                NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                        .inflate(layoutRes, null);

                binding.frAds3.removeAllViews();
                binding.frAds3.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                // ✨ Update UI ngay khi load thành công
                if (binding.viewPager2.getCurrentItem() == 2) {
                    binding.frAds.setVisibility(View.VISIBLE);
                    binding.frAds3.setVisibility(View.VISIBLE);
                }
                checkNextButtonStatus(true);
            }
        });
    }

    private void loadNativeIntro2() {
        if (!Admob.getInstance().isLoadFullAds()) {
            isNative2Loaded = false;
            binding.frAds2.removeAllViews();
            binding.frAds2.setVisibility(View.GONE);
            return;
        }

        if (isLoadingNative2) return; // Prevent duplicate loading
        isLoadingNative2 = true;
        checkNextButtonStatus(false);

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding2), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                runOnUiThread(() -> {
                    isLoadingNative2 = false;
                    isNative2Loaded = false;
                    binding.frAds2.removeAllViews();

                    // ✨ Update UI ngay khi load fail
                    if (binding.viewPager2.getCurrentItem() == 1) {
                        binding.frAds2.setVisibility(View.GONE);
                        binding.frAds.setVisibility(View.GONE);
                    }
                    checkNextButtonStatus(true);
                });
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                runOnUiThread(() -> {
                    isLoadingNative2 = false;
                    isNative2Loaded = true;

                    NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                            .inflate(R.layout.layout_native_introtwo_non_organic, null);

                    binding.frAds2.removeAllViews();
                    binding.frAds2.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                    // ✨ Update UI ngay khi load thành công
                    if (binding.viewPager2.getCurrentItem() == 1) {
                        binding.frAds.setVisibility(View.VISIBLE);
                        binding.frAds2.setVisibility(View.VISIBLE);
                    }

                    new Handler().postDelayed(() -> {
                        checkNextButtonStatus(true);
                    }, 500);
                });
            }
        });
    }

    private void loadNative4() {
        if (!Admob.getInstance().isLoadFullAds()) {
            isNative4Loaded = false;
            binding.frAds4.removeAllViews();
            binding.frAds4.setVisibility(View.GONE);
            return;
        }

        if (isLoadingNative4) return; // Prevent duplicate loading
        isLoadingNative4 = true;
        checkNextButtonStatus(false);

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_onboarding4), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                runOnUiThread(() -> {
                    isLoadingNative4 = false;
                    isNative4Loaded = false;
                    binding.frAds4.removeAllViews();

                    // ✨ Update UI ngay khi load fail
                    if (binding.viewPager2.getCurrentItem() == 3) {
                        binding.frAds4.setVisibility(View.GONE);
                        binding.frAds.setVisibility(View.GONE);
                    }
                    checkNextButtonStatus(true);
                });
            }

            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                runOnUiThread(() -> {
                    isLoadingNative4 = false;
                    isNative4Loaded = true;

                    NativeAdView adView = (NativeAdView) LayoutInflater.from(IntroActivity.this)
                            .inflate(R.layout.layout_native_introtwo_non_organic, null);

                    binding.frAds4.removeAllViews();
                    binding.frAds4.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);

                    // ✨ Update UI ngay khi load thành công
                    if (binding.viewPager2.getCurrentItem() == 3) {
                        binding.frAds.setVisibility(View.VISIBLE);
                        binding.frAds4.setVisibility(View.VISIBLE);
                    }

                    new Handler().postDelayed(() -> {
                        checkNextButtonStatus(true);
                    }, 500);
                });
            }
        });
    }


    @Override
    public void onBackPressed() {
        // Prevent back press
    }
}