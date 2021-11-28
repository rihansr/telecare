package com.telemedicine.telecare.model.appointment;

import com.telemedicine.telecare.util.enums.AppointmentStatus;

import java.io.Serializable;
import java.sql.Timestamp;

public class Appointment implements Comparable, Serializable {

    /**
     * Keys
     **/
    public static String BY = "appointedBy";
    public static String TO = "appointedTo";
    public static String START_AT = "startAt";
    public static String END_AT = "endAt";
    public static String STATUS = "status";

    private String      id;
    private String      appointedBy;
    private String      appointedTo;
    private boolean     isConsultation;
    private boolean     isReport;
    private boolean     isPatient;
    private String      patientName;
    private Integer     patientAge;
    private String      patientGender;
    private String      notes;
    private String      status;
    private Timestamp   startAt;
    private Timestamp   endAt;
    private int         consultationTime;
    private String      chamber;

    public Appointment() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppointedBy() {
        return appointedBy;
    }

    public void setAppointedBy(String appointedBy) {
        this.appointedBy = appointedBy;
    }

    public String getAppointedTo() {
        return appointedTo;
    }

    public void setAppointedTo(String appointedTo) {
        this.appointedTo = appointedTo;
    }

    public boolean isConsultation() {
        return isConsultation;
    }

    public void setConsultation(boolean consultation) {
        isConsultation = consultation;
    }

    public boolean isReport() {
        return isReport;
    }

    public void setReport(boolean report) {
        isReport = report;
    }

    public boolean isPatient() {
        return isPatient;
    }

    public void setPatient(boolean patient) {
        isPatient = patient;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppointmentStatus getStatus() {
        return status == null ? AppointmentStatus.NOT_START : AppointmentStatus.valueOf(status);
    }

    public void setStatus(AppointmentStatus status) {
        this.status = String.valueOf(status);
    }

    public long getStartAt() {
        return startAt == null ? 0 : startAt.getTime();
    }

    public void setStartAt(Long startAt) {
        this.startAt = startAt == null ? null : new Timestamp(startAt);
    }

    public long getEndAt() {
        return endAt == null ? 0 : endAt.getTime();
    }

    public void setEndAt(Long endAt) {
        this.endAt = endAt == null ? null : new Timestamp(endAt);
    }

    public int getConsultationTime() {
        return consultationTime;
    }

    public void setConsultationTime(int consultationTime) {
        this.consultationTime = consultationTime;
    }

    public String getChamber() {
        return chamber;
    }

    public void setChamber(String chamber) {
        this.chamber = chamber;
    }

    @Override
    public int compareTo(Object obj) {
        long past =((Appointment) obj).getStartAt();
        return (int) (this.startAt.getTime() - past);
    }
}
