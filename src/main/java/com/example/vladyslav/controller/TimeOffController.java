package com.example.vladyslav.controller;

import com.example.vladyslav.model.TimeOff;
import com.example.vladyslav.service.TimeOffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/time-offs")
@RequiredArgsConstructor
public class TimeOffController {

    private final TimeOffService service;

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TimeOff> create(@Valid @RequestBody TimeOff timeOff){
        return ResponseEntity.ok(service.create(timeOff));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @GetMapping("/by-id/{id}")
    public ResponseEntity<TimeOff> get(@RequestParam String id){
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String id){
        service.delete(id);
    }





}
