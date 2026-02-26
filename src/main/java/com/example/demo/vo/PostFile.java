package com.example.demo.vo;

/**
 * 게시글 첨부파일 VO.
 * 저장 파일명은 UUID 기반 (origName 은 원본 파일명).
 */
public class PostFile {

    private long   id;
    private long   postId;
    private String origName;    // 원본 파일명 (다운로드 시 Content-Disposition 에 사용)
    private String storedName;  // UUID 기반 저장 파일명
    private long   fileSize;    // 바이트 단위
    private String createdAt;

    // ── getters / setters ──────────────────────────────────────

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPostId() { return postId; }
    public void setPostId(long postId) { this.postId = postId; }

    public String getOrigName() { return origName; }
    public void setOrigName(String origName) { this.origName = origName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
