package com.timebank.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransferExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public TransferExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andTransGuidIsNull() {
            addCriterion("TRANS_GUID is null");
            return (Criteria) this;
        }

        public Criteria andTransGuidIsNotNull() {
            addCriterion("TRANS_GUID is not null");
            return (Criteria) this;
        }

        public Criteria andTransGuidEqualTo(String value) {
            addCriterion("TRANS_GUID =", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidNotEqualTo(String value) {
            addCriterion("TRANS_GUID <>", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidGreaterThan(String value) {
            addCriterion("TRANS_GUID >", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidGreaterThanOrEqualTo(String value) {
            addCriterion("TRANS_GUID >=", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidLessThan(String value) {
            addCriterion("TRANS_GUID <", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidLessThanOrEqualTo(String value) {
            addCriterion("TRANS_GUID <=", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidLike(String value) {
            addCriterion("TRANS_GUID like", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidNotLike(String value) {
            addCriterion("TRANS_GUID not like", value, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidIn(List<String> values) {
            addCriterion("TRANS_GUID in", values, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidNotIn(List<String> values) {
            addCriterion("TRANS_GUID not in", values, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidBetween(String value1, String value2) {
            addCriterion("TRANS_GUID between", value1, value2, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransGuidNotBetween(String value1, String value2) {
            addCriterion("TRANS_GUID not between", value1, value2, "transGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidIsNull() {
            addCriterion("TRANS_FROM_USER_GUID is null");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidIsNotNull() {
            addCriterion("TRANS_FROM_USER_GUID is not null");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidEqualTo(String value) {
            addCriterion("TRANS_FROM_USER_GUID =", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidNotEqualTo(String value) {
            addCriterion("TRANS_FROM_USER_GUID <>", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidGreaterThan(String value) {
            addCriterion("TRANS_FROM_USER_GUID >", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidGreaterThanOrEqualTo(String value) {
            addCriterion("TRANS_FROM_USER_GUID >=", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidLessThan(String value) {
            addCriterion("TRANS_FROM_USER_GUID <", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidLessThanOrEqualTo(String value) {
            addCriterion("TRANS_FROM_USER_GUID <=", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidLike(String value) {
            addCriterion("TRANS_FROM_USER_GUID like", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidNotLike(String value) {
            addCriterion("TRANS_FROM_USER_GUID not like", value, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidIn(List<String> values) {
            addCriterion("TRANS_FROM_USER_GUID in", values, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidNotIn(List<String> values) {
            addCriterion("TRANS_FROM_USER_GUID not in", values, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidBetween(String value1, String value2) {
            addCriterion("TRANS_FROM_USER_GUID between", value1, value2, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransFromUserGuidNotBetween(String value1, String value2) {
            addCriterion("TRANS_FROM_USER_GUID not between", value1, value2, "transFromUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidIsNull() {
            addCriterion("TRANS_TO_USER_GUID is null");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidIsNotNull() {
            addCriterion("TRANS_TO_USER_GUID is not null");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidEqualTo(String value) {
            addCriterion("TRANS_TO_USER_GUID =", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidNotEqualTo(String value) {
            addCriterion("TRANS_TO_USER_GUID <>", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidGreaterThan(String value) {
            addCriterion("TRANS_TO_USER_GUID >", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidGreaterThanOrEqualTo(String value) {
            addCriterion("TRANS_TO_USER_GUID >=", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidLessThan(String value) {
            addCriterion("TRANS_TO_USER_GUID <", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidLessThanOrEqualTo(String value) {
            addCriterion("TRANS_TO_USER_GUID <=", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidLike(String value) {
            addCriterion("TRANS_TO_USER_GUID like", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidNotLike(String value) {
            addCriterion("TRANS_TO_USER_GUID not like", value, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidIn(List<String> values) {
            addCriterion("TRANS_TO_USER_GUID in", values, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidNotIn(List<String> values) {
            addCriterion("TRANS_TO_USER_GUID not in", values, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidBetween(String value1, String value2) {
            addCriterion("TRANS_TO_USER_GUID between", value1, value2, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransToUserGuidNotBetween(String value1, String value2) {
            addCriterion("TRANS_TO_USER_GUID not between", value1, value2, "transToUserGuid");
            return (Criteria) this;
        }

        public Criteria andTransDespIsNull() {
            addCriterion("TRANS_DESP is null");
            return (Criteria) this;
        }

        public Criteria andTransDespIsNotNull() {
            addCriterion("TRANS_DESP is not null");
            return (Criteria) this;
        }

        public Criteria andTransDespEqualTo(String value) {
            addCriterion("TRANS_DESP =", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespNotEqualTo(String value) {
            addCriterion("TRANS_DESP <>", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespGreaterThan(String value) {
            addCriterion("TRANS_DESP >", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespGreaterThanOrEqualTo(String value) {
            addCriterion("TRANS_DESP >=", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespLessThan(String value) {
            addCriterion("TRANS_DESP <", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespLessThanOrEqualTo(String value) {
            addCriterion("TRANS_DESP <=", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespLike(String value) {
            addCriterion("TRANS_DESP like", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespNotLike(String value) {
            addCriterion("TRANS_DESP not like", value, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespIn(List<String> values) {
            addCriterion("TRANS_DESP in", values, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespNotIn(List<String> values) {
            addCriterion("TRANS_DESP not in", values, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespBetween(String value1, String value2) {
            addCriterion("TRANS_DESP between", value1, value2, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransDespNotBetween(String value1, String value2) {
            addCriterion("TRANS_DESP not between", value1, value2, "transDesp");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeIsNull() {
            addCriterion("TRANS_ISSUE_TIME is null");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeIsNotNull() {
            addCriterion("TRANS_ISSUE_TIME is not null");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeEqualTo(Date value) {
            addCriterion("TRANS_ISSUE_TIME =", value, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeNotEqualTo(Date value) {
            addCriterion("TRANS_ISSUE_TIME <>", value, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeGreaterThan(Date value) {
            addCriterion("TRANS_ISSUE_TIME >", value, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("TRANS_ISSUE_TIME >=", value, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeLessThan(Date value) {
            addCriterion("TRANS_ISSUE_TIME <", value, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeLessThanOrEqualTo(Date value) {
            addCriterion("TRANS_ISSUE_TIME <=", value, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeIn(List<Date> values) {
            addCriterion("TRANS_ISSUE_TIME in", values, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeNotIn(List<Date> values) {
            addCriterion("TRANS_ISSUE_TIME not in", values, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeBetween(Date value1, Date value2) {
            addCriterion("TRANS_ISSUE_TIME between", value1, value2, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransIssueTimeNotBetween(Date value1, Date value2) {
            addCriterion("TRANS_ISSUE_TIME not between", value1, value2, "transIssueTime");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusIsNull() {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS is null");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusIsNotNull() {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS is not null");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusEqualTo(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS =", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusNotEqualTo(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS <>", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusGreaterThan(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS >", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusGreaterThanOrEqualTo(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS >=", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusLessThan(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS <", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusLessThanOrEqualTo(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS <=", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusLike(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS like", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusNotLike(String value) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS not like", value, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusIn(List<String> values) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS in", values, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusNotIn(List<String> values) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS not in", values, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusBetween(String value1, String value2) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS between", value1, value2, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransTypeGuidProcessStatusNotBetween(String value1, String value2) {
            addCriterion("TRANS_TYPE_GUID_PROCESS_STATUS not between", value1, value2, "transTypeGuidProcessStatus");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeIsNull() {
            addCriterion("TRANS_PROCESS_TIME is null");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeIsNotNull() {
            addCriterion("TRANS_PROCESS_TIME is not null");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeEqualTo(Date value) {
            addCriterion("TRANS_PROCESS_TIME =", value, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeNotEqualTo(Date value) {
            addCriterion("TRANS_PROCESS_TIME <>", value, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeGreaterThan(Date value) {
            addCriterion("TRANS_PROCESS_TIME >", value, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("TRANS_PROCESS_TIME >=", value, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeLessThan(Date value) {
            addCriterion("TRANS_PROCESS_TIME <", value, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeLessThanOrEqualTo(Date value) {
            addCriterion("TRANS_PROCESS_TIME <=", value, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeIn(List<Date> values) {
            addCriterion("TRANS_PROCESS_TIME in", values, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeNotIn(List<Date> values) {
            addCriterion("TRANS_PROCESS_TIME not in", values, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeBetween(Date value1, Date value2) {
            addCriterion("TRANS_PROCESS_TIME between", value1, value2, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessTimeNotBetween(Date value1, Date value2) {
            addCriterion("TRANS_PROCESS_TIME not between", value1, value2, "transProcessTime");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespIsNull() {
            addCriterion("TRANS_PROCESS_DESP is null");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespIsNotNull() {
            addCriterion("TRANS_PROCESS_DESP is not null");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespEqualTo(String value) {
            addCriterion("TRANS_PROCESS_DESP =", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespNotEqualTo(String value) {
            addCriterion("TRANS_PROCESS_DESP <>", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespGreaterThan(String value) {
            addCriterion("TRANS_PROCESS_DESP >", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespGreaterThanOrEqualTo(String value) {
            addCriterion("TRANS_PROCESS_DESP >=", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespLessThan(String value) {
            addCriterion("TRANS_PROCESS_DESP <", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespLessThanOrEqualTo(String value) {
            addCriterion("TRANS_PROCESS_DESP <=", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespLike(String value) {
            addCriterion("TRANS_PROCESS_DESP like", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespNotLike(String value) {
            addCriterion("TRANS_PROCESS_DESP not like", value, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespIn(List<String> values) {
            addCriterion("TRANS_PROCESS_DESP in", values, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespNotIn(List<String> values) {
            addCriterion("TRANS_PROCESS_DESP not in", values, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespBetween(String value1, String value2) {
            addCriterion("TRANS_PROCESS_DESP between", value1, value2, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransProcessDespNotBetween(String value1, String value2) {
            addCriterion("TRANS_PROCESS_DESP not between", value1, value2, "transProcessDesp");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyIsNull() {
            addCriterion("TRANS_CURRENCY is null");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyIsNotNull() {
            addCriterion("TRANS_CURRENCY is not null");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyEqualTo(Double value) {
            addCriterion("TRANS_CURRENCY =", value, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyNotEqualTo(Double value) {
            addCriterion("TRANS_CURRENCY <>", value, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyGreaterThan(Double value) {
            addCriterion("TRANS_CURRENCY >", value, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyGreaterThanOrEqualTo(Double value) {
            addCriterion("TRANS_CURRENCY >=", value, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyLessThan(Double value) {
            addCriterion("TRANS_CURRENCY <", value, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyLessThanOrEqualTo(Double value) {
            addCriterion("TRANS_CURRENCY <=", value, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyIn(List<Double> values) {
            addCriterion("TRANS_CURRENCY in", values, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyNotIn(List<Double> values) {
            addCriterion("TRANS_CURRENCY not in", values, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyBetween(Double value1, Double value2) {
            addCriterion("TRANS_CURRENCY between", value1, value2, "transCurrency");
            return (Criteria) this;
        }

        public Criteria andTransCurrencyNotBetween(Double value1, Double value2) {
            addCriterion("TRANS_CURRENCY not between", value1, value2, "transCurrency");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table dbo.TRANSFER
     *
     * @mbg.generated
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}