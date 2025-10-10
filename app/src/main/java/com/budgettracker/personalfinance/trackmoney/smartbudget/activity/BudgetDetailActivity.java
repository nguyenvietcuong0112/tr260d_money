package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.BudgetManager;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.CircularProgressViewDetail;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.BudgetAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.BudgetItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetDetailActivity extends AppCompatActivity {
    private CircularProgressViewDetail mainProgressView;
    private TextView tvTotalBudget, tvExpenses, tvRemaining;
    private RecyclerView rvBudgets;
    private BudgetManager budgetManager;
    private BudgetAdapter budgetAdapter;
    private LinearLayout btnAddBudget;
    private FrameLayout frAds;
    ImageView iv_back;
    String currentCurrency;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_detail);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        mainProgressView = findViewById(R.id.main_progress_view);
        tvTotalBudget = findViewById(R.id.tv_total_budget);
        tvExpenses = findViewById(R.id.tv_expenses);
        tvRemaining = findViewById(R.id.tv_remaining);
        rvBudgets = findViewById(R.id.rv_budgets);
        iv_back = findViewById(R.id.iv_back);
        frAds = findViewById(R.id.frAds);
        btnAddBudget = findViewById(R.id.btn_add_budget);
        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
        iv_back.setOnClickListener(v -> onBackPressed());


        budgetManager = new BudgetManager(this);
        setupRecyclerView();
        updateUI();
        loadAds();
    }

    private void loadAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_budget_detail), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView;
                if (SharePreferenceUtils.isOrganic(BudgetDetailActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(BudgetDetailActivity.this)
                            .inflate(R.layout.layout_native_language, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(BudgetDetailActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);
                }
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


    }


    private void setupRecyclerView() {
        List<BudgetItem> budgetItems = budgetManager.getBudgetItems();
        budgetAdapter = new BudgetAdapter(this, budgetItems, item -> {
//            showBudgetDetailDialog(item);

        }, budgetManager);
        rvBudgets.setLayoutManager(new GridLayoutManager(this, 2));
        rvBudgets.setAdapter(budgetAdapter);
    }

    private void updateUI() {
        double totalBudget = budgetManager.getTotalBudget();
        double totalExpenses = budgetManager.getTotalExpenses();
        double remaining = totalBudget - totalExpenses;
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);

        tvTotalBudget.setText("Budget: " + currentCurrency + formatter.format(totalBudget));
        tvExpenses.setText("Expenses: " + currentCurrency + formatter.format(totalExpenses));
        tvRemaining.setText("Remain: " + currentCurrency + formatter.format(remaining));

        int progress = totalBudget > 0 ? (int) ((remaining / totalBudget) * 100) : 0;
        mainProgressView.setProgress(progress);
        mainProgressView.setShowRemainingText(true);

        budgetAdapter.updateBudgets(budgetManager.getBudgetItems());

    }

    private void showAddBudgetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etName = dialogView.findViewById(R.id.et_budget_name);
        EditText etAmount = dialogView.findViewById(R.id.et_budget_amount);
        etAmount.addTextChangedListener(new TextWatcher() {
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
                        etAmount.setText("");
                        isUpdating = false;
                        return;
                    }

                    long parsed = Long.parseLong(cleanString);
                    String formatted = NumberFormat.getNumberInstance(Locale.US).format(parsed);

                    current = formatted;
                    etAmount.setText(formatted);
                    etAmount.setSelection(formatted.length());

                } catch (NumberFormatException e) {
                } finally {
                    isUpdating = false;
                }
            }
        });

        TextView btnAdd = dialogView.findViewById(R.id.btn_add_budget);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel_budget);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String amountStr = etAmount.getText().toString().replaceAll(",", "");
            ;

            if (name.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                double totalAllocatedBudget = 0;
                List<BudgetItem> existingItems = budgetManager.getBudgetItems();
                for (BudgetItem item : existingItems) {
                    totalAllocatedBudget += item.getTotalAmount();
                }

                double initialTotalBudget = budgetManager.getTotalBudget();

                if (totalAllocatedBudget + amount > initialTotalBudget) {
                    Toast.makeText(this,
                            "The total percentage of all jars must be smaller" +
                                    currentCurrency + NumberFormat.getInstance(Locale.US).format(initialTotalBudget - totalAllocatedBudget),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                BudgetItem newBudget = new BudgetItem(name, amount);

                budgetManager.saveBudgetItem(newBudget);
                budgetAdapter.updateBudgets(budgetManager.getBudgetItems());
                updateUI();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

//        builder.setPositiveButton("Add", (dialog, which) -> {
//            String name = etName.getText().toString();
//            String amountStr = etAmount.getText().toString();
//
//            if (name.isEmpty() || amountStr.isEmpty()) {
//                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            double amount = Double.parseDouble(amountStr);
//            BudgetItem newBudget = new BudgetItem(name, amount);
//
//            budgetManager.saveBudgetItem(newBudget);
//            budgetAdapter.updateBudgets(budgetManager.getBudgetItems());
//            updateUI();
//        });
//
//        builder.setNegativeButton("Cancel", null);
//        builder.show();
    }

    private void showBudgetDetailDialog(BudgetItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getName());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_budget_detail, null);
        builder.setView(dialogView);

        TextView tvBudgetAmount = dialogView.findViewById(R.id.tv_budget_amount);
        TextView tvExpenses = dialogView.findViewById(R.id.tv_expenses);
        TextView tvRemaining = dialogView.findViewById(R.id.tv_remaining);
        CircularProgressViewDetail progressView = dialogView.findViewById(R.id.progress_view);

        double expenses = budgetManager.getExpensesForBudget(item.getName());
        double remaining = item.getTotalAmount() - expenses;

        tvBudgetAmount.setText("Budget: " + String.format("$%,.0f", item.getTotalAmount()));
        tvExpenses.setText("Expenses: " + String.format("$%,.0f", expenses));
        tvRemaining.setText("Remain: " + String.format("$%,.0f", remaining));

        int progress = item.getTotalAmount() > 0 ? (int) ((remaining / item.getTotalAmount()) * 100) : 0;
        progressView.setProgress(progress);
        progressView.setShowRemainingText(true);

        builder.setPositiveButton("OK", null);
        builder.show();
    }


}
