package com.cencops.demo.controller;

import com.cencops.demo.dto.request.ChangePasswordRequest;
import com.cencops.demo.dto.response.ApiResponse;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.UserService;
import com.cencops.demo.utils.MessageConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if(user.getStatus()== User.Status.INACTIVE){
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(new ApiResponse(HttpStatusCode.valueOf(403).toString(),MessageConstants.ACCOUNT_INACTIVE));
        }
        return ResponseEntity.ok(userService.getUserById(user.getId()));
    }
    
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));
        if(user.getStatus()== User.Status.INACTIVE){
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(new ApiResponse(HttpStatusCode.valueOf(403).toString(),MessageConstants.ACCOUNT_INACTIVE));
        }
        userService.changePassword(user.getId(), request);

        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.PASSWORD_UPDATED));
    }
}
