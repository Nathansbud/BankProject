package com.nathansbud;

public class Transaction {
    private String timestamp; //Unix time
    private double amount;
    private String from;
    private String to;

    public Transaction(String _from, String _to, double _amount, String _timestamp) {
        amount = _amount;
        timestamp = _timestamp;
        from = _from;
        to = _to;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double _amount) {
        amount = _amount;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String _timestamp) {
        timestamp = _timestamp;
    }

    public String getFrom() {
        return from;
    }
    public void setFrom(String _from) {
        from = _from;
    }

    public String getTo() {
        return to;
    }
    public void setTo(String _to) {
        to = _to;
    }
}
