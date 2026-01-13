package com.cencops.demo.controller;

import com.cencops.demo.dto.response.DailyActivityResponse;
import com.cencops.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "DAILY") String filterType,
            @RequestParam(required = false) String date) {

        String finalDate = (date == null) ? LocalDate.now().toString() : date;
        return ResponseEntity.ok(analyticsService.getDashboardStats(userId, filterType, finalDate));
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<DailyActivityResponse>> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam String filterType,
            @RequestParam String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(analyticsService.getPaginatedLogs(userId, filterType, date, PageRequest.of(page, size)));
    }
}