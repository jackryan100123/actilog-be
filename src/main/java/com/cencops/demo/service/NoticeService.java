package com.cencops.demo.service;


import com.cencops.demo.entity.Notice;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.NoticeRepository;
import com.cencops.demo.utils.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void replaceNoticeByType(String title, String typeStr, MultipartFile file, User admin) throws IOException {

        Notice.NoticeType type;
        try {
            type = Notice.NoticeType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppExceptionHandler.CustomException("Invalid Notice Type", HttpStatus.BAD_REQUEST);
        }

        Notice notice = noticeRepository.findByType(type)
                .orElse(new Notice());

        notice.setTitle(title);
        notice.setType(type);
        notice.setUpdatedBy(admin.getUsername());

        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            notice.setAttachmentData(file.getBytes());
            notice.setAttachmentName(originalName);

            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            }

            String friendlyType = switch (extension) {
                case "pdf" -> "PDF";
                case "doc", "docx" -> "WORD";
                case "xls", "xlsx", "csv" -> "EXCEL";
                default -> "OTHER";
            };

            notice.setAttachmentType(friendlyType);
        }

        noticeRepository.save(notice);
    }

    public Notice getNoticeByType(Notice.NoticeType type) {
        return noticeRepository.findByType(type)
                .orElseThrow(() -> new AppExceptionHandler.ResourceNotFoundException("No notice found for type: " + type));
    }

    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }

    public Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.ResourceNotFoundException("Notice not found"));
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.ResourceNotFoundException("Notice not found"));

        noticeRepository.delete(notice);
    }

}