package com.cencops.demo.dto.response;

import com.cencops.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyActivityResponse {
    private Long id;
    private LocalDate activityDate;
    private String detailOfCase;
    private String typeOfInformation;
    private String nameOfIO;
    private List<String> toolsUsed;
    private List<String> miscellaneousWork;
    private String status;
    private String remarks;
    private Instant createdAt;
    private Instant updatedAt;
    private String user;
    private String createdBy;
    private String updatedBy;
}
