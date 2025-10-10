package com.budgettracker.personalfinance.trackmoney.smartbudget.utils;

import com.budgettracker.personalfinance.trackmoney.smartbudget.model.TransactionModel;

import java.util.List;

public class TransactionUpdateEvent {


    private List<TransactionModel> transactionList;

    public TransactionUpdateEvent(List<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    public List<TransactionModel> getTransactionList() {
        return transactionList;
    }
}