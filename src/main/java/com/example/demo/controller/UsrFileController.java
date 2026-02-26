package com.example.demo.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.repository.PostFileRepository;
import com.example.demo.service.FileStorageService;
import com.example.demo.vo.PostFile;

/**
 * 첨부파일 다운로드 컨트롤러.
 * GET /usr/file/download?id=
 * 비로그인 접근 허용 (WebMvcConfig NeedLoginInterceptor 제외 목록).
 */
@Controller
@RequestMapping("/usr/file")
public class UsrFileController {

    private static final Logger log = LoggerFactory.getLogger(UsrFileController.class);

    private final PostFileRepository postFileRepository;
    private final FileStorageService fileStorageService;

    public UsrFileController(PostFileRepository postFileRepository,
                             FileStorageService fileStorageService) {
        this.postFileRepository = postFileRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("id") long id) {
        PostFile pf = postFileRepository.findById(id);
        if (pf == null) {
            return ResponseEntity.notFound().build();
        }

        Path path = fileStorageService.getFilePath(pf.getStoredName());
        if (!Files.exists(path)) {
            log.warn("다운로드 파일 없음: fileId={}, storedName={}", id, pf.getStoredName());
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(path.toUri());

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // RFC 5987 인코딩 (한글 파일명 지원)
            String encodedName = URLEncoder.encode(pf.getOrigName(), StandardCharsets.UTF_8)
                                           .replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedName
                            + "\"; filename*=UTF-8''" + encodedName)
                    .body(resource);

        } catch (IOException e) {
            log.error("파일 다운로드 오류: fileId={}, storedName={}", id, pf.getStoredName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
