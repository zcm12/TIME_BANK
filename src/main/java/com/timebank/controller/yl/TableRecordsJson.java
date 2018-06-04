package com.timebank.controller.yl;


import java.util.List;

/**
 * Created by ciee on 2017/11/9.
 */
public class TableRecordsJson {

    private List rows;
    private int total;

    public TableRecordsJson(List rows, int total) {
        this.rows = rows;
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
