package com.cencops.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NoticeType type;

    private String title;

    @Lob
    @Column(name = "attachment_data", columnDefinition = "BYTEA")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    @JsonIgnore
    private byte[] attachmentData;

    private String attachmentName;
    private String attachmentType;

    private LocalDateTime updatedAt;
    private String updatedBy;

    public enum NoticeType {
        MONTHLY, CYBER_POLICE_STATION,OTHER
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}