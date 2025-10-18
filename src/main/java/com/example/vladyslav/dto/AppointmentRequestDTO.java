package com.example.vladyslav.dto;

import com.example.vladyslav.model.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequestDTO {

    private LocalDate dateOfBirth;
    private Appointment.Gender gender;
    private String concern;
    private Appointment.AppointmentStatus status;
    private String patientId;
    private String doctorId;

}
