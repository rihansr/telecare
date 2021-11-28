package com.telemedicine.telecare.model.appointment;

import java.io.Serializable;
import java.util.List;

public class Timing implements Serializable {

    private List<String> weekdays;
    private String       startTime;
    private String       endTime;
    private Integer      consultationTime;

    public List<String> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(List<String> weekdays) {
        this.weekdays = weekdays;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getConsultationTime() {
        return consultationTime;
    }

    public void setConsultationTime(Integer consultationTime) {
        this.consultationTime = consultationTime;
    }
}
