package com.cencops.demo.service;

import com.cencops.demo.dto.request.*;
import com.cencops.demo.dto.response.UserResponse;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.utils.MessageConstants;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparingLong(UserResponse::getId))
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND_ID + id));

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new DisabledException(MessageConstants.ACCOUNT_INACTIVE);
        }

        return mapToResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException( MessageConstants.USER_NOT_FOUND_USERNAME+ username));
        return mapToResponse(user);
    }

    public void registerUser(RegisterRequest request,User requestingUser) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppExceptionHandler.UserAlreadyExistsException(MessageConstants.USERNAME_EXISTS);
        }
        User.Role assignedRole;
        try {
            assignedRole = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AppExceptionHandler.CustomException(
                    "Invalid Role: '" + request.getRole() + "'. Allowed roles are: ADMIN and ANALYST",
                    HttpStatus.NO_CONTENT
            );
        }

        User user = User.builder()
                .name(request.getName())
                .designation(request.getDesignation())
                .username(request.getUsername())
                .status(User.Status.ACTIVE)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole()))
                .createdBy(requestingUser.getUsername())
                .updatedBy(requestingUser.getUsername())
                .build();

        userRepository.save(user);
    }

    public void adminUpdateUser(Long id, AdminUpdateUserRequest request,User requestingUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        user.setName(request.getName());
        user.setDesignation(request.getDesignation());
        user.setUsername(request.getUsername());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setUpdatedBy(requestingUser.getUsername());
        userRepository.save(user);
    }

    public void adminChangePassword(Long userId, String newPassword, User requestingUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AppExceptionHandler.CustomException(MessageConstants.PASSWORD_SAME_AS_OLD, HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(requestingUser.getUsername());
        userRepository.save(user);
    }

    public void updateUserStatus(Long id, User.Status newStatus,User requestingUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getStatus() != newStatus) {
            user.setStatus(newStatus);
            user.setUpdatedBy(requestingUser.getUsername());
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND_ID + id));
        userRepository.delete(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppExceptionHandler.CustomException(MessageConstants.PASSWORD_INCORRECT, org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppExceptionHandler.CustomException(MessageConstants.PASSWORD_SAME_AS_OLD, org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(user.getUsername());
        userRepository.save(user);
    }

    public User assignRole(Long userId, User.Role newRole, User superUser) {
        if (superUser.getRole() != User.Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can assign roles");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (targetUser.getRole() == newRole) {
            return targetUser;
        }

        targetUser.setRole(newRole);
        targetUser.setUpdatedBy(superUser.getUsername());
        targetUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(targetUser);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setDesignation(user.getDesignation());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        response.setStatus(String.valueOf(user.getStatus()));
        return response;
    }
}
