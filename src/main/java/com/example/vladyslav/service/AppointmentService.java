package com.example.vladyslav.service;

import com.example.vladyslav.dto.AppointmentDTO;
import com.example.vladyslav.dto.AppointmentRequestDTO;
import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.repository.AppointmentRepository;
import com.example.vladyslav.repository.DoctorRepository;
import com.example.vladyslav.repository.PatientRepository;
import com.example.vladyslav.repository.UserRepository;
import com.example.vladyslav.requests.BookAppointmentRequest;
import com.example.vladyslav.requests.ConfirmAppointmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    public List<Appointment> getAllAppointments(){
        return appointmentRepository.findAll();
    }

    public AppointmentDTO getAppointmentById(String appointmentId){
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow(()-> new RuntimeException("Appointment Not Found!"));
        return AppointmentDTO.builder()
                .id(appt.getId())
                .concern(appt.getConcern())
                .date(appt.getAppointmentDate())
                .time(appt.getAppointmentTime())
                .status(appt.getStatus())
                .createdAt(appt.getCreatedAt())
                .updatedAt(appt.getUpdatedAt())
                .doctorId(appt.getDoctor().getId())
                .patientId(appt.getPatient().getId())
                .build();
    }

    public AppointmentRequestDTO bookAppointment(BookAppointmentRequest request){
        if (request.getDateOfBirth() == null || request.getGender() == null || request.getConcern() == null || request.getPatientId() == null || request.getDoctorId() == null) {
            throw new IllegalArgumentException("Date of birth, gender, concern, doctor ID and patient ID are required");
        }

        // Create & save
        Appointment appt = new Appointment();
        appt.setDateOfBirth(request.getDateOfBirth());
        appt.setGender(request.getGender());
        appt.setConcern(request.getConcern());
        appt.setPatient(patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Invalid patient")) );
        appt.setDoctor(doctorRepository.findById(request.getDoctorId()).orElseThrow(()-> new RuntimeException("Invalid doctor")));
        appt.setStatus(Appointment.AppointmentStatus.REQUESTED);

        appointmentRepository.save(appt);

        return AppointmentRequestDTO.builder()
                .dateOfBirth(appt.getDateOfBirth())
                .gender(appt.getGender())
                .concern(appt.getConcern())
                .status(appt.getStatus())
                .doctorId(request.getDoctorId())
                .patientId(request.getPatientId())
                .build();
    }

    public AppointmentDTO cancelAppointment(String appointmentId){
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment Not Found"));
        appt.setStatus(Appointment.AppointmentStatus.CANCELLED);

        appointmentRepository.save(appt);

        return AppointmentDTO.builder()
                .id(appt.getId())
                .concern(appt.getConcern())
                .date(appt.getAppointmentDate())
                .time(appt.getAppointmentTime())
                .status(appt.getStatus())
                .createdAt(appt.getCreatedAt())
                .updatedAt(appt.getUpdatedAt())
                .doctorId(appt.getDoctor().getId())
                .patientId(appt.getPatient().getId())
                .build();
    }

    public AppointmentDTO confirmAppointment(ConfirmAppointmentRequest request){
        Appointment appt = appointmentRepository.findById(request.getAppointmentId()).orElseThrow(() -> new RuntimeException("Appointment Not Found"));
        appt.setAppointmentDate(request.getDate());
        appt.setAppointmentTime(request.getTime());
        appt.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        appointmentRepository.save(appt);

        return AppointmentDTO.builder()
                .id(appt.getId())
                .concern(appt.getConcern())
                .date(appt.getAppointmentDate())
                .time(appt.getAppointmentTime())
                .status(appt.getStatus())
                .createdAt(appt.getCreatedAt())
                .updatedAt(appt.getUpdatedAt())
                .doctorId(appt.getDoctor().getId())
                .patientId(appt.getPatient().getId())
                .build();
    }

    public AppointmentDTO completedAppointment(String appointmentId){
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow(() -> new RuntimeException("Appointment Not Found"));
        appt.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appt);

        return AppointmentDTO.builder()
                .id(appt.getId())
                .concern(appt.getConcern())
                .date(appt.getAppointmentDate())
                .time(appt.getAppointmentTime())
                .status(appt.getStatus())
                .createdAt(appt.getCreatedAt())
                .updatedAt(appt.getUpdatedAt())
                .doctorId(appt.getDoctor().getId())
                .patientId(appt.getPatient().getId())
                .build();
    }

    public  List<AppointmentDTO> getAppointmentsByPatientId(String patientId){

        List<Appointment> appointmentList = appointmentRepository.getAllAppointmentsByPatientId(patientId);

        return appointmentList.stream().map(appt ->
                        AppointmentDTO.builder()
                                .id(appt.getId())
                                .concern(appt.getConcern())
                                .date(appt.getAppointmentDate())
                                .time(appt.getAppointmentTime())
                                .status(appt.getStatus())
                                .createdAt(appt.getCreatedAt())
                                .updatedAt(appt.getUpdatedAt())
                                .doctorId(appt.getDoctor().getId())
                                .patientId(appt.getPatient().getId())
                                .build()
                ).collect(Collectors.toList());

    }

    public List<AppointmentDTO> getAppointmentsByDoctorId(String doctorId){
        List<Appointment> appointmentList = appointmentRepository.getAllAppointmentsByDoctorId(doctorId);

        return appointmentList.stream().map(appt ->
                AppointmentDTO.builder()
                        .id(appt.getId())
                        .concern(appt.getConcern())
                        .date(appt.getAppointmentDate())
                        .time(appt.getAppointmentTime())
                        .status(appt.getStatus())
                        .createdAt(appt.getCreatedAt())
                        .updatedAt(appt.getUpdatedAt())
                        .doctorId(appt.getDoctor().getId())
                        .patientId(appt.getPatient().getId())
                        .build()
        ).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByStatus(Appointment.AppointmentStatus status){
        List<Appointment> appointmentList = appointmentRepository.getAllAppointmentByStatus(status);

        return appointmentList.stream().map(appt ->
                AppointmentDTO.builder()
                        .id(appt.getId())
                        .concern(appt.getConcern())
                        .date(appt.getAppointmentDate())
                        .time(appt.getAppointmentTime())
                        .status(appt.getStatus())
                        .createdAt(appt.getCreatedAt())
                        .updatedAt(appt.getUpdatedAt())
                        .doctorId(appt.getDoctor().getId())
                        .patientId(appt.getPatient().getId())
                        .build()
        ).collect(Collectors.toList());
    }

}
