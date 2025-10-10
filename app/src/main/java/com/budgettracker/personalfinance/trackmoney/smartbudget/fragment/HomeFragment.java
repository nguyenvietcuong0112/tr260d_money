package com.budgettracker.personalfinance.trackmoney.smartbudget.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.TransactionUpdateEvent;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.LoanTransactionAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.TransactionAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvTotalBalance, tvSelectedMonth, tvTotalExpenditure, tvTotalLabel;
    private LinearLayout llExpend, llIncome, llLoan, headerTotal;
    private RecyclerView rvTransactions;
    private TransactionAdapter regularAdapter;
    private LoanTransactionAdapter loanAdapter;

    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private List<TransactionModel> filteredTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expense";
    private String currentMonth = "";
    private Map<String, List<TransactionModel>> transactionsByDate = new HashMap<>();
    String currentCurrency;
    ImageView ivEditBalance;

    String totalAmount;
    LinearLayout llBanner;
    //    FrameLayout frAds;
    private LinearLayout noDataView;

    boolean isBalanceVisible = true;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        requireActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        initViews(view);
        loadTransactionData();
        setupClickListeners();
        setupInitialFilter();

//        loadAds();

        return view;
    }

    private void initViews(View view) {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        headerTotal = view.findViewById(R.id.header_total);
        tvTotalLabel = view.findViewById(R.id.tv_total_label);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        tvTotalExpenditure = view.findViewById(R.id.tv_total_expenditure);
        llExpend = view.findViewById(R.id.ll_expend);
        llIncome = view.findViewById(R.id.ll_income);
        llLoan = view.findViewById(R.id.ll_loan);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        ivEditBalance = view.findViewById(R.id.iv_edit_balance);
        noDataView = view.findViewById(R.id.layout_no_data);
        frAdsHomeTop = view.findViewById(R.id.frAdsHomeTop);
        frAdsCollap = view.findViewById(R.id.frAdsCollap);
        llBanner = view.findViewById(R.id.ll_banner);
//        frAds = view.findViewById(R.id.frAds);

        regularAdapter = new TransactionAdapter(filteredTransactionList);
        loanAdapter = new LoanTransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(regularAdapter);

        tvTotalExpenditure.setText(currentCurrency);

    }



//    private void loadAds() {
//        if (!SharePreferenceUtils.isOrganic(getContext())) {
//            Admob.getInstance().loadNativeAd(getContext(), getString(R.string.native_home), new NativeCallback() {
//                @Override
//                public void onNativeAdLoaded(NativeAd nativeAd) {
//                    super.onNativeAdLoaded(nativeAd);
//                    NativeAdView adView = (NativeAdView) LayoutInflater.from(getContext()).inflate(R.layout.layout_native_home, null);
//                    frAds.removeAllViews();
//                    frAds.addView(adView);
//                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
//                }
//
//                @Override
//                public void onAdFailedToLoad() {
//                    super.onAdFailedToLoad();
//                    frAds.setVisibility(View.GONE);
//                }
//            });
//        } else  {
//            frAds.removeAllViews();
//        }
//
//
//    }

    private void checkEmptyState() {
        boolean hasTransactions = false;

        for (TransactionModel transaction : allTransactionList) {
            if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                continue;
            }

            try {
                Date transactionDate = parseTransactionDate(transaction.getDate());
                if (transactionDate != null) {
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                    String transactionMonth = outputDateFormat.format(transactionDate);

                    if (transaction.getTransactionType().equals(currentTransactionType) &&
                            transactionMonth.equals(currentMonth)) {
                        hasTransactions = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (hasTransactions) {
            rvTransactions.setVisibility(View.VISIBLE);
            noDataView.setVisibility(View.GONE);
        } else {
            rvTransactions.setVisibility(View.GONE);
            noDataView.setVisibility(View.VISIBLE);
        }
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
    }

    private void setupClickListeners() {
        llExpend.setOnClickListener(v -> {
            currentTransactionType = "Expense";
            updateTransactionTypeUI();
            filterTransactions();
        });

        llIncome.setOnClickListener(v -> {
            currentTransactionType = "Income";
            updateTransactionTypeUI();
            filterTransactions();
        });

        llLoan.setOnClickListener(v -> {
            currentTransactionType = "Loan";
            updateTransactionTypeUI();
            filterTransactions();
        });

        tvSelectedMonth.setOnClickListener(v -> {
            showMonthPickerDialog();
        });

        ivEditBalance.setOnClickListener(view -> {
            if (isBalanceVisible) {
                tvTotalBalance.setText("******");
                ivEditBalance.setImageResource(R.drawable.ic_visibility);
            } else {
                tvTotalBalance.setText(totalAmount);
                ivEditBalance.setImageResource(R.drawable.ic_visibility_off);
            }
            isBalanceVisible = !isBalanceVisible;
        });
    }

    private void setupInitialFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        currentMonth = dateFormat.format(calendar.getTime());
        tvSelectedMonth.setText(currentMonth);

        updateTransactionTypeUI();
        filterTransactions();
    }

    private void updateTransactionTypeUI() {
        // Get references to indicators
        View indicatorExpense = llExpend.findViewById(R.id.indicator_expense);
        View indicatorIncome = llIncome.findViewById(R.id.indicator_income);
        View indicatorLoan = llLoan.findViewById(R.id.indicator_loan);

        indicatorExpense.setVisibility(currentTransactionType.equals("Expense") ? View.VISIBLE : View.INVISIBLE);
        indicatorIncome.setVisibility(currentTransactionType.equals("Income") ? View.VISIBLE : View.INVISIBLE);
        indicatorLoan.setVisibility(currentTransactionType.equals("Loan") ? View.VISIBLE : View.INVISIBLE);

        ImageView ivExpend = llExpend.findViewById(R.id.iv_expend);
        TextView tvExpendLabel = llExpend.findViewById(R.id.tv_expend_label);
        ImageView ivIncome = llIncome.findViewById(R.id.iv_income);
        TextView tvIncomeLabel = llIncome.findViewById(R.id.tv_income_label);
        ImageView ivLoan = llLoan.findViewById(R.id.iv_loan);
        TextView tvLoanLabel = llLoan.findViewById(R.id.tv_loan_label);

        int colorActive = getResources().getColor(R.color.purple);
        int colorInactive = getResources().getColor(R.color.icon_inactive);

        ivExpend.setColorFilter(currentTransactionType.equals("Expense") ? colorActive : colorInactive);
        tvExpendLabel.setTextColor(currentTransactionType.equals("Expense") ? colorActive : colorInactive);

        ivIncome.setColorFilter(currentTransactionType.equals("Income") ? colorActive : colorInactive);
        tvIncomeLabel.setTextColor(currentTransactionType.equals("Income") ? colorActive : colorInactive);

        ivLoan.setColorFilter(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
    }

    private void filterTransactions() {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM, d yyyy", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        SimpleDateFormat alternateInputFormat = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());

        filteredTransactionList.clear();
        transactionsByDate.clear();

        double totalAmount = 0;

        if ("Loan".equals(currentTransactionType)) {
            rvTransactions.setAdapter(loanAdapter);
            headerTotal.setVisibility(View.GONE);

            List<TransactionModel> loanTransactions = new ArrayList<>();
            for (TransactionModel transaction : allTransactionList) {
                if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                    continue;
                }

                try {
                    Date transactionDate;
                    String dateStr = transaction.getDate();

                    try {
                        transactionDate = inputDateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        try {
                            transactionDate = alternateInputFormat.parse(dateStr);
                        } catch (ParseException e2) {
                            System.err.println("Could not parse date: " + dateStr);
                            continue;
                        }
                    }

                    String transactionMonth = outputDateFormat.format(transactionDate);

                    if (transaction.getTransactionType().equals(currentTransactionType) &&
                            transactionMonth.equals(currentMonth)) {
                        loanTransactions.add(transaction);

                        try {
                            double amount = Double.parseDouble(transaction.getAmount());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            loanAdapter.updateData(loanTransactions);
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvTotalExpenditure.setText(currentCurrency + formatter.format(totalAmount));

        } else {
            rvTransactions.setAdapter(regularAdapter);
            headerTotal.setVisibility(View.VISIBLE);

            if ("Expense".equals(currentTransactionType)) {
                tvTotalLabel.setText("Total expenditure");
            } else {
                tvTotalLabel.setText("Total income");

            }

            for (TransactionModel transaction : allTransactionList) {
                if (transaction.getDate() == null || transaction.getDate().trim().isEmpty()) {
                    continue;
                }

                try {
                    Date transactionDate;
                    String dateStr = transaction.getDate();

                    try {
                        transactionDate = inputDateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        try {
                            transactionDate = alternateInputFormat.parse(dateStr);
                        } catch (ParseException e2) {
                            System.err.println("Could not parse date: " + dateStr);
                            continue;
                        }
                    }

                    String transactionMonth = outputDateFormat.format(transactionDate);

                    if (transaction.getTransactionType().equals(currentTransactionType) &&
                            transactionMonth.equals(currentMonth)) {

                        filteredTransactionList.add(transaction);

                        String dayKey = new SimpleDateFormat("EEE, dd MMMM", Locale.US).format(transactionDate);
                        if (!transactionsByDate.containsKey(dayKey)) {
                            transactionsByDate.put(dayKey, new ArrayList<>());
                        }
                        transactionsByDate.get(dayKey).add(transaction);

                        try {
                            double amount = Double.parseDouble(transaction.getAmount());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            regularAdapter.updateData(filteredTransactionList, transactionsByDate);
            updateTotalAmount();
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvTotalExpenditure.setText(currentCurrency + formatter.format(totalAmount));
        }
        updateTotalAmount();
        checkEmptyState();

    }

    private void updateTotalAmount() {
        double totalBalance = 0;
        double totalExpenditure = 0;
        double totalIncome = 0;
        double totalLoan = 0;

        for (TransactionModel transaction : allTransactionList) {
            try {
                double amount = Double.parseDouble(transaction.getAmount());

                if (transaction.getTransactionType().equals("Income")) {
                    totalBalance += amount;
                } else if (transaction.getTransactionType().equals("Expense")) {
                    totalBalance -= amount;
                }
                if (transaction.getDate() != null && !transaction.getDate().trim().isEmpty()) {
                    try {
                        Date transactionDate = parseTransactionDate(transaction.getDate());
                        if (transactionDate != null) {
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                            String transactionMonth = outputDateFormat.format(transactionDate);

                            if (transactionMonth.equals(currentMonth)) {
                                switch (transaction.getTransactionType()) {
                                    case "Income":
                                        totalIncome += amount;
                                        break;
                                    case "Expense":
                                        totalExpenditure += amount;
                                        break;
                                    case "Loan":
                                        totalLoan += amount;
                                        break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBalance.setText(currentCurrency + " " + formatter.format(totalBalance));
        totalAmount = currentCurrency + formatter.format(totalBalance);
        switch (currentTransactionType) {
            case "Income":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalIncome));
                tvTotalExpenditure.setTextColor(Color.parseColor("#17B26A"));
                break;
            case "Expense":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalExpenditure));
                tvTotalExpenditure.setTextColor(Color.parseColor("#F04438"));
                break;
            case "Loan":
                tvTotalExpenditure.setText(currentCurrency + formatter.format(totalLoan));
                break;
        }
    }

    private Date parseTransactionDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MMMM, d yyyy", Locale.US),
                new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateString);
            } catch (ParseException e) {
            }
        }

        System.err.println("Could not parse date with any format: " + dateString);
        return null;
    }

    private void showMonthPickerDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.picker_month_dialog);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        TextView yearText = dialog.findViewById(R.id.tv_year);
        yearText.setText(String.valueOf(currentYear));

        String currentSelectedMonth = tvSelectedMonth.getText().toString();
        String[] currentParts = currentSelectedMonth.split(" ");
        if (currentParts.length > 0) {
            yearText.setText(currentParts[1]);
        }

        ImageButton prevYear = dialog.findViewById(R.id.btn_previous_year);
        ImageButton nextYear = dialog.findViewById(R.id.btn_next_year);

        prevYear.setOnClickListener(v -> {
            int year = Integer.parseInt(yearText.getText().toString()) - 1;
            yearText.setText(String.valueOf(year));
        });

        nextYear.setOnClickListener(v -> {
            int year = Integer.parseInt(yearText.getText().toString()) + 1;
            yearText.setText(String.valueOf(year));
        });

        int[] monthIds = {
                R.id.month_jan, R.id.month_feb, R.id.month_mar, R.id.month_apr,
                R.id.month_may, R.id.month_jun, R.id.month_jul, R.id.month_aug,
                R.id.month_sep, R.id.month_oct, R.id.month_nov, R.id.month_dec
        };

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        final TextView[] selectedMonthHolder = new TextView[1];

        String currentMonthAbbrev = currentSelectedMonth.split(" ")[0].substring(0, 3);
        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);
            if (monthView.getText().toString().equals(currentMonthAbbrev)) {
                monthView.setBackgroundResource(R.drawable.bg_selected_month);
                monthView.setTextColor(getResources().getColor(android.R.color.white));
                selectedMonthHolder[0] = monthView;
            }
        }

        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);

            monthView.setOnClickListener(v -> {
                for (int id : monthIds) {
                    dialog.findViewById(id).setBackgroundResource(android.R.color.transparent);
                    ((TextView) dialog.findViewById(id)).setTextColor(getResources().getColor(android.R.color.black));
                }

                v.setBackgroundResource(R.drawable.bg_selected_month);
                ((TextView) v).setTextColor(getResources().getColor(android.R.color.white));

                selectedMonthHolder[0] = (TextView) v;
            });
        }

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (selectedMonthHolder[0] != null) {
                String month = selectedMonthHolder[0].getText().toString();
                String year = yearText.getText().toString();
                String fullMonthName = months[Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").indexOf(month)];
                currentMonth = fullMonthName + " " + year;
                tvSelectedMonth.setText(currentMonth);
                filterTransactions();
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = width;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(getContext());
        allTransactionList = preferenceUtils.getTransactionList();
        filterTransactions();
        updateTotalAmount();
        regularAdapter.notifyDataSetChanged();
        loanAdapter.notifyDataSetChanged();

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


    private void loadNativeCollap(@Nullable final Runnable onLoaded) {
        if (!isAdded() || getContext() == null) return;

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
        if (!isAdded()) return;

        Log.d("homefragggggggggg", "loadNativeCollapB: ");
        Context context = requireContext();

        Admob.getInstance().loadNativeAd(context, getString(R.string.native_expand_home), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (!isAdded()) return;

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
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
        filterTransactions();
        updateTotalAmount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            int deletedPosition = data.getIntExtra("deleted_position", -1);
            if (deletedPosition != -1) {
                SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(getContext());
                List<TransactionModel> allTransactions = preferenceUtils.getTransactionList();

                for (int i = 0; i < filteredTransactionList.size(); i++) {
                    TransactionModel currentTransaction = filteredTransactionList.get(i);
                    boolean stillExists = false;
                    for (TransactionModel t : allTransactions) {
                        if (isSameTransaction(currentTransaction, t)) {
                            stillExists = true;
                            break;
                        }
                    }
                    if (!stillExists) {
                        filteredTransactionList.remove(i);
                        break;
                    }
                }
            }
            filterTransactions();
            updateTotalAmount();
            regularAdapter.notifyDataSetChanged();
            loanAdapter.notifyDataSetChanged();
        }
    }

    private boolean isSameTransaction(TransactionModel t1, TransactionModel t2) {
        return t1.getDate().equals(t2.getDate())
                && t1.getAmount().equals(t2.getAmount())
                && t1.getCategoryName().equals(t2.getCategoryName())
                && t1.getTransactionType().equals(t2.getTransactionType())
                && t1.getTime().equals(t2.getTime());
    }


}