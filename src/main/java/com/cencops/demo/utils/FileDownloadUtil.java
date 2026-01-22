package com.cencops.demo.utils;

import com.cencops.demo.entity.Notice;
import com.cencops.demo.exception.AppExceptionHandler;
import com.cencops.demo.entity.AppManual;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class FileDownloadUtil {

    public static MediaType getMediaType(String attachmentType) {
        if (attachmentType == null) return MediaType.APPLICATION_OCTET_STREAM;

        switch (attachmentType.toUpperCase()) {
            case "PDF":
                return MediaType.APPLICATION_PDF;
            case "WORD":
            case "DOCX":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "DOC":
                return MediaType.parseMediaType("application/msword");
            case "EXCEL":
            case "XLSX":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "XLS":
                return MediaType.parseMediaType("application/vnd.ms-excel");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    public static ResponseEntity<byte[]> prepareDownload(Notice notice) {
        if (notice.getAttachmentData() == null || notice.getAttachmentData().length == 0) {
            throw new AppExceptionHandler.CustomException(
                    "No file attachment associated with this notice",
                    HttpStatus.NOT_FOUND
            );
        }

        return ResponseEntity.ok()
                .contentType(getMediaType(notice.getAttachmentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + notice.getAttachmentName() + "\"")
                .body(notice.getAttachmentData());
    }

    public static ResponseEntity<byte[]> prepareDownload(AppManual appManual) {
        if (appManual.getFileData() == null || appManual.getFileData().length == 0) {
            throw new AppExceptionHandler.CustomException(
                    "No file attachment associated with this Manual",
                    HttpStatus.NOT_FOUND
            );
        }

        return ResponseEntity.ok()
                .contentType(getMediaType(appManual.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + appManual.getFileName() + "\"")
                .body(appManual.getFileData());
    }
}