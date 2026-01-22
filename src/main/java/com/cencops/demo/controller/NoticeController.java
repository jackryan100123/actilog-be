package com.cencops.demo.controller;

import com.cencops.demo.dto.response.ApiResponse;
import com.cencops.demo.entity.Notice;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.UserRepository;
import com.cencops.demo.service.NoticeService;
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
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final UserRepository userRepository;
    private final NoticeService noticeService;

    private User validateAndGetSessionUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppExceptionHandler.UserNotFoundException(MessageConstants.USER_NOT_FOUND));

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ACCOUNT_INACTIVE, HttpStatus.FORBIDDEN);
        }
        return user;
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadNotice(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "file", required = true) MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new AppExceptionHandler.CustomException(MessageConstants.ATTACHMENT_EMPTY, HttpStatus.BAD_REQUEST);
        }

        User user = validateAndGetSessionUser();
        noticeService.replaceNoticeByType(title, type, file, user);

        return ResponseEntity.ok(new ApiResponse(MessageConstants.SUCCESS, MessageConstants.NOTICE_CREATED + type));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','USER')")
    @GetMapping
    public ResponseEntity<?> getNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','USER')")
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadNoticeAttachment(@PathVariable Long id) {
        Notice notice = noticeService.getNoticeById(id);
        return FileDownloadUtil.prepareDownload(notice);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok().body("Notice deleted successfully");
    }
}
