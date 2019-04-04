package org.edx.mobile.tta.data.local.db.table;

public class Points {
    private String condition;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getConditionPoints() {
        return conditionPoints;
    }

    public void setConditionPoints(String conditionPoints) {
        this.conditionPoints = conditionPoints;
    }

    private String conditionPoints;
}
