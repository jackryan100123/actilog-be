package com.cencops.demo.repository;

import com.cencops.demo.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<Notice> findByType(Notice.NoticeType type);

    void deleteByType(Notice.NoticeType type);
}