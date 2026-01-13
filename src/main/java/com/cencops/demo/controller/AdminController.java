package com.cencops.demo.controller;

import com.cencops.demo.dto.request.AdminChangePasswordRequest;
import com.cencops.demo.dto.request.AdminUpdateUserRequest;
import com.cencops.demo.dto.request.RegisterRequest;
import com.cencops.demo.dto.response.UserResponse;
import com.cencops.demo.dto.response.ApiResponse;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.cencops.demo.utils.MessageConstants;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    private User validateAndGetSessionUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ACCOUNT_INACTIVE, HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private void checkPrivileges(User currentUser, Long targetId) {
        if (currentUser.getId().equals(targetId)) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ACTION_NOT_ALLOWED, HttpStatus.FORBIDDEN);
        }

        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (currentUser.getRole() == User.Role.ADMIN && targetUser.getRole() == User.Role.SUPER_ADMIN) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ADMIN_MODIFICATION_RESTRICTED, HttpStatus.FORBIDDEN);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','MANAGER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = validateAndGetSessionUser();
        userService.registerUser(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.USER_CREATED));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id,@Valid @RequestBody AdminUpdateUserRequest request) {
        User user = validateAndGetSessionUser();
        checkPrivileges(user, id);
        userService.adminUpdateUser(id, request, user);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.USER_UPDATED));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse> changeStatus(@PathVariable Long id, @Valid @RequestParam("status") String statusStr) {
        User user = validateAndGetSessionUser();
        checkPrivileges(user, id);

        User.Status status;
        try {
            status = User.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppExceptionHandler.CustomException(MessageConstants.INVALID_STATUS, HttpStatus.BAD_REQUEST);
        }

        userService.updateUserStatus(id, status, user);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.USER_STATUS_UPDATED + status));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PutMapping("/users/{id}/password")
    public ResponseEntity<ApiResponse> updateUserPasswordByAdmin(@PathVariable Long id, @Valid @RequestBody AdminChangePasswordRequest request) {
        User user = validateAndGetSessionUser();
        checkPrivileges(user, id);
        userService.adminChangePassword(id, request.getNewPassword(), user);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.PASSWORD_UPDATED));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        User user = validateAndGetSessionUser();
        checkPrivileges(user, id);
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.USER_DELETED));
    }
}