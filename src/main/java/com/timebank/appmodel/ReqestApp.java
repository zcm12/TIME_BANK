package com.timebank.appmodel;


public class ReqestApp {


    private String reqAddress;

    private String reqTitle;

    private String reqDesp;

    private String reqComment;

    private String reqTypeGuidClass;
    private String reqTypeGuidUrgency;

    private String reqAvailableStartTime;
    private String reqAvailableEndTime;

    private String reqRreDurationTime;

    private String reqPersonNum;

    public String getReqAddress() {
        return reqAddress;
    }

    public void setReqAddress(String reqAddress) {
        this.reqAddress = reqAddress;
    }

    public String getReqTitle() {
        return reqTitle;
    }

    public void setReqTitle(String reqTitle) {
        this.reqTitle = reqTitle;
    }

    public String getReqDesp() {
        return reqDesp;
    }

    public void setReqDesp(String reqDesp) {
        this.reqDesp = reqDesp;
    }

    public String getReqComment() {
        return reqComment;
    }

    public void setReqComment(String reqComment) {
        this.reqComment = reqComment;
    }

    public String getReqTypeGuidClass() {
        return reqTypeGuidClass;
    }

    public void setReqTypeGuidClass(String reqTypeGuidClass) {
        this.reqTypeGuidClass = reqTypeGuidClass;
    }

    public String getReqTypeGuidUrgency() {
        return reqTypeGuidUrgency;
    }

    public void setReqTypeGuidUrgency(String reqTypeGuidUrgency) {
        this.reqTypeGuidUrgency = reqTypeGuidUrgency;
    }

    public String getReqAvailableStartTime() {
        return reqAvailableStartTime;
    }

    public void setReqAvailableStartTime(String reqAvailableStartTime) {
        this.reqAvailableStartTime = reqAvailableStartTime;
    }

    public String getReqAvailableEndTime() {
        return reqAvailableEndTime;
    }

    public void setReqAvailableEndTime(String reqAvailableEndTime) {
        this.reqAvailableEndTime = reqAvailableEndTime;
    }

    public String getReqRreDurationTime() {
        return reqRreDurationTime;
    }

    public void setReqRreDurationTime(String reqRreDurationTime) {
        this.reqRreDurationTime = reqRreDurationTime;
    }

    public String getReqPersonNum() {
        return reqPersonNum;
    }

    public void setReqPersonNum(String reqPersonNum) {
        this.reqPersonNum = reqPersonNum;
    }

    @Override
    public String toString() {
        return "ReqestApp{" +
                "reqAddress='" + reqAddress + '\'' +
                ", reqTitle='" + reqTitle + '\'' +
                ", reqDesp='" + reqDesp + '\'' +
                ", reqComment='" + reqComment + '\'' +
                ", reqTypeGuidClass='" + reqTypeGuidClass + '\'' +
                ", reqTypeGuidUrgency='" + reqTypeGuidUrgency + '\'' +
                ", reqAvailableStartTime='" + reqAvailableStartTime + '\'' +
                ", reqAvailableEndTime='" + reqAvailableEndTime + '\'' +
                ", reqRreDurationTime=" + reqRreDurationTime +
                ", reqPersonNum=" + reqPersonNum +
                '}';
    }
}