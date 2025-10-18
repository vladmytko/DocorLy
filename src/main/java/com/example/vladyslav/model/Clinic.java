package com.example.vladyslav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "clinics")
public class Clinic {

    @Id
    private String id;

    private String name;

    private String address;

    private String phoneNumber;

    private String bio;

    @DBRef(lazy = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    private Float averageRating;

    @DBRef(lazy = true)
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();
}
