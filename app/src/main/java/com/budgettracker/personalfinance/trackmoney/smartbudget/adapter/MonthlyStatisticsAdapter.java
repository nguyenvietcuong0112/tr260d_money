package com.budgettracker.personalfinance.trackmoney.smartbudget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.MonthlyStatisticsModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MonthlyStatisticsAdapter extends RecyclerView.Adapter<MonthlyStatisticsAdapter.ViewHolder> {

    private List<MonthlyStatisticsModel> monthlyStatistics;
    private String currencySymbol;

    public MonthlyStatisticsAdapter(List<MonthlyStatisticsModel> monthlyStatistics, String currencySymbol) {
        this.monthlyStatistics = monthlyStatistics;
        this.currencySymbol = currencySymbol;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_monthly_statistics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthlyStatisticsModel item = monthlyStatistics.get(position);
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);

        holder.tvMonth.setText(item.getMonth());

        if (item.getExpend() > 0) {
            holder.tvExpend.setText(formatter.format(item.getExpend()));
        } else {
            holder.tvExpend.setText("-");
        }

        if (item.getIncome() > 0) {
            holder.tvIncome.setText(formatter.format(item.getIncome()));
        } else {
            holder.tvIncome.setText("-");
        }

        if (item.getLoan() > 0) {
            holder.tvLoan.setText(formatter.format(item.getLoan()));
        } else {
            holder.tvLoan.setText("-");
        }

        if (item.getBorrow() > 0) {
            holder.tvBorrow.setText(formatter.format(item.getBorrow()));
        } else {
            holder.tvBorrow.setText("-");
        }

        holder.tvBalance.setText(formatter.format(item.getBalance()));

        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.color.white);
        } else {
            holder.itemView.setBackgroundResource(R.color.background);
        }
    }

    @Override
    public int getItemCount() {
        return monthlyStatistics.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth, tvExpend, tvIncome, tvLoan, tvBorrow, tvBalance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvExpend = itemView.findViewById(R.id.tv_expend);
            tvIncome = itemView.findViewById(R.id.tv_income);
            tvLoan = itemView.findViewById(R.id.tv_loan);
            tvBorrow = itemView.findViewById(R.id.tv_borrow);
            tvBalance = itemView.findViewById(R.id.tv_balance);
        }
    }
}