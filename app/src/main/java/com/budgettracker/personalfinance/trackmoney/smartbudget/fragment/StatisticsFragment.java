package com.budgettracker.personalfinance.trackmoney.smartbudget.fragment;

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

import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.MainActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.LoadNativeFullNew;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.Gson;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.StatisticDetailActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.TransactionUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsFragment extends Fragment {
    private TextView tvTotalBalance, tvSelectedMonth;
    private LinearLayout llExpend, llIncome, llLoan, llSelectedMmonth;
    private LinearLayout llCategoryList;
    private BarChart barChart;

    private List<TransactionModel> allTransactionList = new ArrayList<>();
    private String currentTransactionType = "Expense";
    private String currentMonth;
    private Map<String, Double> categoryTotals = new HashMap<>();
    String currentCurrency;

    ImageView nextStatistic;
    LinearLayout llBanner;
    private LinearLayout noDataView;
    private boolean isFirstLoad = true;


    FrameLayout fr_ads;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        initViews(view);
        loadTransactionData();
        setupClickListeners();
        setupInitialFilter();

        return view;
    }

    private void initViews(View view) {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        llSelectedMmonth = view.findViewById(R.id.ll_selected_month);
        llExpend = view.findViewById(R.id.ll_expend);
        llIncome = view.findViewById(R.id.ll_income);
        llLoan = view.findViewById(R.id.ll_loan);
        llCategoryList = view.findViewById(R.id.ll_category_list);
        barChart = view.findViewById(R.id.bar_chart);
        nextStatistic = view.findViewById(R.id.next_statistic);
        llBanner = view.findViewById(R.id.ll_banner);
        noDataView = view.findViewById(R.id.layout_no_data);
        fr_ads = view.findViewById(R.id.fr_ads);
    }



    private void loadTransactionData() {
        allTransactionList.clear();
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(getContext());
        allTransactionList = preferenceUtils.getTransactionList();
        if (allTransactionList == null) {
            allTransactionList = new ArrayList<>();
        }
    }

    private void setupClickListeners() {
        llExpend.setOnClickListener(v -> {
            currentTransactionType = "Expense";
            updateTransactionTypeUI();
            updateStatistics();
        });

        llIncome.setOnClickListener(v -> {
            currentTransactionType = "Income";
            updateTransactionTypeUI();
            updateStatistics();
        });

        llLoan.setOnClickListener(v -> {
            currentTransactionType = "Loan";
            updateTransactionTypeUI();
            updateStatistics();
        });

        llSelectedMmonth.setOnClickListener(v -> showMonthPickerDialog());

        nextStatistic.setOnClickListener(v -> {
            nextStatistic.setEnabled(false);
            if (Admob.getInstance().isLoadFullAds()) {
                Admob.getInstance().loadSplashInterAds2(requireContext(), getString(R.string.inter_balance), 0, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Bundle bundle = new Bundle();

                        String transactionListJson = new Gson().toJson(allTransactionList);
                        bundle.putString("transactionList", transactionListJson);

                        bundle.putString("currentTransactionType", currentTransactionType);
                        bundle.putString("currentMonth", currentMonth);

                        Intent intent = new Intent(getActivity(), StatisticDetailActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }

                    @Override
                    public void onAdClosedByUser() {
                        super.onAdClosedByUser();
                        Bundle bundle = new Bundle();

                        String transactionListJson = new Gson().toJson(allTransactionList);
                        bundle.putString("transactionList", transactionListJson);

                        bundle.putString("currentTransactionType", currentTransactionType);
                        bundle.putString("currentMonth", currentMonth);

                        Intent intent = new Intent(getActivity(), StatisticDetailActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            } else {
                Bundle bundle = new Bundle();

                String transactionListJson = new Gson().toJson(allTransactionList);
                bundle.putString("transactionList", transactionListJson);

                bundle.putString("currentTransactionType", currentTransactionType);
                bundle.putString("currentMonth", currentMonth);

                Intent intent = new Intent(getActivity(), StatisticDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

        });
    }

    private void setupInitialFilter() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        currentMonth = dateFormat.format(new Date());
        tvSelectedMonth.setText(currentMonth);
        updateTransactionTypeUI();
        updateStatistics();
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

        int colorActive = getResources().getColor(R.color.black);
        int colorInactive = getResources().getColor(R.color.icon_inactive);

        ivExpend.setColorFilter(currentTransactionType.equals("Expense") ? colorActive : colorInactive);
        tvExpendLabel.setTextColor(currentTransactionType.equals("Expense") ? colorActive : colorInactive);

        ivIncome.setColorFilter(currentTransactionType.equals("Income") ? colorActive : colorInactive);
        tvIncomeLabel.setTextColor(currentTransactionType.equals("Income") ? colorActive : colorInactive);

        ivLoan.setColorFilter(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
    }

    private void updateStatistics() {
        categoryTotals.clear();
        double totalAmount = 0;

        List<TransactionModel> filteredTransactions = filterTransactionsByMonthAndType();

        for (TransactionModel transaction : filteredTransactions) {
            String category = transaction.getCategoryName();
            double amount = Double.parseDouble(transaction.getAmount());
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            totalAmount += amount;
        }

        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        tvTotalBalance.setText(currentCurrency + " "+ formatter.format(totalAmount));
        if (filteredTransactions.isEmpty() || categoryTotals.isEmpty()) {
            noDataView.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
        } else {
            noDataView.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);

            updateCategoryList();
            updateChart();
        }
    }

    private List<TransactionModel> filterTransactionsByMonthAndType() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        return allTransactionList.stream()
                .filter(transaction -> {
                    try {
                        Date transactionDate = parseTransactionDate(transaction.getDate());
                        return transaction.getTransactionType().equals(currentTransactionType) &&
                                monthFormat.format(transactionDate).equals(currentMonth);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void updateCategoryList() {
        llCategoryList.removeAllViews();

        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryTotals.entrySet());
        sortedCategories.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        for (Map.Entry<String, Double> entry : sortedCategories) {
            View categoryItem = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_statistics_category, llCategoryList, false);

            TextView tvCategory = categoryItem.findViewById(R.id.tvCategoryName);
            TextView tvAmount = categoryItem.findViewById(R.id.tvCategoryAmount);
            ImageView ivCategory = categoryItem.findViewById(R.id.ivCategoryIcon);

            tvCategory.setText(entry.getKey());
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvAmount.setText(currentCurrency + formatter.format(entry.getValue()));

            Map<String, Integer> categoryIcons = new HashMap<>();

            categoryIcons.put("Food", R.drawable.ic_food);
            categoryIcons.put("Social", R.drawable.ic_social);
            categoryIcons.put("Traffic", R.drawable.ic_trafic);
            categoryIcons.put("Shopping", R.drawable.ic_shopping);
            categoryIcons.put("Grocery", R.drawable.ic_grocery);
            categoryIcons.put("Education", R.drawable.ic_education);
            categoryIcons.put("Bills", R.drawable.ic_bills);
            categoryIcons.put("Rentals", R.drawable.ic_rentals);
            categoryIcons.put("Medical", R.drawable.ic_medical);
            categoryIcons.put("Investment", R.drawable.ic_investment);
            categoryIcons.put("Gift", R.drawable.ic_gift);
            categoryIcons.put("Others", R.drawable.ic_other);


            Integer iconRes = categoryIcons.get(entry.getKey());
            if (iconRes != null) {
                ivCategory.setImageResource(iconRes);
            } else {
                ivCategory.setImageResource(R.drawable.ic_other);
            }

            llCategoryList.addView(categoryItem);
        }
    }

    private void updateChart() {
        barChart.clear();

        List<BarEntry> entries = new ArrayList<>();
        List<String> categoryLabels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            categoryLabels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#FF9E44"),
                Color.parseColor("#E84141"),
                Color.parseColor("#47C7FF"),
                Color.parseColor("#FF7199"),
                Color.parseColor("#22D47D"),
                Color.parseColor("#8FBEFF"),
                Color.parseColor("#FFD859"),
                Color.parseColor("#2DAAD8"),
                Color.parseColor("#24B26D"),
                Color.parseColor("#FF9E44"),
                Color.parseColor("#E84141"),
                Color.parseColor("#47C7FF")
        );
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleEnabled(false);
        barData.setBarWidth(0.5f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                return index >= 0 && index < categoryLabels.size() ? categoryLabels.get(index) : "";
            }
        });
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        barChart.invalidate();
    }

    private void showMonthPickerDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.picker_month_dialog);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        TextView yearText = dialog.findViewById(R.id.tv_year);
        yearText.setText(String.valueOf(currentYear));

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

        String currentSelectedMonth = tvSelectedMonth.getText().toString();
        String currentMonthAbbrev = "";
        if (!currentSelectedMonth.isEmpty()) {
            currentMonthAbbrev = currentSelectedMonth.split(" ")[0].substring(0, 3);
        }

        for (int i = 0; i < monthIds.length; i++) {
            TextView monthView = dialog.findViewById(monthIds[i]);
            monthView.setText(months[i].substring(0, 3));

            if (monthView.getText().toString().equals(currentMonthAbbrev)) {
                monthView.setBackgroundResource(R.drawable.bg_selected_month);
                monthView.setTextColor(getResources().getColor(android.R.color.white));
                selectedMonthHolder[0] = monthView;
            }
        }

        for (int id : monthIds) {
            TextView monthView = dialog.findViewById(id);

            monthView.setOnClickListener(v -> {
                for (int idInner : monthIds) {
                    dialog.findViewById(idInner).setBackgroundResource(android.R.color.transparent);
                    ((TextView) dialog.findViewById(idInner)).setTextColor(getResources().getColor(android.R.color.black));
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
                updateStatistics();
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


    private Date parseTransactionDate(String dateString) {
        SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("MMMM, d yyyy", Locale.US),
                new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        };

        for (SimpleDateFormat format : dateFormats) {
            try {
                return format.parse(dateString);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdated(TransactionUpdateEvent event) {
        allTransactionList = event.getTransactionList();
        loadTransactionData();
        updateStatistics();


    }

    private void loadNative() {
        if (!isAdded()) return;

        if (fr_ads != null) {
            fr_ads.removeAllViews();
        }
        if (Admob.getInstance().isLoadFullAds()) {
            Admob.getInstance().loadNativeAd(requireContext(), getString(R.string.native_home), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    if (getContext() == null || !isAdded() || nativeAd == null) {
                        return;
                    }

                    NativeAdView adView = (NativeAdView) LayoutInflater.from(requireContext()).inflate(R.layout.layout_native_home, null);
                    if (fr_ads != null) {
                        fr_ads.removeAllViews();
                        fr_ads.addView(adView);
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    }

                }

                @Override
                public void onAdFailedToLoad() {
                    if (!isAdded() || getContext() == null) return;

                    if (fr_ads != null) {
                        fr_ads.removeAllViews();
                    }

                }
            });
        } else {
            fr_ads.removeAllViews();
            fr_ads.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        nextStatistic.setEnabled(true);
        SharePreferenceUtils preferenceUtils = SharePreferenceUtils.getInstance(getContext());
        allTransactionList = preferenceUtils.getTransactionList();
        loadTransactionData();
        updateStatistics();
        loadNative();
    }

    @Override
    public void onPause() {
        super.onPause();

    }
}