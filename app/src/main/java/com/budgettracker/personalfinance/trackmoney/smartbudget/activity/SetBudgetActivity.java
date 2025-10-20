package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.BudgetManager;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;

import java.text.NumberFormat;
import java.util.Locale;

public class SetBudgetActivity extends AppCompatActivity {

    private EditText inputBudget;
    private TextView btnSave;
    private BudgetManager budgetManager;
    FrameLayout frAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        initializeViews();
        setupBudgetManager();
        setupListeners();
        loadAds();
    }

    private void initializeViews() {
        inputBudget = findViewById(R.id.input_budget);
        btnSave = findViewById(R.id.btn_save);
        frAds = findViewById(R.id.fr_ads);

    }

    private void setupBudgetManager() {
        budgetManager = new BudgetManager(this);
    }

    private void setupListeners() {
        // Format number with comma separator
        inputBudget.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) {
                    return;
                }

                try {
                    isUpdating = true;

                    String str = s.toString();
                    if (str.equals(current)) {
                        isUpdating = false;
                        return;
                    }

                    String cleanString = str.replaceAll("[,]", "");

                    if (cleanString.isEmpty()) {
                        inputBudget.setText("");
                        isUpdating = false;
                        return;
                    }

                    long parsed = Long.parseLong(cleanString);
                    String formatted = NumberFormat.getNumberInstance(Locale.US).format(parsed);

                    current = formatted;
                    inputBudget.setText(formatted);
                    inputBudget.setSelection(formatted.length());

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } finally {
                    isUpdating = false;
                }
            }
        });

        btnSave.setOnClickListener(v -> {
            String budgetText = inputBudget.getText().toString().replaceAll(",", "");

            if (!budgetText.isEmpty()) {
                try {
                    double budgetAmount = Double.parseDouble(budgetText);

                    // Save to SharedPreferences
                    budgetManager.setTotalBudget(budgetAmount);

                    // Navigate to main activity or finish
                    finish();

                } catch (NumberFormatException e) {
                    inputBudget.setError("Invalid amount");
                }
            } else {
                inputBudget.setError("Please enter budget amount");
            }
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void loadAds() {
        if (Admob.getInstance().isLoadFullAds()) {
            Admob.getInstance().loadNativeAd(SetBudgetActivity.this, getString(R.string.native_budget_first), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);


                    NativeAdView adView = (NativeAdView) LayoutInflater.from(SetBudgetActivity.this)
                            .inflate(R.layout.layout_native_introthree_non_organic, null);

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
            frAds.setVisibility(View.GONE);
        }


    }
}