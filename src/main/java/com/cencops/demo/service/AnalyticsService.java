package com.cencops.demo.service;

import com.cencops.demo.dto.response.DailyActivityResponse;
import com.cencops.demo.entity.DailyActivity;
import com.cencops.demo.repository.DailyActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DailyActivityRepository activityRepository;
    private final DailyActivityService activityService;

    public Map<String, Object> getDashboardStats(Long userId, String filterType, String dateStr) {
        LocalDate refDate = parseSafeDate(dateStr);
        LocalDate[] range = calculateRange(filterType, refDate);

        List<DailyActivity> activities = activityRepository.findDashboardSummary(userId, range[0], range[1]);

        long completed = activities.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus().name())).count();
        long inProgress = activities.stream().filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus().name())).count();
        long pending = activities.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus().name())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", activities.size());
        stats.put("completedTasks", completed);
        stats.put("inProgressTasks", inProgress);
        stats.put("pendingTasks", pending);
        stats.put("allLogsForExcel", activities.stream().map(activityService::mapToResponse).collect(Collectors.toList()));

//        Map<LocalDate, Long> trends = activities.stream()
//                .collect(Collectors.groupingBy(DailyActivity::getActivityDate, Collectors.counting()));
//        List<LocalDate> sortedDates = trends.keySet().stream().sorted().toList();

        Map<LocalDate, Long> trends = activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> LocalDate.ofInstant(activity.getCreatedAt(), ZoneId.systemDefault()),
                        Collectors.counting()
                ));
        List<LocalDate> sortedDates = trends.keySet().stream().sorted().toList();

        stats.put("chartData", Map.of(
                "labels", sortedDates.stream().map(LocalDate::toString).toList(),
                "datasets", List.of(Map.of("label", "Logs Created", "data", sortedDates.stream().map(trends::get).toList(),
                        "borderColor", "#0d9488", "backgroundColor", "rgba(13, 148, 136, 0.1)", "fill", true, "tension", 0.4))
        ));

        stats.put("statusData", Map.of(
                "labels", List.of("Completed", "In Progress", "Pending"),
                "datasets", List.of(Map.of(
                        "data", List.of(completed, inProgress, pending),
                        "backgroundColor", List.of("#0d9488", "#3b82f6", "#f59e0b")
                ))
        ));

        stats.put("allLogsForExcel", activities.stream()
                .map(activityService::mapToResponse)
                .collect(Collectors.toList()));

        return stats;
    }

    public Page<DailyActivityResponse> getPaginatedLogs(Long userId, String filterType, String dateStr, Pageable pageable) {
        LocalDate refDate = parseSafeDate(dateStr);
        LocalDate[] range = calculateRange(filterType, refDate);
        return activityRepository.findByFilters(userId, range[0], range[1], pageable)
                .map(activityService::mapToResponse);
    }

    private LocalDate parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("undefined")) return LocalDate.now();
        try {
            return LocalDate.parse(dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public LocalDate[] calculateRange(String filterType, LocalDate refDate) {
        LocalDate start;
        LocalDate end = LocalDate.now();

        switch (filterType) {
            case "DAILY":
                start = refDate;
                end = refDate;
                break;
            case "WEEKLY":
                start = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = start.plusDays(6);
                break;
            case "MONTHLY":
                start = refDate.with(TemporalAdjusters.firstDayOfMonth());
                end = refDate.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "YEARLY":
                start = refDate.with(TemporalAdjusters.firstDayOfYear());
                end = refDate.with(TemporalAdjusters.lastDayOfYear());
                break;
            case "TILL_DATE":
            default:
                start = LocalDate.of(2000, 1, 1);
                end = LocalDate.now();
                break;
        }
        return new LocalDate[]{start, end};
    }
}