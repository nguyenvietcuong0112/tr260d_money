package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial.BottomSheetPerNotiFragment;
import com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial.WakeService;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.Utils;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.LoadNativeFullNew;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.TransactionUpdateEvent;
import com.budgettracker.personalfinance.trackmoney.smartbudget.fragment.BudgetFragment;
import com.budgettracker.personalfinance.trackmoney.smartbudget.fragment.HomeFragment;
import com.budgettracker.personalfinance.trackmoney.smartbudget.fragment.SettingsFragment;
import com.budgettracker.personalfinance.trackmoney.smartbudget.fragment.StatisticsFragment;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.ak1.BubbleTabBar;

public class MainActivity extends AppCompatActivity {


    private static final int ADD_TRANSACTION_REQUEST = 1;
    private LinearLayout navHome, navStatistic, navBudget, navSettings, navAdd;
    private ImageView fabAdd;
    private ImageView ivHome, ivStatistic, ivBudget, ivSettings;
    private TextView tvHome, tvStatistic, tvBudget, tvSettings;

    private Fragment activeFragment;
    private SharePreferenceUtils sharePreferenceUtils;
    private List<TransactionModel> transactionList;
    private boolean isFirstClick = true;
    private long lastAdTime = 0;
    private static final long AD_COOLDOWN_PERIOD = 30000;
    private Handler handler = new Handler();

    private BubbleTabBar bubbleTabBar;

    private FrameLayout frAdsBanner;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    private static final String PREFS_NAME = "permission_flags";
    private static final String KEY_SHOWN_NOTI_SHEET = "shown_noti_sheet";

    ConstraintLayout itemNoti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        initializeViews();
        setupClickListeners();

        sharePreferenceUtils = new SharePreferenceUtils(this);
        sharePreferenceUtils.incrementCounter();
        transactionList = sharePreferenceUtils.getTransactionList();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateIcons(0);
        }


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                CustomBottomSheetDialogExitFragment dialog = CustomBottomSheetDialogExitFragment.newInstance();
                dialog.show(getSupportFragmentManager(), "ExitDialog");
            }
        });
        itemNoti.setOnClickListener(v -> {
            Utils.openNotificationSettings(this, MainActivity.class);
        });
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
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
        );
        int launchCount = SharePreferenceUtils.getAppLaunchCount(this);

        if (launchCount == 0) {
            requestNotificationPermission();
        } else {
            checkPermission();
        }
        SharePreferenceUtils.setAppLaunchCount(this, launchCount + 1);


//        loadInterAddTrans();

//        if (!SharePreferenceUtils.isOrganic(this)) {
//            TimerManager.getInstance().startTimer();
//        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void checkPermission() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!Utils.checkPermissionNoty(this)) {
            prefs.edit().putBoolean(KEY_SHOWN_NOTI_SHEET, true).apply();
            BottomSheetPerNotiFragment bottomSheet = BottomSheetPerNotiFragment.newInstance();
            bottomSheet.setOnSetClickListener(new BottomSheetPerNotiFragment.OnSetClickListener() {
                @Override
                public void onSetClick() {
                    Utils.openNotificationSettings(MainActivity.this, MainActivity.class);
                    bottomSheet.dismiss();
                }

                @Override
                public void onDontSetClick() {
                    bottomSheet.dismiss();
                }
            });
            bottomSheet.show(getSupportFragmentManager(), "BottomSheetDialogAlwayFragment");

        }
    }

    private void loadAdsBanner() {

        Admob.getInstance().loadNativeAd(this, getString(R.string.native_banner_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.ad_native_admob_banner_1, null);
                frAdsBanner.setVisibility(View.VISIBLE);
                frAdsBanner.removeAllViews();
                frAdsBanner.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                frAdsBanner.setVisibility(View.GONE);
            }
        });


    }
    private void initializeViews() {
        itemNoti = findViewById(R.id.item_Noti);
        navHome = findViewById(R.id.nav_home);
        navStatistic = findViewById(R.id.nav_statistic);
        navAdd = findViewById(R.id.nav_add);
        navBudget = findViewById(R.id.nav_budget);
        navSettings = findViewById(R.id.nav_settings);

        ivHome = findViewById(R.id.iv_home);
        ivStatistic = findViewById(R.id.iv_statistic);
        ivBudget = findViewById(R.id.iv_budget);
        ivSettings = findViewById(R.id.iv_settings);

        tvHome = findViewById(R.id.tv_home);
        tvStatistic = findViewById(R.id.tv_statistic);
        tvBudget = findViewById(R.id.tv_budget);
        tvSettings = findViewById(R.id.tv_settings);
        frAdsBanner = findViewById(R.id.fr_ads_banner);
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> {
            v.setEnabled(false);
            handleNavClick(() -> {
                loadFragment(new HomeFragment());
                updateIcons(0);
            }, v);
        });

        navStatistic.setOnClickListener(v -> {
            v.setEnabled(false);
            handleNavClick(() -> {
                loadFragment(new StatisticsFragment());
                updateIcons(1);
            }, v);
        });

        navAdd.setOnClickListener(v -> {
            v.setEnabled(false);
            if (!SharePreferenceUtils.isOrganic(this)) {
                Admob.getInstance().loadSplashInterAds2(MainActivity.this, getString(R.string.inter_navbar), 0, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startAddTransactionActivity();
                        v.setEnabled(true);
                    }

                    @Override
                    public void onAdClosedByUser() {
                        super.onAdClosedByUser();
                        startActivity(new Intent(MainActivity.this, LoadNativeFullNew.class)
                                .putExtra(LoadNativeFullNew.EXTRA_NATIVE_AD_ID, getString(R.string.native_full_navbar)));
                        v.setEnabled(true);
                    }
                });
            } else {
                startAddTransactionActivity();
                v.setEnabled(true);
            }
        });

        navBudget.setOnClickListener(v -> {
            v.setEnabled(false);
            handleNavClick(() -> {
                loadFragment(new BudgetFragment());
                updateIcons(2);
            }, v);
        });

        navSettings.setOnClickListener(v -> {
            v.setEnabled(false);
            handleNavClick(() -> {
                loadFragment(new SettingsFragment());
                updateIcons(3);
            }, v);
        });
    }


    private void loadFragment(Fragment fragment) {
        if (activeFragment != null && activeFragment.getClass() == fragment.getClass()) {
            return;
        }

        if (fragment instanceof HomeFragment || fragment instanceof StatisticsFragment || fragment instanceof BudgetFragment) {
            Bundle bundle = new Bundle();
            transactionList = sharePreferenceUtils.getTransactionList();
            bundle.putString("transactionList", new Gson().toJson(transactionList));
            fragment.setArguments(bundle);
        }

        activeFragment = fragment;
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading fragment", e);
        }
    }

    private void updateIcons(int selectedIndex) {
        ivHome.setColorFilter(getResources().getColor(R.color.icon_inactive));
        ivStatistic.setColorFilter(getResources().getColor(R.color.icon_inactive));
        ivBudget.setColorFilter(getResources().getColor(R.color.icon_inactive));
        ivSettings.setColorFilter(getResources().getColor(R.color.icon_inactive));

        tvHome.setTextColor(getResources().getColor(R.color.icon_inactive));
        tvStatistic.setTextColor(getResources().getColor(R.color.icon_inactive));
        tvBudget.setTextColor(getResources().getColor(R.color.icon_inactive));
        tvSettings.setTextColor(getResources().getColor(R.color.icon_inactive));

        switch (selectedIndex) {
            case 0:
                ivHome.setColorFilter(getResources().getColor(R.color.green_nav));
                tvHome.setTextColor(getResources().getColor(R.color.green_nav));
                break;
            case 1:
                ivStatistic.setColorFilter(getResources().getColor(R.color.green_nav));
                tvStatistic.setTextColor(getResources().getColor(R.color.green_nav));
                break;
            case 2:
                ivBudget.setColorFilter(getResources().getColor(R.color.green_nav));
                tvBudget.setTextColor(getResources().getColor(R.color.green_nav));
                break;
            case 3:
                ivSettings.setColorFilter(getResources().getColor(R.color.green_nav));
                tvSettings.setTextColor(getResources().getColor(R.color.green_nav));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("transactionData")) {
                String transactionJson = data.getStringExtra("transactionData");
                TransactionModel newTransaction = TransactionModel.fromJson(transactionJson);

                if (newTransaction != null) {
                    transactionList = sharePreferenceUtils.getTransactionList();

                    if (activeFragment instanceof HomeFragment) {
                        ((HomeFragment) activeFragment).onTransactionUpdated(new TransactionUpdateEvent(transactionList));
                    } else if (activeFragment instanceof StatisticsFragment) {
                        ((StatisticsFragment) activeFragment).onTransactionUpdated(new TransactionUpdateEvent(transactionList));
                    } else if (activeFragment instanceof BudgetFragment) {
                        ((BudgetFragment) activeFragment).onTransactionUpdated(new TransactionUpdateEvent(transactionList));
                    }

                    EventBus.getDefault().post(new TransactionUpdateEvent(transactionList));

                    Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void startAddTransactionActivity() {
        Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
        startActivityForResult(intent, ADD_TRANSACTION_REQUEST);
    }


    private void handleNavClick(Runnable action, View navButton) {
        long currentTime = System.currentTimeMillis();
        if(!SharePreferenceUtils.isOrganic(this)) {
            if (currentTime - lastAdTime > AD_COOLDOWN_PERIOD) {
                Admob.getInstance().loadSplashInterAds2(this, getString(R.string.inter_navbar), 0, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        lastAdTime = System.currentTimeMillis();
                        action.run();
                        navButton.setEnabled(true);
                    }

                    @Override
                    public void onAdClosedByUser() {
                        super.onAdClosedByUser();
                        lastAdTime = System.currentTimeMillis();
                        startActivity(new Intent(MainActivity.this, LoadNativeFullNew.class)
                                .putExtra(LoadNativeFullNew.EXTRA_NATIVE_AD_ID, getString(R.string.native_full_navbar)));
                        navButton.setEnabled(true);
                    }
                });
            } else {
                action.run();
                navButton.setEnabled(true);
            }
        } else {
            action.run();
            navButton.setEnabled(true);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.checkPermissionNoty(MainActivity.this)) {
            itemNoti.setVisibility(View.GONE);
        } else {
            itemNoti.setVisibility(View.VISIBLE);
        }
        navAdd.setEnabled(true);
        navHome.setEnabled(true);
        navBudget.setEnabled(true);
        navStatistic.setEnabled(true);
        navSettings.setEnabled(true);
//        if (!SharePreferenceUtils.isOrganic(this)) {
//            TimerManager.getInstance().startTimer();
//        }
        loadAdsBanner();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        TimerManager.getInstance().stopTimer();
//
//    }

//    private void loadInterAddTrans() {
//        if (!SharePreferenceUtils.isOrganic(MainActivity.this)) {
//            Admob.getInstance().loadInterAds(this, getString(R.string.inter_add_transaction), new InterCallback() {
//                @Override
//                public void onInterstitialLoad(InterstitialAd interstitialAd) {
//                    super.onInterstitialLoad(interstitialAd);
//                    Constant.interAddTransaction = interstitialAd;
//                }
//            });
//        }
//
//    }


}