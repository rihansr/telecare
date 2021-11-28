package com.telemedicine.telecare.model.other;

import java.io.Serializable;

public class TimeLimit implements Serializable {

    private Integer min;
    private Integer max;

    public TimeLimit() {}

    public TimeLimit(long min, long max) {
        this.min = (int) min;
        this.max = (int) max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }
}
