package com.example.vladyslav.service;

import com.example.vladyslav.dto.PatientDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.model.Patient;
import com.example.vladyslav.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;


    public PatientDTO getPatientByPatientId(String patientId){

        Patient patient = patientRepository.findByUserId(patientId)
                .orElseThrow(()-> new NotFoundException("Patient not found for patientId: " + patientId));

        return toDto(patient);
    }



    public PatientDTO getPatientByUserId(String userId){

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException("Patient not found for userId: " + userId));

        return toDto(patient);
    }



    public List<PatientDTO> getAllPatients(){

        List<Patient> patients = patientRepository.findAll();

        return patients.stream()
                .map(this::toDto)
                .toList();
    }

    
    private PatientDTO toDto(Patient patient){
        return PatientDTO.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .dateOfBirth(patient.getDateOfBirth())
                .userId(patient.getUser().getId())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .photoUrl(patient.getImageUrl())
                .role(patient.getUser().getRole())
                .build();
    }
}
