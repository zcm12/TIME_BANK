package com.timebank.appmodel;

/**
 * Created by chase on 2018/5/16.
 */

public class TransferApp {
    private String transToUserAccount;

    private String transCurrency;

    private String transDesp;

    public String getTransToUserAccount() {
        return transToUserAccount;
    }

    public void setTransToUserAccount(String transToUserAccount) {
        this.transToUserAccount = transToUserAccount;
    }

    public String getTransCurrency() {
        return transCurrency;
    }

    public void setTransCurrency(String transCurrency) {
        this.transCurrency = transCurrency;
    }

    public String getTransDesp() {
        return transDesp;
    }

    public void setTransDesp(String transDesp) {
        this.transDesp = transDesp;
    }

    @Override
    public String toString() {
        return "TransferApp{" +
                "transToUserAccount='" + transToUserAccount + '\'' +
                ", transCurrency='" + transCurrency + '\'' +
                ", transDesp='" + transDesp + '\'' +
                '}';
    }
}
