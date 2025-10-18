package com.example.vladyslav.requests;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ConfirmAppointmentRequest {
    private String appointmentId;
    private LocalDate date;
    private LocalTime time;
}
