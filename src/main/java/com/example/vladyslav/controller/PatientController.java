package com.example.vladyslav.controller;

import com.example.vladyslav.dto.PatientDTO;
import com.example.vladyslav.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {


    private final PatientService patientService;

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<PatientDTO> getPatientByUserId(@PathVariable String userId){
        PatientDTO dto = patientService.getPatientByUserId(userId); // throws NotFoundException
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<PatientDTO> getPatientByPatientId(@PathVariable String patientId){
        PatientDTO dto = patientService.getPatientByPatientId(patientId); // throws NotFoundException
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PatientDTO>> getAllPatients(){
        List<PatientDTO> dto = patientService.getAllPatients();
        return ResponseEntity.ok(dto);
    }
}
