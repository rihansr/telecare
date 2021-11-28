package com.telemedicine.telecare.model.other;

import java.io.Serializable;

public class ActivityLevel implements Serializable {

    private String  level;
    private String  details;
    private Double  value;

    public ActivityLevel() {}

    public ActivityLevel(String level, String details, Double value) {
        this.level = level;
        this.details = details;
        this.value = value;
    }


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
