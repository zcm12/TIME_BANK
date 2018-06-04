package com.timebank.appmodel;

/**
 * Created by chase on 2018/4/18.
 */

public class ResultModel {

    /**
     * code : 0
     * msg : 密码不正确
     */

    private int code;
    private String msg;

    public ResultModel(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResultModel{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
