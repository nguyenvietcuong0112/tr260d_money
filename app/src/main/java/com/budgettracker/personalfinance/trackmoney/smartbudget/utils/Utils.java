package com.budgettracker.personalfinance.trackmoney.smartbudget.utils;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.budgettracker.personalfinance.trackmoney.smartbudget.model.CurrencyUnitModel;
import com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial.WakeService;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static ArrayList<CurrencyUnitModel> getCurrencyUnit() {
        List<ExtendedCurrency> currencies = ExtendedCurrency.getAllCurrencies();
        ArrayList<CurrencyUnitModel> currencyUnitModels = new ArrayList<>();
        for (ExtendedCurrency currency : currencies) {
            String symbol = currency.getSymbol();
            String name = currency.getName();
            String code = currency.getCode();
            int flagResId = currency.getFlag();
            currencyUnitModels.add(new CurrencyUnitModel(symbol,name, code, false,flagResId));
        }
        return  currencyUnitModels;
    }
    public static void openNotificationSettings(Activity context, Class<?> nextActivityClass) {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                startCheckingPermissionNoti(context,nextActivityClass);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
                startCheckingPermissionNoti(context,nextActivityClass);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Intent fallbackIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            fallbackIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(fallbackIntent);
            startCheckingPermissionNoti(context,nextActivityClass);
        }
    }

    private static void startCheckingPermissionNoti(Activity mContext,Class<?> nextActivityClass) {
        Handler handler = new Handler();
        Runnable checkPermissionTask = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(mContext, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            Intent serviceIntent = new Intent(mContext, WakeService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mContext.startForegroundService(serviceIntent);
                            } else {
                                mContext.startService(serviceIntent);
                            }
                        } catch (Exception e) {
                            // Silently handle any exceptions
                        }
                        backToAppNoti(mContext,nextActivityClass);
                    } else {
                        handler.postDelayed(this, 300);
                    }
                }
            }
        };
        handler.postDelayed(checkPermissionTask, 300);
    }
    private static void backToAppNoti(Activity mContext,Class<?> nextActivityClass) {
        Intent intent = new Intent(mContext, nextActivityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent);
    }
    public static boolean checkPermissionNoty(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            return ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
