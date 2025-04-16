package ua.mykola.photoservice.rest.dto;

import jakarta.validation.constraints.NotNull;
import commons.dto.QuestCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record UploadPhotoRequest(
        @NotNull(message = "Quest code is required")
        QuestCode questCode,

        @NotNull(message = "Date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @NotNull(message = "Time is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime time
) {}
