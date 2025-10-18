package com.example.vladyslav.controller;

import com.example.vladyslav.dto.AppointmentDTO;
import com.example.vladyslav.dto.AppointmentRequestDTO;
import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.requests.BookAppointmentRequest;
import com.example.vladyslav.requests.ConfirmAppointmentRequest;
import com.example.vladyslav.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(){
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/id/{appointmentId}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable String appointmentId){
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable String patientId){
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable String doctorId){
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
    }

    @PostMapping
    public ResponseEntity<AppointmentRequestDTO> bookAppointment(@RequestBody BookAppointmentRequest request) {
            return new ResponseEntity<>(appointmentService.bookAppointment(request), HttpStatus.CREATED);
    }

    @PostMapping("/cancel/{appointmentId}")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable String appointmentId){
        return new ResponseEntity<>(appointmentService.cancelAppointment(appointmentId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/confirm")
    public ResponseEntity<AppointmentDTO> confirmAppointment(@RequestBody ConfirmAppointmentRequest request){
        return new ResponseEntity<>(appointmentService.confirmAppointment(request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/completed/{appointmentId}")
    public ResponseEntity<AppointmentDTO> completedAppointment(@PathVariable String appointmentId){
        return new ResponseEntity<>(appointmentService.completedAppointment(appointmentId), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable Appointment.AppointmentStatus status){
        return new ResponseEntity<>(appointmentService.getAppointmentsByStatus(status), HttpStatus.OK);
    }
}
