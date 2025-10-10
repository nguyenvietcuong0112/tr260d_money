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
import com.budgettracker.personalfinance.trackmoney.smartbudget.activity.TransactionDetailActivity;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoanTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SUMMARY_HEADER = 0;
    private static final int TYPE_DATE_HEADER = 1;
    private static final int TYPE_TRANSACTION = 2;

    private List<Object> items = new ArrayList<>();
    private Map<String, List<TransactionModel>> transactionsByDate = new TreeMap<>();
    private double totalBorrow = 0;
    private double totalLoan = 0;

    public void updateData(List<TransactionModel> transactions) {
        items.clear();
        transactionsByDate.clear();
        totalBorrow = 0;
        totalLoan = 0;

        for (TransactionModel transaction : transactions) {
            double amount = Double.parseDouble(transaction.getAmount());
            if ("Borrow".equals(transaction.getCategoryName())) {
                totalBorrow += amount;
            } else if ("Loan".equals(transaction.getCategoryName())) {
                totalLoan += amount;
            }

            String date = transaction.getDate();
            if (!transactionsByDate.containsKey(date)) {
                transactionsByDate.put(date, new ArrayList<>());
            }
            transactionsByDate.get(date).add(transaction);
        }

        items.add(new SummaryHeader(totalBorrow, totalLoan));

        for (Map.Entry<String, List<TransactionModel>> entry : transactionsByDate.entrySet()) {
            items.add(new DateHeader(entry.getKey()));
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof SummaryHeader) return TYPE_SUMMARY_HEADER;
        if (item instanceof DateHeader) return TYPE_DATE_HEADER;
        return TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_SUMMARY_HEADER:
                return new SummaryHeaderViewHolder(
                        inflater.inflate(R.layout.item_loan_summary_header, parent, false)
                );
            case TYPE_DATE_HEADER:
                return new DateHeaderViewHolder(
                        inflater.inflate(R.layout.item_date_header, parent, false)
                );
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(holder.itemView.getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        if (holder instanceof SummaryHeaderViewHolder) {
            ((SummaryHeaderViewHolder) holder).bind((SummaryHeader) item, currentCurrency);
        } else if (holder instanceof DateHeaderViewHolder) {
            DateHeader dateHeader = (DateHeader) item;
            DateHeaderViewHolder dateHeaderViewHolder = (DateHeaderViewHolder) holder;

            List<TransactionModel> dateTransactions = transactionsByDate.get(dateHeader.date);
            dateHeaderViewHolder.bind(dateHeader.date, dateTransactions);

            BigDecimal totalLoan = BigDecimal.ZERO;
            BigDecimal totalBorrow = BigDecimal.ZERO;

            if (dateTransactions != null) {
                for (TransactionModel transaction : dateTransactions) {
                    String category = transaction.getCategoryName();
                    BigDecimal amount = new BigDecimal(transaction.getAmount());

                    if ("Loan".equals(category)) {
                        totalLoan = totalLoan.add(amount);
                    } else if ("Borrow".equals(category)) {
                        totalBorrow = totalBorrow.add(amount);
                    }
                }
            }

            BigDecimal dateTotal = totalLoan.subtract(totalBorrow);
            double totalAmount = dateTotal.doubleValue();
            dateHeaderViewHolder.setTotalAmount(totalAmount, currentCurrency);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SummaryHeader {
        double borrowTotal;
        double loanTotal;

        SummaryHeader(double borrowTotal, double loanTotal) {
            this.borrowTotal = borrowTotal;
            this.loanTotal = loanTotal;
        }
    }

    static class DateHeader {
        String date;

        DateHeader(String date) {
            this.date = date;
        }
    }

    static class SummaryHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvBorrowAmount, tvLoanAmount;

        SummaryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBorrowAmount = itemView.findViewById(R.id.tv_borrow_amount);
            tvLoanAmount = itemView.findViewById(R.id.tv_loan_amount);
        }

        void bind(SummaryHeader header, String currency) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tvBorrowAmount.setText(currency + formatter.format(header.borrowTotal));
            tvLoanAmount.setText(currency + formatter.format(header.loanTotal));
        }
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateNumber, tvDayOfWeek, tvMonthYear, tvTotalAmount;
        RecyclerView rvTransactions;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateNumber = itemView.findViewById(R.id.tv_date_number);
            tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
            tvMonthYear = itemView.findViewById(R.id.tv_month_year);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            rvTransactions = itemView.findViewById(R.id.rv_transactions);

            rvTransactions.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvTransactions.setNestedScrollingEnabled(false);
        }

        void bind(String dateString, List<TransactionModel> transactions) {
            try {
                Date date = new SimpleDateFormat("dd/M/yyyy", Locale.US).parse(dateString);
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);


                    SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.US);
                    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.US);
                    SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

                    tvDateNumber.setText(dayNumberFormat.format(calendar.getTime()));
                    tvDayOfWeek.setText(dayOfWeekFormat.format(calendar.getTime()));
                    tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (transactions != null && rvTransactions != null) {
                TransactionItemAdapter nestedAdapter = new TransactionItemAdapter(transactions);
                rvTransactions.setAdapter(nestedAdapter);
                rvTransactions.setVisibility(View.VISIBLE);
            } else {
                rvTransactions.setVisibility(View.GONE);
            }
        }

        void setTotalAmount(double amount, String currency) {
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            int textColor;
            String prefix;

            if (amount < 0) {
                textColor = itemView.getContext().getResources().getColor(R.color.red);
                prefix = "-" + currency + " ";
                amount = Math.abs(amount);
            } else {
                textColor = itemView.getContext().getResources().getColor(R.color.green);
                prefix = currency + " ";
            }

            String formattedAmount = formatter.format(amount);
            tvTotalAmount.setText(prefix + formattedAmount);
            tvTotalAmount.setTextColor(textColor);
        }
    }

    static class TransactionItemAdapter extends RecyclerView.Adapter<TransactionViewHolder> {
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
            holder.bind(transactions.get(position), "$"); // Hoặc lấy currency dynamic nếu bạn thích
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

        void bind(TransactionModel transaction, String currency) {
            tvCategory.setText(transaction.getCategoryName().isEmpty() ? "Category" : transaction.getCategoryName());
            tvTime.setText(transaction.getTime());

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            String prefix = "Borrow".equals(transaction.getCategoryName()) ? "- " : "+ ";
            String amountText = prefix + currency + formatter.format(Double.parseDouble(transaction.getAmount()));

            int textColor = itemView.getContext().getResources().getColor(
                    "Borrow".equals(transaction.getCategoryName()) ? R.color.red : R.color.green
            );

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
