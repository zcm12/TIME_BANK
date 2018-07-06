package com.timebank.appmodel;

/**
 * Created by chase on 2018/5/16.
 */

public class TransferApp {
    private String transToUserGuid;

    private String transCurrency;

    private String transDesp;

    public String getTransToUserGuid() {
        return transToUserGuid;
    }

    public void setTransToUserGuid(String transToUserGuid) {
        this.transToUserGuid = transToUserGuid;
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
                "transToUserGuid='" + transToUserGuid + '\'' +
                ", transCurrency='" + transCurrency + '\'' +
                ", transDesp='" + transDesp + '\'' +
                '}';
    }
}
