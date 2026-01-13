package com.cencops.demo.repository;

import com.cencops.demo.entity.DailyActivity;
import com.cencops.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long>, JpaSpecificationExecutor<DailyActivity> {

    Optional<DailyActivity> findByUserAndActivityDate(User user, LocalDate activityDate);

    List<DailyActivity> findAllByUser(User user);

    Page<DailyActivity> findAllByUser(User user, Pageable pageable);

    @Query("SELECT d FROM DailyActivity d WHERE " +
            "(:userId IS NULL OR d.user.id = :userId) AND " +
            "(d.activityDate BETWEEN :startDate AND :endDate)")
    List<DailyActivity> findDashboardSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DailyActivity d WHERE " +
            "(:userId IS NULL OR d.user.id = :userId) AND " +
            "(cast(:startDate as localdate) IS NULL OR d.activityDate >= :startDate) AND " +
            "(cast(:endDate as localdate) IS NULL OR d.activityDate <= :endDate) " +
            "ORDER BY d.activityDate DESC")
    Page<DailyActivity> findByFiltersPaged(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT d FROM DailyActivity d WHERE " +
            "(:userId IS NULL OR d.user.id = :userId) AND " +
            "(d.activityDate BETWEEN :startDate AND :endDate) " +
            "ORDER BY d.activityDate DESC")
    Page<DailyActivity> findByFilters(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}