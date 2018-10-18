package com.timebank.controller.sxq;
import java.util.List;

/**
 * Created by ciee on 2017/11/9.
 */
public class TableRecordsJson {

    private List rows;
    private int total;

//    public void setNumber(int number) {
//        this.number = number;
//    }
//
//    public int getNumber() {
//        return number;
//    }
//
//    private int number;

    public TableRecordsJson(List rows, int total) {
        this.rows = rows;
        this.total = total;
    }
//    public TableRecordsJson(List rows, int total,int number) {
//        this.rows = rows;
//        this.total = total;
//        this.number=number;
//    }
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
