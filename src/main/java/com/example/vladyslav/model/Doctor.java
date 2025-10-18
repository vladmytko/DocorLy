package com.example.vladyslav.model;

import com.example.vladyslav.model.enums.LanguageCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "doctors")
public class Doctor {
    @Id
    private String id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @DBRef
    private Speciality speciality;

    @Pattern(regexp = "^[0-9]{11}$", message = "Invalid phone number format, example '07529201824'")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private String bio;

    @NotBlank(message = "Photo url is required")
    private String imageUrl;

    @Indexed
    @Builder.Default
    private List<LanguageCode> languages = new ArrayList<>();

    @DBRef(lazy = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    private Float averageRating;

    @Min(0)
    @Builder.Default
    private int consultationFee = 0;

    @DBRef
    private User user;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef
    private Clinic clinic; // optional - can be null if independent;


    public String getFullName() {
        return firstName + " " + lastName;
    }

    @JsonIgnore
    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
