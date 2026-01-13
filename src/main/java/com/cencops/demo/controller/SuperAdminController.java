package com.cencops.demo.controller;

import com.cencops.demo.dto.response.ApiResponse;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.UserService;
import com.cencops.demo.utils.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse> assignRole(
            @PathVariable Long userId,
            @RequestParam User.Role role
    ) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        userService.assignRole(userId, role,user);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.ROLE_UPDATED));
    }
}
