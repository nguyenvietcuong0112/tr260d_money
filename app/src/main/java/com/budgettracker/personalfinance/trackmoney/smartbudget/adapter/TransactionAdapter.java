package com.budgettracker.personalfinance.trackmoney.smartbudget.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.TransactionDetailActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<TransactionModel>> transactionsByDate = new TreeMap<>(Collections.reverseOrder());
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.US);
    private SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.US);
    private SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.US);
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

    public TransactionAdapter(List<TransactionModel> transactionList) {
        updateData(transactionList, null);
    }

    public void updateData(List<TransactionModel> transactionList, Map<String, List<TransactionModel>> transactionsByDate) {
        items.clear();

        if (transactionsByDate != null) {
            this.transactionsByDate = new TreeMap<>(Collections.reverseOrder());
            this.transactionsByDate.putAll(transactionsByDate);

            // First, add only the headers to ensure they are in the correct order
            for (String date : this.transactionsByDate.keySet()) {
                items.add(date);
            }
        } else {
            items.addAll(transactionList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_DATE_HEADER;
        } else {
            return TYPE_TRANSACTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_DATE_HEADER) {
            DateHeaderViewHolder headerHolder = (DateHeaderViewHolder) holder;
            String dateString = (String) items.get(position);
            headerHolder.bind(dateString);

            double totalAmount = 0;
            if (transactionsByDate.containsKey(dateString)) {
                for (TransactionModel transaction : transactionsByDate.get(dateString)) {
                    if (transaction.getTransactionType().equals("Expense")) {
                        totalAmount -= Double.parseDouble(transaction.getAmount());
                    } else if (transaction.getTransactionType().equals("Income")) {
                        totalAmount += Double.parseDouble(transaction.getAmount());
                    }
                }
            }
            headerHolder.setTotalAmount(totalAmount);

            List<TransactionModel> transactions = transactionsByDate.get(dateString);
            if (transactions != null && !transactions.isEmpty()) {
                Collections.sort(transactions, (t1, t2) -> t2.getTime().compareTo(t1.getTime()));

                TransactionItemAdapter nestedAdapter = new TransactionItemAdapter(transactions);
                headerHolder.rvTransactions.setAdapter(nestedAdapter);
                headerHolder.rvTransactions.setVisibility(View.VISIBLE);
            } else {
                headerHolder.rvTransactions.setVisibility(View.GONE);
            }
        } else {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            TransactionModel transaction = (TransactionModel) items.get(position);
            transactionHolder.bind(transaction);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateNumber, tvDayOfWeek, tvMonthYear, tvTotalAmount;
        View divider;
        RecyclerView rvTransactions;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateNumber = itemView.findViewById(R.id.tv_date_number);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            tvMonthYear = itemView.findViewById(R.id.tv_month_year);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            divider = itemView.findViewById(R.id.divider);
            rvTransactions = itemView.findViewById(R.id.rv_transactions);

            // Set up the RecyclerView
            rvTransactions.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvTransactions.setNestedScrollingEnabled(false);
        }

        void bind(String dateString) {
            try {
                Date date = new SimpleDateFormat("EEE, dd MMMM", Locale.US).parse(dateString);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    Calendar now = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));

                    SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.US);
                    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.US);
                    SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

                    tvDateNumber.setText(dayNumberFormat.format(calendar.getTime()));
                    tvDayOfWeek.setText(dayOfWeekFormat.format(calendar.getTime()));
                    tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                // Fallback to original date string if parsing fails
                tvDateNumber.setText("");
                tvDayOfWeek.setText(dateString);
                tvMonthYear.setText("");
            }
        }

        void setTotalAmount(double amount) {
            String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(itemView.getContext());
            if (currentCurrency.isEmpty()) currentCurrency = "$";
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            String formattedAmount = formatter.format(Math.abs(amount));

            // Set color based on amount (negative = red, positive = green)
            int textColor;
            String prefix = "";
            if (amount < 0) {
                textColor = itemView.getContext().getResources().getColor(R.color.black);
                prefix = "-" + currentCurrency + " ";
            } else if (amount > 0) {
                textColor = itemView.getContext().getResources().getColor(R.color.black);
                prefix = currentCurrency + " ";
            } else {
                textColor = itemView.getContext().getResources().getColor(R.color.black);
                prefix = currentCurrency + " ";
            }

            tvTotalAmount.setText(prefix + formattedAmount);
            tvTotalAmount.setTextColor(textColor);
        }
    }

    // Inner adapter for transactions within a date group
    class TransactionItemAdapter extends RecyclerView.Adapter<TransactionViewHolder> {
        private List<TransactionModel> transactions;

        TransactionItemAdapter(List<TransactionModel> transactions) {
            this.transactions = transactions;
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            holder.bind(transactions.get(position));
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvTime;
        ImageView imCategory;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
            imCategory = itemView.findViewById(R.id.iv_category);
        }

        void bind(TransactionModel transaction) {
            tvCategory.setText(transaction.getCategoryName());
            tvTime.setText(transaction.getTime());
            String amountPrefix = "";
            int textColor = 0;

            switch (transaction.getTransactionType()) {
                case "Income":
                    amountPrefix = "+ ";
                    textColor = itemView.getContext().getResources().getColor(R.color.green);
                    break;
                case "Expense":
                    amountPrefix = "- ";
                    textColor = itemView.getContext().getResources().getColor(R.color.red);
                    break;
                case "Loan":
                    amountPrefix = "~ ";
                    textColor = itemView.getContext().getResources().getColor(R.color.black);
                    break;
            }
            String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(itemView.getContext());
            if (currentCurrency.isEmpty()) currentCurrency = "$";
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);

            String amountText = amountPrefix + currentCurrency + formatter.format(Double.parseDouble(transaction.getAmount()));
            tvAmount.setText(amountText);
            tvAmount.setTextColor(textColor);
            imCategory.setBackgroundResource(transaction.getCategoryIcon());

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), TransactionDetailActivity.class);
                intent.putExtra("transaction", transaction);
                Fragment fragment = ((AppCompatActivity) itemView.getContext())
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    fragment.startActivityForResult(intent, 1001);
                }
            });
        }
    }
}