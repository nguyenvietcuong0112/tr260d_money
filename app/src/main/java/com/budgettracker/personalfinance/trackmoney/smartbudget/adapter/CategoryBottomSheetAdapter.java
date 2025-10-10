package com.budgettracker.personalfinance.trackmoney.smartbudget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.CategoryItem;

import java.util.List;

public class CategoryBottomSheetAdapter extends RecyclerView.Adapter<CategoryBottomSheetAdapter.ViewHolder> {

    private List<CategoryItem> categories;
    private Context context;
    private OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryItem category, int position);
    }

    public CategoryBottomSheetAdapter(Context context, List<CategoryItem> categories, OnCategorySelectedListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem item = categories.get(position);
        holder.ivIcon.setImageResource(item.getIconResource());
        holder.tvName.setText(item.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
