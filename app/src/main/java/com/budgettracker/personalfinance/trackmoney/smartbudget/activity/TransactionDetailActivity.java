package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {
    private TextView tvDate, tvAmount, tvCategory, tvNote, tvTransactionType, tvEdit;
    private LinearLayout btnDelete;
    private ImageButton btnBack;
    private TransactionModel transaction;
    private FrameLayout frAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        initializeViews();
        setupBackButton();
        loadTransactionData();
        setupDeleteButton();
        setupEditButton();

        loadAds();
    }

    private void setupEditButton() {
        tvEdit.setOnClickListener(v -> {

            if (indexToDelete == -1) {
                findTransactionIndex();
            }
            Intent intent = new Intent(TransactionDetailActivity.this, AddTransactionActivity.class);
            intent.putExtra("transaction", transaction);
            intent.putExtra("position", indexToDelete);
            startActivityForResult(intent, 1);


        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        tvDate = findViewById(R.id.tv_date);
        tvAmount = findViewById(R.id.tv_amount);
        tvCategory = findViewById(R.id.tv_category);
        tvNote = findViewById(R.id.tv_note);
        tvTransactionType = findViewById(R.id.tv_transaction_type);
        btnDelete = findViewById(R.id.btn_delete);
        frAds = findViewById(R.id.frAds);
        tvEdit = findViewById(R.id.tv_edit);

    }

    private void loadAds() {
        if (!SharePreferenceUtils.isOrganic(this)) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_detail_transaction), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView
                            adView = (NativeAdView) LayoutInflater.from(TransactionDetailActivity.this)
                            .inflate(R.layout.layout_native_language_non_organic, null);

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
            frAds.setVisibility(View.GONE);
            frAds.removeAllViews();
        }


    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadTransactionData() {
        Intent intent = getIntent();
        if (intent == null) {
            showError("Invalid transaction data");
            return;
        }

        transaction = (TransactionModel) intent.getSerializableExtra("transaction");

        if (transaction == null) {
            showError("Transaction details not found");
            return;
        }

        displayTransactionDetails();
    }

    private void displayTransactionDetails() {
        tvDate.setText(transaction.getDate());
        tvCategory.setText(transaction.getCategoryName());
        tvTransactionType.setText(transaction.getTransactionType());

        String note = transaction.getNote();
        tvNote.setText(note != null && !note.isEmpty() ? note : "");

        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        String formattedAmount = String.format("%s%s",
                currentCurrency,
                formatter.format(Double.parseDouble(transaction.getAmount()))
        );

        tvAmount.setText(formattedAmount);
        setAmountColor();
    }


    private void setAmountColor() {
        int colorResId;
        switch (transaction.getTransactionType()) {
            case "Income":
                colorResId = R.color.green;
                break;
            case "Expense":
                colorResId = R.color.red;
                break;
            default:
                colorResId = R.color.black;
        }
        tvAmount.setTextColor(getResources().getColor(colorResId));
    }

    private void setupDeleteButton() {
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Cancel", null)
                .show();
    }

    int indexToDelete = -1;

    private void deleteTransaction() {
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
        List<TransactionModel> transactions = preferenceUtils.getTransactionList();

        if (transactions == null || transactions.isEmpty()) {
            showError("Unable to delete transaction");
            return;
        }


        for (int i = 0; i < transactions.size(); i++) {
            TransactionModel t = transactions.get(i);
            if (t.getDate().equals(transaction.getDate()) &&
                    t.getAmount().equals(transaction.getAmount()) &&
                    t.getCategoryName().equals(transaction.getCategoryName()) &&
                    t.getTransactionType().equals(transaction.getTransactionType()) &&
                    t.getTime().equals(transaction.getTime())) {
                indexToDelete = i;
                break;
            }
        }

        if (indexToDelete == -1) {
            showError("Transaction not found");
            return;
        }
        transactions.remove(indexToDelete);
        preferenceUtils.saveTransactionList(transactions);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleted_position", indexToDelete);
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void findTransactionIndex() {
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
        List<TransactionModel> transactions = preferenceUtils.getTransactionList();

        if (transactions != null && !transactions.isEmpty()) {
            for (int i = 0; i < transactions.size(); i++) {
                TransactionModel t = transactions.get(i);
                if (t.getDate().equals(transaction.getDate()) &&
                        t.getAmount().equals(transaction.getAmount()) &&
                        t.getCategoryName().equals(transaction.getCategoryName()) &&
                        t.getTransactionType().equals(transaction.getTransactionType()) &&
                        t.getTime().equals(transaction.getTime())) {
                    indexToDelete = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("transactionData")) {
                String transactionJson = data.getStringExtra("transactionData");
                Gson gson = new Gson();
                TransactionModel updatedTransaction = gson.fromJson(transactionJson, TransactionModel.class);

                transaction = updatedTransaction;

                SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(this);
                List<TransactionModel> transactions = preferenceUtils.getTransactionList();
                if (transactions != null && indexToDelete != -1) {
                    transactions.set(indexToDelete, updatedTransaction);
                    preferenceUtils.saveTransactionList(transactions);
                }

                displayTransactionDetails();
            }
        }
    }


}