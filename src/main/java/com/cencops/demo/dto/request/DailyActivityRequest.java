package com.cencops.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyActivityRequest {
    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    @NotBlank(message = "Detail of case is required")
    private String detailOfCase;

    @NotBlank(message = "Type of information is required")
    private String typeOfInformation;

//    @NotBlank(message = "Name of IO is required")
    private String nameOfIO;
//
//    @NotEmpty(message = "At least one tool must be specified")
    private List<String> toolsUsed;

    private List<String> miscellaneousWork;

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;
}