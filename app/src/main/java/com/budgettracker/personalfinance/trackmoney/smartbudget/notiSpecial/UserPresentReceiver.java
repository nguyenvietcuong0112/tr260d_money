package com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class UserPresentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            UnlockNotifier.handleUserPresent(context.getApplicationContext());
            // Start ProWakeService for a short session to enable realtime observers right after unlock
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                try {
                    Intent serviceIntent = new Intent(context, WakeService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.getApplicationContext().startForegroundService(serviceIntent);
                    } else {
                        context.getApplicationContext().startService(serviceIntent);
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }
}
