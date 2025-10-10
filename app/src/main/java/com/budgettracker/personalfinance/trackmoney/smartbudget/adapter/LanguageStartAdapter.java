package com.budgettracker.personalfinance.trackmoney.smartbudget.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.ItemLanguageBinding;
import com.budgettracker.personalfinance.trackmoney.smartbudget.model.LanguageHandModel;

import java.util.List;


public class LanguageStartAdapter extends RecyclerView.Adapter<LanguageStartAdapter.LanguageViewHolder> {
    private Context context;
    private List<LanguageHandModel> lists;
    private IClickLanguage iClickLanguage;

    public LanguageStartAdapter(Context context, List<LanguageHandModel> lists, IClickLanguage iClickLanguage) {
        this.context = context;
        this.lists = lists;
        this.iClickLanguage = iClickLanguage;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLanguageBinding binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LanguageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageHandModel data = lists.get(position);
        holder.bind(data, context, position);

        holder.binding.rlItem.setOnClickListener(view -> {
            setSelectLanguage(data.getIsoLanguage());
            iClickLanguage.onClick(data);
            for (LanguageHandModel item : lists) {
                item.setHandVisible(false);
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {

        private ItemLanguageBinding binding;


        public LanguageViewHolder(ItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        public void bind(LanguageHandModel data, Context context, int position) {
            if (data.isHandVisible() && position == 2) {
                binding.animHand.setVisibility(View.VISIBLE);
            } else {
                binding.animHand.setVisibility(View.INVISIBLE);
            }

            binding.ivAvatar.setImageDrawable(context.getDrawable(data.getImage()));
            binding.tvTitle.setText(data.getLanguageName());

            if (data.getCheck()) {
                binding.getRoot().setBackgroundColor(Color.parseColor("#1B1732"));
                binding. v2.setVisibility(View.VISIBLE);
            } else {
                binding.getRoot().setBackgroundColor(Color.parseColor("#1B1732"));
                binding.rlItem.setBackgroundColor(Color.TRANSPARENT);
                binding.v2.setVisibility(View.GONE);
            }
        }
    }

    public void setSelectLanguage(String code) {
        for (LanguageHandModel data : lists) {
            if (data.getIsoLanguage().equals(code)) {
                data.setCheck(true);
            } else {
                data.setCheck(false);
            }
        }
        notifyDataSetChanged();
    }

    public interface IClickLanguage {
        void onClick(LanguageHandModel model);
    }
}
