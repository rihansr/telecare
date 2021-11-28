package com.telemedicine.telecare.model.other;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class DailyData implements Serializable {

    @ServerTimestamp
    private Date date;
    private Integer value;

    public DailyData() {}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
