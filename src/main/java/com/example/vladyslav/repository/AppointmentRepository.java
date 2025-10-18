package com.example.vladyslav.repository;

import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.model.enums.AppointmentStatus;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    ArrayList<Appointment> getAllAppointmentsByDoctorId(String doctorId);

    ArrayList<Appointment> getAllAppointmentsByPatientId(String patientId);

    Page<Appointment> findByDoctorIdAndStartBetween(String doctorId, Instant from, Instant to, Pageable pageable);

    Page<Appointment> findByPatientIdOrderByStartDesc(String patientId, Pageable pageable);

    Page<Appointment> findByClinicIdAndStartBetween(String clinicId, Instant from, Instant to, Pageable pageable);

    Optional<Appointment> findByDoctorIdAndStart(String doctorId, Instant start);

    List<Appointment> findByDoctorIdAndStartLessThanAndEndGreaterThan(String doctorId, Instant endExclusive, Instant startExclusive);

    @Query("{ 'doctorId': ?0, 'status': ?1, 'start': { $gte: ?2 }, 'end': { $lte: ?3 } }")
    Page<Appointment> findByDoctorIdAndStatusBetween(String doctorId, AppointmentStatus status, Instant from, Instant to, Pageable pageable);


}
