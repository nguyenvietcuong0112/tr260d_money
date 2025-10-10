package com.budgettracker.personalfinance.trackmoney.smartbudget.model;

public class MonthlyStatisticsModel {
    private String month;
    private Double expend;
    private Double income;
    private Double loan;
    private Double borrow;
    private Double balance;

    public MonthlyStatisticsModel() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Double getExpend() {
        return expend;
    }

    public void setExpend(Double expend) {
        this.expend = expend;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }

    public Double getLoan() {
        return loan;
    }

    public void setLoan(Double loan) {
        this.loan = loan;
    }

    public Double getBorrow() {
        return borrow;
    }

    public void setBorrow(Double borrow) {
        this.borrow = borrow;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}