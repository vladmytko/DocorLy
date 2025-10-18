package com.example.vladyslav.repository;

import com.example.vladyslav.model.Speciality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialityRepository extends MongoRepository<Speciality, String> {

    Optional<Speciality> findByTitle(String title);

    // Efficient type-head (uses index on 'title')
    List<Speciality> findByTitleStartsWithIgnoreCase(String prefix, Pageable pageable);
}
