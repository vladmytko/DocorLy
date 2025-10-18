package com.example.vladyslav.controller;

import com.example.vladyslav.dto.DoctorDTO;
import com.example.vladyslav.model.Doctor;
import com.example.vladyslav.requests.DoctorRegisterRequest;
import com.example.vladyslav.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public List<DoctorDTO> getAllDoctors(){
        return doctorService.getAllDoctors();
    }

    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody DoctorRegisterRequest request){
        return new ResponseEntity<>(doctorService.createDoctor(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable String id){
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return doctor != null ? ResponseEntity.ok(doctor) : ResponseEntity.notFound().build();
    }

    @GetMapping("/speciality")
    public Page<DoctorDTO> findDoctorsBySpeciality(
            @RequestParam(required = false) String specialityId,
            @RequestParam(required = false) String specialityTitle,
            @PageableDefault(size = 20) Pageable pageable
            )   {
        if(specialityId != null) return doctorService.findDoctorsBySpecialityId(specialityId, pageable);
        if(specialityTitle != null && !specialityTitle.isBlank()) return doctorService.findBySpecialityTitle(specialityTitle, pageable);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide specialityId or speciality title");
    }
}
