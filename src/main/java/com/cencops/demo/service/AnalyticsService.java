package com.cencops.demo.service;

import com.cencops.demo.dto.response.DailyActivityResponse;
import com.cencops.demo.entity.DailyActivity;
import com.cencops.demo.repository.DailyActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

        // Fetch logs strictly within the activityDate range
        List<DailyActivity> activities = activityRepository.findDashboardSummary(userId, range[0], range[1]);

        // Group by activityDate (consistent with filter)
        Map<LocalDate, Long> trends = activities.stream()
                .collect(Collectors.groupingBy(DailyActivity::getActivityDate, Collectors.counting()));

        // Generate full date labels for the chart so there are no gaps
        List<LocalDate> dateLabels = range[0].datesUntil(range[1].plusDays(1)).toList();
        List<Long> chartValues = dateLabels.stream()
                .map(d -> trends.getOrDefault(d, 0L))
                .toList();

        long completed = activities.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus().name())).count();
        long inProgress = activities.stream().filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus().name())).count();
        long pending = activities.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus().name())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", activities.size());
        stats.put("chartData", Map.of(
                "labels", dateLabels.stream().map(LocalDate::toString).toList(),
                "datasets", List.of(Map.of(
                        "label", "Logs",
                        "data", chartValues,
                        "borderColor", "#0d9488",
                        "backgroundColor", "rgba(13, 148, 136, 0.1)",
                        "fill", true
                ))
        ));

        stats.put("statusData", Map.of(
                "labels", List.of("Completed", "In Progress", "Pending"),
                "datasets", List.of(Map.of("data", List.of(completed, inProgress, pending)))
        ));

        return stats;
    }

    public Page<DailyActivityResponse> getPaginatedLogs(Long userId, String filterType, String dateStr, Pageable pageable) {
        LocalDate refDate = parseSafeDate(dateStr);
        LocalDate[] range = calculateRange(filterType, refDate);

        // Debugging print to see range in console
        System.out.println("Filter: " + filterType + " | Range: " + range[0] + " to " + range[1]);

        return activityRepository.findByFilters(userId, range[0], range[1], pageable)
                .map(activityService::mapToResponse);
    }

    private LocalDate parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank() || dateStr.equalsIgnoreCase("undefined") || dateStr.equalsIgnoreCase("null")) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr.substring(0, 10));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public LocalDate[] calculateRange(String filterType, LocalDate refDate) {
        String type = (filterType == null) ? "TILL_DATE" : filterType.toUpperCase();
        return switch (type) {
            case "DAILY" -> new LocalDate[]{refDate, refDate};
            case "WEEKLY" -> {
                LocalDate start = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new LocalDate[]{start, start.plusDays(6)};
            }
            case "MONTHLY" -> new LocalDate[]{
                    refDate.with(TemporalAdjusters.firstDayOfMonth()),
                    refDate.with(TemporalAdjusters.lastDayOfMonth())
            };
            case "YEARLY" -> new LocalDate[]{
                    refDate.with(TemporalAdjusters.firstDayOfYear()),
                    refDate.with(TemporalAdjusters.lastDayOfYear())
            };
            default -> new LocalDate[]{LocalDate.of(2000, 1, 1), LocalDate.now()};
        };
    }
}