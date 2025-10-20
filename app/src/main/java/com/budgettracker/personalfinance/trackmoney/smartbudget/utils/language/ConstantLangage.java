package com.budgettracker.personalfinance.trackmoney.smartbudget.utils.language;



import android.content.Context;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.LanguageModel;

import java.util.ArrayList;

public class ConstantLangage {

    public static ArrayList<LanguageModel> getLanguage1(Context context) {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageModel(context.getString(R.string.english_us), "en-US", false, R.drawable.flag_us));
        listLanguage.add(new LanguageModel(context.getString(R.string.english_uk), "en-GB", false, R.drawable.flag_en));
        listLanguage.add(new LanguageModel(context.getString(R.string.english_canada), "en-CA", false, R.drawable.flag_ca));
        listLanguage.add(new LanguageModel(context.getString(R.string.english_south_africa), "en-ZA", false, R.drawable.flag_sou));
        return listLanguage;
    }


    public static ArrayList<LanguageModel> getLanguage2(Context context) {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageModel(context.getString(R.string.hindi), "hi", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.bengali), "bn", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.marathi), "mr", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.telugu), "te", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.tamil), "ta", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.urdu), "ur", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.kannada), "kn", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.odia), "or", false, 0));
        listLanguage.add(new LanguageModel(context.getString(R.string.malayalam), "ml", false, 0));
        return listLanguage;
    }

    public static ArrayList<LanguageModel> getLanguage3(Context context) {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_brazil), "pt", false, R.drawable.flag_bra));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_europeu), "pt", false, R.drawable.flag_euro));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_angola), "pt", false, R.drawable.flag_angola));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_mozambique), "pt", false, R.drawable.flag_mozam));
        return listLanguage;
    }

    public static ArrayList<LanguageModel> getLanguage4(Context context) {
        ArrayList<LanguageModel> listLanguage = new ArrayList<>();


        listLanguage.add(new LanguageModel(context.getString(R.string.germany), "de", false, R.drawable.flag_de));
        listLanguage.add(new LanguageModel(context.getString(R.string.mexico), "es", false, R.drawable.flag_mexico));
        listLanguage.add(new LanguageModel(context.getString(R.string.korea), "ko", false, R.drawable.flag_korea));
        listLanguage.add(new LanguageModel(context.getString(R.string.russia), "ru", false, R.drawable.flag_russian));
        listLanguage.add(new LanguageModel(context.getString(R.string.poland), "pl", false, R.drawable.flag_poland));

        listLanguage.add(new LanguageModel(context.getString(R.string.english), "en", false, R.drawable.flag_en));

        listLanguage.add(new LanguageModel(context.getString(R.string.japan), "ja", false, R.drawable.flag_japanese));
        listLanguage.add(new LanguageModel(context.getString(R.string.turkey), "tr", false, R.drawable.flag_turkish));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_brazil), "pt", false, R.drawable.flag_bra));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_europeu), "pt", false, R.drawable.flag_euro));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_angola), "pt", false, R.drawable.flag_angola));
        listLanguage.add(new LanguageModel(context.getString(R.string.portuguese_mozambique), "pt", false, R.drawable.flag_mozam));
        listLanguage.add(new LanguageModel(context.getString(R.string.hindi), "hi", false, R.drawable.flag_hi));

        listLanguage.add(new LanguageModel(context.getString(R.string.indonesia), "in", false, R.drawable.flag_indonesia));
        listLanguage.add(new LanguageModel(context.getString(R.string.thailand), "th", false, R.drawable.flag_thailand));
        listLanguage.add(new LanguageModel(context.getString(R.string.philippines), "tl", false, R.drawable.flag_philippines));
        listLanguage.add(new LanguageModel(context.getString(R.string.nigeria), "en", false, R.drawable.flag_nigeria));


        listLanguage.add(new LanguageModel(context.getString(R.string.iraq), "ar", false, R.drawable.flag_iraq));
        listLanguage.add(new LanguageModel(context.getString(R.string.ukraine), "uk", false, R.drawable.flag_ukraine));
        listLanguage.add(new LanguageModel(context.getString(R.string.argentina), "es", false, R.drawable.flag_argentina));
        listLanguage.add(new LanguageModel(context.getString(R.string.kazakhstan), "kk", false, R.drawable.flag_kazakhstan));
        listLanguage.add(new LanguageModel(context.getString(R.string.arabic), "ar", false, R.drawable.flag_arabic));
        return listLanguage;
    }
}

