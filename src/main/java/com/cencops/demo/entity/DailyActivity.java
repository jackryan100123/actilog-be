package com.cencops.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "daily_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate activityDate;

    private String detailOfCase;
    private String typeOfInformation;
    private String nameOfIO;

    @Column(name = "tools_used")
    private String toolsUsed;

    @Column(name = "miscellaneous_work")
    private String miscellaneousWork;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    private String remarks;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id", nullable = false)
    private User updatedBy;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public List<Long> getToolsUsedIds() {
        if (toolsUsed == null || toolsUsed.isEmpty()) return List.of();
        return Arrays.stream(toolsUsed.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public void setToolsUsedIds(List<Long> ids) {
        this.toolsUsed = ids == null || ids.isEmpty()
                ? null
                : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public List<String> getMiscellaneousWorkList() {
        if (miscellaneousWork == null || miscellaneousWork.isEmpty()) return List.of();
        return Arrays.stream(miscellaneousWork.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public void setMiscellaneousWorkList(List<String> list) {
        this.miscellaneousWork = list == null || list.isEmpty()
                ? null
                : String.join(",", list);
    }

    public enum ActivityStatus {
        PENDING, IN_PROGRESS, COMPLETED
    }
}
