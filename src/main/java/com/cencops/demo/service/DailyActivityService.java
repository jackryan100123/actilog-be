package com.cencops.demo.service;

import com.cencops.demo.dto.request.DailyActivityRequest;
import com.cencops.demo.dto.response.DailyActivityResponse;
import com.cencops.demo.entity.DailyActivity;
import com.cencops.demo.entity.Tool;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.DailyActivityRepository;
import com.cencops.demo.repository.ToolRepository;
import com.cencops.demo.utils.MessageConstants;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyActivityService {

    private final DailyActivityRepository repository;
    private final ToolRepository toolRepository;

    private boolean isEodPassed(Instant activityCreatedAt) {
        LocalDate activityDate = activityCreatedAt.atZone(ZoneId.systemDefault()).toLocalDate();
        Instant endOfActivityDay = activityDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
        return Instant.now().isAfter(endOfActivityDay);
    }

    private List<Long> resolveToolIds(List<String> toolNames) {
        if (toolNames == null) return List.of();
        return toolNames.stream()
                .map(name -> toolRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> toolRepository.save(Tool.builder().name(name).build()))
                        .getId())
                .collect(Collectors.toList());
    }

    public void createActivity(User user, DailyActivityRequest request) {
        repository.findByUserAndActivityDate(user, request.getActivityDate())
                .ifPresent(a -> {
                    throw new AppExceptionHandler.CustomException(MessageConstants.ACTIVITY_ALREADY_PRESENT, HttpStatus.CONFLICT);
                });

        DailyActivity activity = DailyActivity.builder()
                .user(user)
                .activityDate(request.getActivityDate() != null ? request.getActivityDate() : LocalDate.now())
                .detailOfCase(request.getDetailOfCase())
                .typeOfInformation(request.getTypeOfInformation())
                .nameOfIO(request.getNameOfIO())
                .status(DailyActivity.ActivityStatus.valueOf(request.getStatus()))
                .remarks(request.getRemarks())
                .createdBy(user)
                .updatedBy(user)
                .build();
        activity.setToolsUsedIds(resolveToolIds(request.getToolsUsed()));
        activity.setMiscellaneousWorkList(request.getMiscellaneousWork());

        repository.save(activity);
    }

    public Page<DailyActivityResponse> getUserActivities(User user, Map<String, String> allParams, Pageable pageable) {
        Specification<DailyActivity> spec = buildDynamicQuery(allParams, user);
        return repository.findAll(spec, pageable).map(this::mapToResponse);
    }

//    public List<DailyActivityResponse> getUserActivities(User user) {
//        // Updated to include SUPER_ADMIN for fetching all records
//        return (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN
//                ? repository.findAll()
//                : repository.findAllByUser(user))
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    public Page<DailyActivityResponse> getUserActivities(User user, Pageable pageable) {
//        Page<DailyActivity> page = (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN)
//                ? repository.findAll(pageable)
//                : repository.findAllByUser(user, pageable);
//
//        return page.map(this::mapToResponse);
//    }

    public List<Tool> getAllTools() {
        return toolRepository.findAll();
    }

    public void update(Long id, DailyActivityRequest request, User user, boolean isPrivileged) {
        DailyActivity activity = repository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.ResourceNotFoundException(
                        MessageConstants.ACTIVITY_NOT_FOUND
                ));

        if (!isPrivileged) {
            if (!activity.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException(MessageConstants.UNAUTHORIZED);
            }
//            if (isEodPassed(activity.getCreatedAt())) {
//                throw new AppExceptionHandler.CustomException(
//                        MessageConstants.EDIT_AFTER_DAY_END,
//                        HttpStatus.FORBIDDEN
//                );
//            }
        }

        activity.setActivityDate(request.getActivityDate());
        activity.setDetailOfCase(request.getDetailOfCase());
        activity.setTypeOfInformation(request.getTypeOfInformation());
        activity.setNameOfIO(request.getNameOfIO());
        activity.setToolsUsedIds(resolveToolIds(request.getToolsUsed()));
        activity.setMiscellaneousWorkList(request.getMiscellaneousWork());
        activity.setStatus(DailyActivity.ActivityStatus.valueOf(request.getStatus()));
        activity.setRemarks(request.getRemarks());
        activity.setUpdatedBy(user);
        repository.save(activity);
    }

    public void deleteActivity(Long id, User user, boolean isPrivileged) {
        DailyActivity activity = repository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.ResourceNotFoundException(
                        MessageConstants.ACTIVITY_NOT_FOUND
                ));

        if (!isPrivileged) {
            if (!activity.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException(MessageConstants.UNAUTHORIZED);
            }
            if (isEodPassed(activity.getCreatedAt())) {
                throw new AppExceptionHandler.CustomException(
                        MessageConstants.DELETE_AFTER_DAY_END,
                        HttpStatus.FORBIDDEN
                );
            }
        }

        repository.delete(activity);
    }

    public DailyActivityResponse mapToResponse(DailyActivity activity) {
        List<String> toolNames = activity.getToolsUsedIds().stream()
                .map(id -> toolRepository.findById(id).map(Tool::getName).orElse("Unknown"))
                .collect(Collectors.toList());

        return DailyActivityResponse.builder()
                .id(activity.getId())
                .activityDate(activity.getActivityDate())
                .detailOfCase(activity.getDetailOfCase())
                .typeOfInformation(activity.getTypeOfInformation())
                .nameOfIO(activity.getNameOfIO())
                .toolsUsed(toolNames)
                .miscellaneousWork(activity.getMiscellaneousWorkList())
                .status(activity.getStatus().name())
                .remarks(activity.getRemarks())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .user(activity.getUser().getName())
                .updatedBy(activity.getUpdatedBy().getUsername())
                .createdBy(activity.getCreatedBy().getUsername())
                .build();
    }

    public static Specification<DailyActivity> buildDynamicQuery(Map<String, String> allParams, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.SUPER_ADMIN) {
                predicates.add(cb.equal(root.get("user"), user));
            }
            allParams.forEach((key, value) -> {
                if (value == null || value.isEmpty() || List.of("page", "size", "sort").contains(key)) return;

                if (key.equals("global")) {
                    String pattern = "%" + value.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("detailOfCase")), pattern),
                            cb.like(cb.lower(root.get("typeOfInformation")), pattern),
                            cb.like(cb.lower(root.get("nameOfIO")), pattern)
                    ));
                }
                else if (key.equals("user.name")) {
                    predicates.add(cb.like(cb.lower(root.join("user").get("name")), "%" + value.toLowerCase() + "%"));
                }

                else if (key.equals("status")) {
                    predicates.add(cb.equal(root.get("status"), value));
                }
                else {
                    predicates.add(cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%"));
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}