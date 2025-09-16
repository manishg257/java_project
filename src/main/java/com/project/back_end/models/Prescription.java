package com.project.back_end.models;

public class Prescription {
    private String id;
    private Long appointmentId;
    private String notes;

    public Prescription() {}
    public Prescription(Long appointmentId, String notes) {
        this.appointmentId = appointmentId;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
