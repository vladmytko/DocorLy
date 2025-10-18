package com.example.vladyslav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("time_off")
@CompoundIndex(name = "idx_doctor_timeOff", def = "{ 'doctorId': 1, 'start': 1, 'end': 1}")
public class TimeOff {

    @Id
    private String id;

    @Indexed
    private String doctorId;

    /**
     * Absolute UTC range when the doctor is unavailable.
     * If the doctor take a full day off, set start at 00:00 and end at 23:59 of that date.
     */

    private Instant start;
    private Instant end;

    private String reason;  // e.g. Annual Leave, Sick, Conference...
}
