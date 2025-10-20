package com.budgettracker.personalfinance.trackmoney.smartbudget.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.CategoryBottomSheetAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.BudgetManager;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.TransactionUpdateEvent;
import com.budgettracker.personalfinance.trackmoney.smartbudget.adapter.CategoryAdapter;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.BudgetItem;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.CategoryItem;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private SharePreferenceUtils sharePreferenceUtils;

    private TextView tvCurrency;
    private LinearLayout rbExpend, rbIncome, rbLoan;
    private EditText etAmount, etNote, etLender;
    private Spinner spBudget;
    private LinearLayout btnDate, btnTime;
    private TextView tv_date, tvTime;

    //    private RecyclerView rvCategories;
    private LinearLayout layoutLender, layoutBudget;
    private String currentTransactionType = "Expense";
    private int colorActive, colorInactive;
    private ImageView ivExpend, ivIncome, ivLoan, tvCancel;

    private View indicator_expense, indicator_income,indicator_loan;

    private LinearLayout tvSave;
    private TextView tvExpendLabel, tvIncomeLabel, tvLoanLabel;

    private String transactionType = "Expense";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private String selectedDate = "";
    private String selectedTime = "";

    private CategoryAdapter categoryAdapter;
    private CategoryItem selectedCategory;

    private List<CategoryItem> expenseCategories;
    private List<CategoryItem> incomeCategories;
    private List<CategoryItem> loanCategories;
    private FrameLayout frAds;
    private int editPosition = -1;

    private View categoryView;
    private TextView tvSelectedCategory;
    private ImageView ivSelectedCategoryIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction_activity);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        sharePreferenceUtils = new SharePreferenceUtils(this);


        initViews();
        initCategories();
        setupListeners();
        initBudget();
        selectedDate = dateFormat.format(new Date());
        selectedTime = timeFormat.format(new Date());

        tv_date.setText(selectedDate);
        tvTime.setText(selectedTime);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("transaction")) {
            TransactionModel transaction = (TransactionModel) intent.getSerializableExtra("transaction");
            editPosition = intent.getIntExtra("position", -1);
            if (transaction != null) {
                loadTransactionData(transaction);
            }
        } else {
            selectTransactionType("Expense");
        }
        loadPreviousData();
        loadAds();

    }

    private void loadTransactionData(TransactionModel transaction) {
        etAmount.setText(transaction.getAmount());
        etNote.setText(transaction.getNote());

        switch (transaction.getTransactionType()) {
            case "Expense":
                updateTabSelection(rbExpend);
                updateTabColors("Expense");
                selectTransactionType("Expense");
                break;
            case "Income":
                updateTabSelection(rbIncome);
                updateTabColors("Income");
                selectTransactionType("Income");
                break;
            case "Loan":
                updateTabSelection(rbLoan);
                updateTabColors("Loan");
                selectTransactionType("Loan");
                if (transaction.getLender() != null) {
                    etLender.setText(transaction.getLender());
                }
                break;
        }

        if ("Expense".equals(transaction.getTransactionType())) {
            setSpinnerSelection(spBudget, transaction.getBudget());
        }

        if (transaction.getDate() != null) {
            selectedDate = transaction.getDate();
            tv_date.setText(selectedDate);
        }

        if (transaction.getTime() != null) {
            selectedTime = transaction.getTime();
            tvTime.setText(selectedTime);
        }

        List<CategoryItem> categories = new ArrayList<>();
        switch (transaction.getTransactionType()) {
            case "Expense":
                categories = expenseCategories;
                break;
            case "Income":
                categories = incomeCategories;
                break;
            case "Loan":
                categories = loanCategories;
                break;
        }

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equals(transaction.getCategoryName())) {
                selectedCategory = categories.get(i);
                tvSelectedCategory.setText(selectedCategory.getName());
                ivSelectedCategoryIcon.setImageResource(selectedCategory.getIconResource());
                break;
            }
        }
    }

    private void initBudget() {
        BudgetManager budgetManager = new BudgetManager(this);
        List<BudgetItem> budgetItems = budgetManager.getBudgetItems();
        List<String> budgetNames = new ArrayList<>();
        budgetNames.add("None");
        for (BudgetItem item : budgetItems) {
            budgetNames.add(item.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, budgetNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBudget.setAdapter(adapter);
    }

    private void initViews() {
        tvCancel = findViewById(R.id.tv_cancel);
        tvSave = findViewById(R.id.ll_save);
        rbExpend = findViewById(R.id.ll_expend);
        rbIncome = findViewById(R.id.ll_income);
        rbLoan = findViewById(R.id.ll_loan);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        etLender = findViewById(R.id.et_lender);
        spBudget = findViewById(R.id.sp_budget);
        tvCurrency = findViewById(R.id.tv_currency);
        btnDate = findViewById(R.id.btn_date);
        btnTime = findViewById(R.id.btn_time);
        tv_date = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
//        rvCategories = findViewById(R.id.rv_categories);
        layoutLender = findViewById(R.id.layout_lender);
        layoutBudget = findViewById(R.id.layout_budget);

        categoryView = findViewById(R.id.category_view);
        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        ivSelectedCategoryIcon = findViewById(R.id.iv_selected_category_icon);
        if (expenseCategories != null && !expenseCategories.isEmpty()) {
            selectedCategory = expenseCategories.get(0);
            tvSelectedCategory.setText(selectedCategory.getName());
            ivSelectedCategoryIcon.setImageResource(selectedCategory.getIconResource());
        }
        frAds = findViewById(R.id.frAds);

        ivExpend = rbExpend.findViewById(R.id.iv_expend);
        tvExpendLabel = rbExpend.findViewById(R.id.tv_expend_label);
        ivIncome = rbIncome.findViewById(R.id.iv_income);
        tvIncomeLabel = rbIncome.findViewById(R.id.tv_income_label);
        ivLoan = rbLoan.findViewById(R.id.iv_loan);
        tvLoanLabel = rbLoan.findViewById(R.id.tv_loan_label);

        indicator_expense = findViewById(R.id.indicator_expense);
        indicator_income = findViewById(R.id.indicator_income);
        indicator_loan = findViewById(R.id.indicator_loan);


        colorActive = getResources().getColor(android.R.color.black);
        colorInactive = getResources().getColor(R.color.icon_inactive);




        updateTabColors("Expense");
        configAmount();


        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "USD";
        tvCurrency.setText(currentCurrency);
    }

    private void loadAds() {
        if (Admob.getInstance().isLoadFullAds()) {
            Admob.getInstance().loadNativeAd(this, getString(R.string.native_all), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView;
                    adView = (NativeAdView) LayoutInflater.from(AddTransactionActivity.this)
                            .inflate(R.layout.layout_native_language, null);
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

    void configAmount() {
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
    }

    private void showCategoryBottomSheet(List<CategoryItem> categories) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_category, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        RecyclerView rvCategories = bottomSheetView.findViewById(R.id.rv_categories_bottom_sheet);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 4));

        CategoryBottomSheetAdapter adapter = new CategoryBottomSheetAdapter(this, categories, (category, position) -> {
            selectedCategory = category;
            tvSelectedCategory.setText(category.getName());
            ivSelectedCategoryIcon.setImageResource(category.getIconResource());
            bottomSheetDialog.dismiss();
        });

        rvCategories.setAdapter(adapter);
        bottomSheetDialog.show();
    }

    private void updateTabSelection(LinearLayout selectedTab) {
        rbExpend.setSelected(false);
        rbIncome.setSelected(false);
        rbLoan.setSelected(false);

        selectedTab.setSelected(true);
    }

    private void updateTabColors(String selectedType) {
        currentTransactionType = selectedType;

        ivExpend.setColorFilter(currentTransactionType.equals("Expense") ? colorActive : colorInactive);
        tvExpendLabel.setTextColor(currentTransactionType.equals("Expense") ? colorActive : colorInactive);

        indicator_expense.setVisibility(currentTransactionType.equals("Expense") ? colorActive : colorInactive);
        indicator_income.setVisibility(currentTransactionType.equals("Income") ? colorActive : colorInactive);
        indicator_loan.setVisibility(currentTransactionType.equals("Loan") ? colorActive : colorInactive);

        ivIncome.setColorFilter(currentTransactionType.equals("Income") ? colorActive : colorInactive);
        tvIncomeLabel.setTextColor(currentTransactionType.equals("Income") ? colorActive : colorInactive);

        ivLoan.setColorFilter(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
        tvLoanLabel.setTextColor(currentTransactionType.equals("Loan") ? colorActive : colorInactive);
    }

    private void setupListeners() {
        tvCancel.setOnClickListener(view -> onBackPressed());
        tvSave.setEnabled(false);
        tvSave.setOnClickListener(view -> {

            if (Admob.getInstance().isLoadFullAds()) {
                Admob.getInstance().loadAndShowInter(AddTransactionActivity.this, getString(R.string.inter_save), 0, 30000, new InterCallback() {

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        saveTransactionData();

                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        saveTransactionData();

                    }
                });
            } else {
                saveTransactionData();

            }

        });

        rbExpend.setOnClickListener(v -> {
            updateTabColors("Expense");
            updateTabSelection(rbExpend);
            selectTransactionType("Expense");
        });

        rbIncome.setOnClickListener(v -> {
            updateTabColors("Income");
            updateTabSelection(rbIncome);
            selectTransactionType("Income");
        });

        rbLoan.setOnClickListener(v -> {

            updateTabColors("Loan");

            updateTabSelection(rbLoan);
            selectTransactionType("Loan");
        });

        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        tv_date.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        tvTime.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });
        categoryView.setOnClickListener(v -> {
            switch (transactionType) {
                case "Expense":
                    showCategoryBottomSheet(expenseCategories);
                    break;
                case "Income":
                    showCategoryBottomSheet(incomeCategories);
                    break;
                case "Loan":
                    showCategoryBottomSheet(loanCategories);
                    break;
            }
        });
    }

    private void initCategories() {
        expenseCategories = new ArrayList<>();
        expenseCategories.add(new CategoryItem(R.drawable.ic_food, "Food"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_social, "Social"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_trafic, "Traffic"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_shopping, "Shopping"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_grocery, "Grocery"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_education, "Education"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_bills, "Bills"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_rentals, "Rentals"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_medical, "Medical"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_investment, "Investment"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_gift, "Gift"));
        expenseCategories.add(new CategoryItem(R.drawable.ic_other, "Other"));

        incomeCategories = new ArrayList<>();
        incomeCategories.add(new CategoryItem(R.drawable.ic_salary, "Salary"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_invest, "Invest"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_business, "Business"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_interest, "Interest"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_extra_income, "Extra Income"));
        incomeCategories.add(new CategoryItem(R.drawable.ic_other, "Other"));

        loanCategories = new ArrayList<>();
        loanCategories.add(new CategoryItem(R.drawable.ic_loan, "Loan"));
        loanCategories.add(new CategoryItem(R.drawable.ic_borrow, "Borrow"));
    }

//    private void setupCategoryGrid(List<CategoryItem> categories) {
//        categoryAdapter = new CategoryAdapter(this, categories, (category, position) -> {
//            selectedCategory = category;
//        });
//
//        rvCategories.setLayoutManager(new GridLayoutManager(this, 4)); // 4 columns
//        rvCategories.setAdapter(categoryAdapter);
//    }

    private void selectTransactionType(String type) {
        transactionType = type;

        switch (type) {
            case "Expense":
                layoutBudget.setVisibility(View.VISIBLE);
                layoutLender.setVisibility(View.GONE);
                if (expenseCategories != null && !expenseCategories.isEmpty()) {
                    selectedCategory = expenseCategories.get(0);
                    tvSelectedCategory.setText(selectedCategory.getName());
                    ivSelectedCategoryIcon.setImageResource(selectedCategory.getIconResource());
                }
                break;

            case "Income":
                layoutBudget.setVisibility(View.GONE);
                layoutLender.setVisibility(View.GONE);
                if (incomeCategories != null && !incomeCategories.isEmpty()) {
                    selectedCategory = incomeCategories.get(0);
                    tvSelectedCategory.setText(selectedCategory.getName());
                    ivSelectedCategoryIcon.setImageResource(selectedCategory.getIconResource());
                }
                break;

            case "Loan":
                layoutBudget.setVisibility(View.GONE);
                layoutLender.setVisibility(View.VISIBLE);
                if (loanCategories != null && !loanCategories.isEmpty()) {
                    selectedCategory = loanCategories.get(0);
                    tvSelectedCategory.setText(selectedCategory.getName());
                    ivSelectedCategoryIcon.setImageResource(selectedCategory.getIconResource());
                }
                break;
        }
    }

    private void saveTransactionData() {
        if (etAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(this);
        if (currentCurrency.isEmpty()) currentCurrency = "USD";

        String amount = etAmount.getText().toString().replaceAll(",", "");
        String note = etNote.getText().toString();
        String budget = "None";

        if ("Expense".equals(transactionType)) {
            budget = spBudget.getSelectedItem().toString();
        }

        TransactionModel transaction = new TransactionModel(
                transactionType,
                amount,
                currentCurrency,
                selectedCategory.getName(),
                selectedCategory.getIconResource(),
                budget,
                note,
                selectedDate,
                selectedTime
        );

        if ("Loan".equals(transactionType)) {
            String lenderText = etLender.getText().toString();
            transaction.setLender(lenderText);
        }

        List<TransactionModel> transactions = sharePreferenceUtils.getTransactionList();

        if (editPosition != -1 && editPosition < transactions.size()) {
            transactions.set(editPosition, transaction);
            System.out.println("ádasdqwgqwgqwL" + editPosition);
        } else {
            transactions.add(transaction);
        }

        sharePreferenceUtils.saveTransactionList(transactions);
        EventBus.getDefault().post(new TransactionUpdateEvent(transactions));

        if ("Expense".equals(transactionType) && !"None".equals(budget)) {
            BudgetManager budgetManager = new BudgetManager(this);
            budgetManager.updateBudgetExpenses(budget);
        }

        Gson gson = new Gson();
        String transactionJson = gson.toJson(transaction);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("transactionData", transactionJson);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void loadPreviousData() {
        TransactionModel transaction = sharePreferenceUtils.getTransaction();
        if (transaction != null) {
            etAmount.setText(transaction.getAmount());
            etNote.setText(transaction.getNote());

            switch (transaction.getTransactionType()) {
                case "Expense":
                    updateTabSelection(rbExpend);
                    updateTabColors("Expense");
                    selectTransactionType("Expense");
                    break;
                case "Income":
                    updateTabSelection(rbIncome);
                    updateTabColors("Income");
                    selectTransactionType("Income");
                    break;
                case "Loan":
                    updateTabSelection(rbLoan);
                    updateTabColors("Loan");
                    selectTransactionType("Loan");
                    if (transaction.getLender() != null) {
                        etLender.setText(transaction.getLender());
                    }
                    break;
            }

            if ("Expense".equals(transaction.getTransactionType())) {
                setSpinnerSelection(spBudget, transaction.getBudget());
            }

            if (transaction.getDate() != null) {
                selectedDate = transaction.getDate();
                tv_date.setText(selectedDate);
            }

            if (transaction.getTime() != null) {
                selectedTime = transaction.getTime();
                tvTime.setText(selectedTime);
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvSave.setEnabled(true);
    }
}