package com.example.vladyslav.requests;

import com.example.vladyslav.model.Appointment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookAppointmentRequest {

    private LocalDate dateOfBirth;
    private Appointment.Gender gender;
    private String concern;
    private String patientId;
    private String doctorId;
}
