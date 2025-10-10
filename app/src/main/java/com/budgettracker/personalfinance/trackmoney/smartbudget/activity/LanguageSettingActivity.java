package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.activity.OnBackPressedCallback;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.base.AbsBaseActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ActivityLanguageSettingBinding;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SystemUtil;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.language.ConstantLangage;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.language.UILanguageCustom;

import java.util.Locale;


public class LanguageSettingActivity extends AbsBaseActivity implements UILanguageCustom.OnItemClickListener {

    String langDevice = "en";
    String codeLang = "en";


    private ActivityLanguageSettingBinding binding;

    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        binding = ActivityLanguageSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpLayoutLanguage();
        Configuration config = new Configuration();
        Locale locale = Locale.getDefault();
        langDevice = locale.getLanguage();
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        Locale.setDefault(locale);
        config.locale = locale;
        SharedPreferences preferences = getBaseContext().getSharedPreferences("LANGUAGE", MODE_PRIVATE);
        binding.ivSelect.setOnClickListener(v -> {
            SystemUtil.saveLocale(getBaseContext(), codeLang);
            preferences.edit().putBoolean("language", true).apply();
           getOnBackPressedDispatcher().onBackPressed();
        });
        binding.ivBack.setOnClickListener(v -> finish());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(LanguageSettingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void setUpLayoutLanguage() {
        binding.uiLanguage.upDateData(ConstantLangage.getLanguage1(this), ConstantLangage.getLanguage2(this), ConstantLangage.getLanguage3(this), ConstantLangage.getLanguage4(this));
        binding.uiLanguage.setOnItemClickListener(this);
    }

    @Override
    public void onItemClickListener(int position, boolean itemseleted, String codeLang2) {
        if (!codeLang2.isEmpty()) {
            codeLang = codeLang2;
            SystemUtil.saveLocale(getBaseContext(), codeLang);
            updateLocale(codeLang);
        }
    }

    private void updateLocale(String langCode) {
        Locale newLocale = new Locale(langCode);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        binding.uiLanguage.upDateData(ConstantLangage.getLanguage1(this), ConstantLangage.getLanguage2(this), ConstantLangage.getLanguage3(this), ConstantLangage.getLanguage4(this));
    }

    @Override
    public void onPreviousPosition(int pos) {

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}