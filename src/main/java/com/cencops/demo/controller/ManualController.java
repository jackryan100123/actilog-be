package com.cencops.demo.controller;

import com.cencops.demo.dto.response.ApiResponse;

import com.cencops.demo.entity.AppManual;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.AppManualService;
import com.cencops.demo.utils.FileDownloadUtil;
import com.cencops.demo.utils.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/manuals")
@RequiredArgsConstructor
public class ManualController {

    private final AppManualService manualService;
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

    @GetMapping
    public ResponseEntity<?> listManuals() {
        return ResponseEntity.ok(manualService.getAllManuals());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadManual(
            @RequestParam("title") String title,
            @RequestParam(value = "file", required = true) MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ATTACHMENT_EMPTY, HttpStatus.BAD_REQUEST);
        }

        User sessionUser = validateAndGetSessionUser();
        manualService.saveManual(title, file, sessionUser);

        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.MANUAL_ADDED));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> removeManual(@PathVariable Long id) {
        manualService.deleteManual(id);
        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.MANUAL_REMOVED));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','USER')")
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadManualAttachment(@PathVariable Long id) {
        AppManual appManual = manualService.getManualById(id);
        return FileDownloadUtil.prepareDownload(appManual);
    }
}
