package com.cencops.demo.service;

import com.cencops.demo.entity.AppManual;
import com.cencops.demo.entity.Notice;
import com.cencops.demo.entity.User;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.repository.AppManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppManualService {

    private final AppManualRepository manualRepository;

    public void saveManual(String title, MultipartFile file, User admin) throws IOException {
        AppManual manual = AppManual.builder()
                .title(title)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileData(file.getBytes())
                .updatedBy(admin.getUsername())
                .build();
        manualRepository.save(manual);
    }

    public List<AppManual> getAllManuals() {
        return manualRepository.findAll();
    }

    public void deleteManual(Long id) {
        manualRepository.deleteById(id);
    }

    public AppManual getManualById(Long id) {
        return manualRepository.findById(id)
                .orElseThrow(() -> new AppExceptionHandler.ResourceNotFoundException("Manual not found"));
    }
}