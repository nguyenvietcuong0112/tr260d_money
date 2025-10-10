package com.budgettracker.personalfinance.trackmoney.smartbudget.notiSpecial;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.budgettracker.personalfinance.trackmoney.smartbudget.R;
import com.budgettracker.personalfinance.trackmoney.smartbudget.databinding.FragmentPerNotiBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class BottomSheetPerNotiFragment extends BottomSheetDialogFragment {

    private FragmentPerNotiBinding binding;
    private OnSetClickListener onSetClickListener;

    public interface OnSetClickListener {
        void onSetClick();
        void onDontSetClick();
    }

    public void setOnSetClickListener(OnSetClickListener listener) {
        this.onSetClickListener = listener;
    }

    public BottomSheetPerNotiFragment() {
        // Required empty constructor
    }

    public static BottomSheetPerNotiFragment newInstance() {
        return new BottomSheetPerNotiFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPerNotiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBottomSheet();
        setupViews();
    }

    private void setupBottomSheet() {
        if (getDialog() != null) {
            getDialog().setOnShowListener(dialog -> {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;

                // Bắt sự kiện click ra ngoài
                View outside = bottomSheetDialog.findViewById(com.google.android.material.R.id.touch_outside);
                if (outside != null) {
                    outside.setOnClickListener(v -> {
                        if (onSetClickListener != null) {
                            onSetClickListener.onDontSetClick();
                        }
                        dismiss();
                    });
                }

                View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setDraggable(false);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    bottomSheet.setBackground(
                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_alway)
                    );
                }

            });
        }
    }
    private void setupViews() {
        if (binding != null) {
            binding.notNow.setOnClickListener(v -> {
                if (onSetClickListener != null) {
                    onSetClickListener.onDontSetClick();
                }
                dismiss();
            });

            binding.set.setOnClickListener(v -> {
                if (onSetClickListener != null) {
                    onSetClickListener.onSetClick();
                }
                dismiss();
            });
            binding.later.setOnClickListener(v->{
                if (onSetClickListener != null) {
                    onSetClickListener.onDontSetClick();
                }
                dismiss();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Khi nhấn back => gọi callback
        dialog.setOnCancelListener(d -> {
            if (onSetClickListener != null) {
                onSetClickListener.onDontSetClick();
            }
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            // Cho phép click ra ngoài để cancel
            getDialog().setCanceledOnTouchOutside(true);
        }
    }
}

