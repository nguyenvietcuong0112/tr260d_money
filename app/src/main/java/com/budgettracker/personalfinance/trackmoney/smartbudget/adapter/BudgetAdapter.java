package com.budgettracker.personalfinance.trackmoney.smartbudget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.BudgetManager;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.CircularProgressViewDetail;
import com.budgettracker.personalfinance.trackmoney.smartbudget.utils.SharePreferenceUtils;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.BudgetItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private Context context;
    private List<BudgetItem> budgets;
    private BudgetItemListener listener;
    private BudgetManager budgetManager;


    public interface BudgetItemListener {
        void onBudgetItemClick(BudgetItem item);
    }

    public BudgetAdapter(Context context, List<BudgetItem> budgets, BudgetItemListener listener,BudgetManager budgetManager) {
        this.context = context;
        this.budgets = budgets;
        this.listener = listener;
        this.budgetManager = budgetManager;

    }

    public void updateBudgets(List<BudgetItem> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    @Override
    public BudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BudgetViewHolder holder, int position) {
        BudgetItem item = budgets.get(position);
        String currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(context);
        if (currentCurrency.isEmpty()) currentCurrency = "$";
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);

        holder.tvName.setText(item.getName());
        holder.tvAmount.setText(currentCurrency + formatter.format(item.getTotalAmount()));


        double expenses = budgetManager.getExpensesForBudget(item.getName());
        double remaining = item.getTotalAmount() - expenses;
        int progress = item.getTotalAmount() > 0 ? (int) ((remaining / item.getTotalAmount()) * 100) : 0;

        holder.progressView.setProgress(progress);
        holder.progressView.setProgressColor(item.getColor());
        holder.progressView.setShowRemainingText(false);

        holder.itemView.setOnClickListener(v -> listener.onBudgetItemClick(item));
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        CircularProgressViewDetail progressView;

        BudgetViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_budget_name);
            tvAmount = itemView.findViewById(R.id.tv_budget_amount);
            progressView = itemView.findViewById(R.id.progress_view);
        }
    }
}