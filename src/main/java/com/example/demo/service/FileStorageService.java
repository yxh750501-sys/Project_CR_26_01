package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.vo.PostFile;

/**
 * 파일 저장/조회/삭제 서비스.
 *
 * <p>저장 정책:
 * <ul>
 *   <li>저장 디렉토리: {@code app.upload.dir} (기본: ${user.home}/careroute-uploads)</li>
 *   <li>파일명: UUID + 원본 확장자 (원본 파일명 노출 방지)</li>
 *   <li>허용 확장자: jpg, jpeg, png, gif, pdf, doc, docx, xls, xlsx, ppt, pptx, hwp, zip</li>
 *   <li>최대 크기: 10MB (spring.servlet.multipart.max-file-size 로 1차 제한)</li>
 * </ul>
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif",
            "pdf",
            "doc", "docx",
            "xls", "xlsx",
            "ppt", "pptx",
            "hwp",
            "zip"
    );
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * MultipartFile을 업로드 디렉토리에 저장하고 PostFile 메타 객체를 반환한다.
     *
     * @param file   업로드된 파일
     * @param postId 연결할 게시글 PK
     * @return DB 저장용 PostFile (id/createdAt 제외)
     * @throws RuntimeException 확장자 불허·크기 초과·IO 오류 시
     */
    public PostFile store(MultipartFile file, long postId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
        }

        String origName = sanitizeFilename(file.getOriginalFilename());
        String ext      = extractExtension(origName);

        if (!ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + ext);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다: " + origName);
        }

        String storedName = UUID.randomUUID() + "." + ext.toLowerCase();
        Path   dir        = Paths.get(uploadDir);
        Path   dest       = dir.resolve(storedName);

        try {
            Files.createDirectories(dir);
            file.transferTo(dest.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + origName, e);
        }

        PostFile pf = new PostFile();
        pf.setPostId(postId);
        pf.setOrigName(origName);
        pf.setStoredName(storedName);
        pf.setFileSize(file.getSize());
        return pf;
    }

    /**
     * 저장된 파일의 Path를 반환한다 (다운로드용).
     */
    public Path getFilePath(String storedName) {
        return Paths.get(uploadDir).resolve(storedName);
    }

    /**
     * 디스크에서 파일을 삭제한다 (존재하지 않으면 무시).
     */
    public void deleteFile(String storedName) {
        try {
            Path path = Paths.get(uploadDir).resolve(storedName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 삭제 실패는 무시 — 운영에서는 별도 로그 수집 권장
        }
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "unknown";
        // 경로 조작 방지: 파일명만 추출
        return Paths.get(filename).getFileName().toString();
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }
        return filename.substring(dot + 1);
    }
}
