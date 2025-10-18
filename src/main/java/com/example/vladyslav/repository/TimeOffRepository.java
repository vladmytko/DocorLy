package com.example.vladyslav.repository;

import com.example.vladyslav.model.TimeOff;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface TimeOffRepository extends MongoRepository<TimeOff, String > {

    List<TimeOff> findByDoctorIdAndStartLessThanEqualAndEndGreaterThanEqual(String doctorId, Instant end, Instant start);

    List<TimeOff> findByDoctorIdAndStartBetween(String doctorId, Instant from, Instant to);
}
