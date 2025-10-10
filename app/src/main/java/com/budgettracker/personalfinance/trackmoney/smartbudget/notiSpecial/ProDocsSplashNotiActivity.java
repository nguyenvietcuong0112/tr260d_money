package com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.MainActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.AbsBaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivitySplashBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mallegan.ads.callback.AdCallback;
import com.mallegan.ads.util.AppOpenManager;

public class ProDocsSplashNotiActivity extends AbsBaseActivity {

    private ActivitySplashBinding binding;

    @Override
    public void bind() {
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadAndShowOpenSplash();

        FirebaseAnalytics.getInstance(this)
                .logEvent("event_click_noti_new", null);
    }

    private void loadAndShowOpenSplash() {
        AppOpenManager.getInstance().loadOpenAppAdSplash(
                this,
                getString(R.string.open_splash_noti),
                3000,
                60000,
                true,
                new AdCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        proceedToNextActivity();
                    }
                }
        );
    }

    private void proceedToNextActivity() {
        Intent intent = new Intent(ProDocsSplashNotiActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
