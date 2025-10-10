package com.budgettracker.personalfinance.trackmoney.smartbudget.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.BudgetManager;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.CircularProgressView;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.TransactionUpdateEvent;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.BudgetDetailActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.BudgetAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.BudgetItem;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment implements BudgetAdapter.BudgetItemListener {
    private BudgetManager budgetManager;
    private CircularProgressView mainProgressView;
    private TextView tvTotalBudget, tvExpenses;
    private LinearLayout btnBudgetDetail;
    private List<TransactionModel> allTransactionList;
    ImageView ivEditBalance;
    String currentCurrency;
    LinearLayout llBanner;
    private boolean isFirstLoad = true;


    FrameLayout frAdsHomeTop;

    FrameLayout frAdsCollap;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedLoadExpandTask;

    private Runnable loadTask = new Runnable() {
        @Override
        public void run() {
            loadNativeExpnad();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        initializeViews(view);
        setupBudgetManager();
        loadTransactionData();
        setupListeners();
        updateUI();
        return view;
    }

    private void initializeViews(View view) {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        mainProgressView = view.findViewById(R.id.main_progress_view);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        ivEditBalance = view.findViewById(R.id.iv_edit_balance);
        tvExpenses = view.findViewById(R.id.tv_expenses);
        btnBudgetDetail = view.findViewById(R.id.btn_budget_detail);
        llBanner = view.findViewById(R.id.ll_banner);
        frAdsHomeTop = view.findViewById(R.id.frAdsHomeTop);
        frAdsCollap = view.findViewById(R.id.frAdsCollap);
    }


    private void setupBudgetManager() {
        budgetManager = new BudgetManager(requireContext());
        if (budgetManager.getTotalBudget() == 0) {
            budgetManager.setTotalBudget(0);
        }
    }


    private void setupListeners() {
        View.OnClickListener editBudgetListener = v -> showEditBudgetDialog();
        ivEditBalance.setOnClickListener(editBudgetListener);

        btnBudgetDetail.setOnClickListener(v -> {

            Intent intent = new Intent(requireContext(), BudgetDetailActivity.class);
            startActivity(intent);

        });
    }

    private void loadTransactionData() {
        if (getArguments() == null || !getArguments().containsKey("transactionList")) {
            return;
        }

        String transactionListJson = getArguments().getString("transactionList");
        if (transactionListJson == null || transactionListJson.isEmpty()) {
            return;
        }

        Type type = new TypeToken<List<TransactionModel>>() {
        }.getType();
        allTransactionList = new Gson().fromJson(transactionListJson, type);

        if (allTransactionList == null) {
            allTransactionList = new ArrayList<>();
        }

        calculateAndUpdateTotalExpenses();
    }

    private void calculateAndUpdateTotalExpenses() {
        if (allTransactionList == null || allTransactionList.isEmpty()) {
            budgetManager.setTotalExpenses(0);
            updateUI();
            return;
        }

        double totalExpenseAmount = 0.0;
        for (TransactionModel transaction : allTransactionList) {
            if ("Expense".equals(transaction.getTransactionType())) {
                totalExpenseAmount += Double.parseDouble(transaction.getAmount());
            }
        }

        budgetManager.setTotalExpenses(totalExpenseAmount);

        updateUI();
    }

    private void showEditBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);

        EditText inputBudget = dialogView.findViewById(R.id.input_budget);
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
                } finally {
                    isUpdating = false;
                }
            }
        });
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnSave = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newBudget = inputBudget.getText().toString().replaceAll(",", "");
            ;
            if (!newBudget.isEmpty()) {
                double budgetAmount = Double.parseDouble(newBudget);
                budgetManager.setTotalBudget(budgetAmount);
                updateUI();
                dialog.dismiss();
            }
        });
    }

    private void updateUI() {
        double totalBudget = budgetManager.getTotalBudget();
        double totalExpenses = budgetManager.getTotalExpenses();
        double remaining = totalBudget - totalExpenses;

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBudget.setText(currentCurrency + " " + formatter.format(totalBudget));

        tvExpenses.setText("Expenses: " + currentCurrency + formatter.format(totalExpenses));

        int progress = totalBudget > 0 ? (int) ((remaining / totalBudget) * 100) : 0;

        progress = Math.max(0, Math.min(100, progress));

        // Cập nhật progress view
        mainProgressView.setProgress(progress);
        mainProgressView.setShowRemainingText(true);
    }

    @Override
    public void onBudgetItemClick(BudgetItem item) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
    }


    private void loadNativeCollap(@Nullable final Runnable onLoaded) {
        if (!isAdded() || getContext() == null) return;

        Log.d("Truowng", "loadNativeCollapA: ");
        if (frAdsHomeTop != null) {
            frAdsHomeTop.removeAllViews();
        }

        Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_collap_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (getContext() == null || !isAdded() || nativeAd == null) {
                    return;
                }

                NativeAdView adView = (NativeAdView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_native_home_collap, null);
                if (frAdsCollap != null) {
                    frAdsCollap.removeAllViews();
                    frAdsCollap.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                if (onLoaded != null) {
                    onLoaded.run();
                }
            }

            @Override
            public void onAdFailedToLoad() {
                if (!isAdded() || getContext() == null) return;

                if (frAdsCollap != null) {
                    frAdsCollap.removeAllViews();
                }

                if (onLoaded != null) {
                    onLoaded.run();
                }
            }
        });
    }


    private void loadNativeExpnad() {
        if (!isAdded()) return; // Tránh gọi khi Fragment chưa gắn vào Activity

        Log.d("Truong", "loadNativeCollapB: ");
        Context context = requireContext(); // an toàn sau khi isAdded()

        Admob.getInstance().loadNativeAd(context, getString(R.string.native_expand_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (!isAdded()) return; // Fragment có thể đã bị detach khi callback đến

                Context context = requireContext();
                NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.layout_native_home_expnad, null);

                frAdsHomeTop.removeAllViews();

                MediaView mediaView = adView.findViewById(R.id.ad_media);
                ImageView closeButton = adView.findViewById(R.id.close);
                closeButton.setOnClickListener(v -> {
                    mediaView.performClick();
                });

                Log.d("Truong", "onNativeAdLoaded: ");
                frAdsHomeTop.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                if (isAdded()) {
                    frAdsHomeTop.removeAllViews();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!SharePreferenceUtils.isOrganic(requireContext())) {
            if (isFirstLoad) {
                loadNativeCollap(() -> {
                    delayedLoadExpandTask = new Runnable() {
                        @Override
                        public void run() {
                            loadNativeExpnad();
                            isFirstLoad = false;
                        }
                    };
                    handler.postDelayed(delayedLoadExpandTask, 1000);
                });
            } else {
                loadNativeCollap(() -> {
                    delayedLoadExpandTask = new Runnable() {
                        @Override
                        public void run() {
                            loadNativeExpnad();
                        }
                    };
                    handler.postDelayed(delayedLoadExpandTask, 10000);
                });
            }
        } else {
            frAdsCollap.removeAllViews();
            frAdsHomeTop.removeAllViews();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(loadTask);
        if (delayedLoadExpandTask != null) {
            handler.removeCallbacks(delayedLoadExpandTask);
            delayedLoadExpandTask = null;
        }
    }
}