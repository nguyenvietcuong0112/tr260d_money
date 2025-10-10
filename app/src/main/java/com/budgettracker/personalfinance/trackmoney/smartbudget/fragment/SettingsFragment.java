package com.budgettracker.personalfinance.trackmoney.smartbudget.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.AppOpenManager;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.LanguageSettingActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.CurrencyUnitActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsFragment extends Fragment {
    private boolean isBtnProcessing = false;
    private static final int REQUEST_CURRENCY_SELECT = 100;
    String currentCurrency;

    TextView tvCurrency;

    LinearLayout btnShare, btnLanguage, btnRateUs, btnPrivacyPolicy, llCurrency;
    FrameLayout frAds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        setupClickListeners();


        return view;
    }

    private void initViews(View view) {
        btnShare = view.findViewById(R.id.btn_share);
        btnLanguage = view.findViewById(R.id.btn_language);
        btnPrivacyPolicy = view.findViewById(R.id.btn_privacy_policy);
        btnRateUs = view.findViewById(R.id.btn_rate_us);
        llCurrency = view.findViewById(R.id.llCurrency);
        frAds = view.findViewById(R.id.fr_ads);

        tvCurrency = view.findViewById(R.id.tv_currency);
        tvCurrency.setText(currentCurrency);

        updateCurrencyDisplay();
        loadAds();
    }

    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(requireContext())) {
            Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_settings), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    if (!isAdded() || getActivity() == null) return;

                    NativeAdView adView = (NativeAdView) LayoutInflater.from(getActivity())
                            .inflate(R.layout.ad_native_admob_banner_3, null);

                    frAds.removeAllViews();
                    frAds.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    frAds.setVisibility(View.GONE);
                }
            });

        } else {
            frAds.removeAllViews();
        }


    }
    private void setupClickListeners() {
        btnShare.setOnClickListener(v -> {
            if (isBtnProcessing) return;
            isBtnProcessing = true;

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = "có link app thì điền vào";
            String sub = "AI Money";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, "Share"));
            AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBtnProcessing = false;
                }
            }, 1000);
        });

        btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LanguageSettingActivity.class);
            startActivity(intent);
        });

        btnRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                this.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=")));
            }
        });


        btnPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://2nd-chance-farms.vercel.app/policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });


        llCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CurrencyUnitActivity.class);
            intent.putExtra(CurrencyUnitActivity.EXTRA_FROM_SETTINGS, true);
            startActivityForResult(intent, REQUEST_CURRENCY_SELECT);
        });


    }

    private void updateCurrencyDisplay() {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";
        tvCurrency.setText(currentCurrency);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CURRENCY_SELECT && resultCode == Activity.RESULT_OK) {
            updateCurrencyDisplay();
        }
    }

}