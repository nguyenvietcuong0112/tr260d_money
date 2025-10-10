package com.budgettracker.personalfinance.trackmoney.smartbudget.utils;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.LanguageHandModel;

import java.util.ArrayList;

public class Constant {
    public static InterstitialAd interIntro = null;
    public static InterstitialAd interNavBar = null;
    public static InterstitialAd interBudgetDetail = null;
    public static InterstitialAd interAddTransaction = null;
    public static InterstitialAd interSaveTransaction = null;
    public static InterstitialAd interEditTransaction = null;

    public static ArrayList<LanguageHandModel> getLanguage() {
        ArrayList<LanguageHandModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageHandModel("Hindi", "hi", false, R.drawable.flag_hi,false));
        listLanguage.add(new LanguageHandModel("Spanish", "es", false, R.drawable.flag_es,false));
        listLanguage.add(new LanguageHandModel("English", "en", false, R.drawable.flag_en,true));
        listLanguage.add(new LanguageHandModel("French", "fr", false, R.drawable.flag_fr,false));
        listLanguage.add(new LanguageHandModel("German", "de", false, R.drawable.flag_de,false));
        listLanguage.add(new LanguageHandModel("Italia", "it", false, R.drawable.flag_italia,false));
        listLanguage.add(new LanguageHandModel("Portuguese", "pt", false, R.drawable.flag_portugese,false));
        listLanguage.add(new LanguageHandModel("Korea", "ko", false, R.drawable.flag_korea,false));
        return listLanguage;
    }

}
