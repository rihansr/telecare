package com.telemedicine.telecare.util.enums;

public enum AppointmentStatus {

    NOT_START(0), START(1), RUNNING(2), END(3), CANCEL(4);

    private int action;

    AppointmentStatus(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
