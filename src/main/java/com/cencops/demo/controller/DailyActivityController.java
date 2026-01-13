package com.cencops.demo.controller;

import com.cencops.demo.dto.request.DailyActivityRequest;
import com.cencops.demo.dto.response.ApiResponse;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.DailyActivityService;
import com.cencops.demo.utils.MessageConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/daily-activities")
@RequiredArgsConstructor
public class DailyActivityController {

    private final DailyActivityService dailyActivityService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ACCOUNT_INACTIVE, HttpStatus.FORBIDDEN);
        }
        return user;
    }

    @PreAuthorize("hasRole('ANALYST')")
    @PostMapping
    public ResponseEntity<ApiResponse> createActivity(@Valid @RequestBody DailyActivityRequest request) {
        User user = getAuthenticatedUser();
        dailyActivityService.createActivity(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.ACTIVITY_CREATED));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getMyActivities(
            @RequestParam Map<String, String> allParams,
            Pageable pageable) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(dailyActivityService.getUserActivities(user, allParams, pageable));
    }

    @GetMapping("/tools")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getAllTools() {
        getAuthenticatedUser();
        return ResponseEntity.ok(dailyActivityService.getAllTools());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody DailyActivityRequest request) {

        User user = getAuthenticatedUser();
        boolean isPrivileged = (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN);

        dailyActivityService.update(id, request, user, isPrivileged);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.ACTIVITY_UPDATED));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse> deleteActivity(@PathVariable Long id) {

        User user = getAuthenticatedUser();
        boolean isPrivileged = (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN);

        dailyActivityService.deleteActivity(id, user, isPrivileged);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.ACTIVITY_DELETED));
    }
}