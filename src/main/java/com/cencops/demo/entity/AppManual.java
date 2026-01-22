package com.cencops.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_manuals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String fileName;
    private String fileType;

    @Lob
    @Column(name = "file_data", columnDefinition = "BYTEA")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private byte[] fileData;

    private LocalDateTime updatedAt;
    private String updatedBy;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}