package com.example.demo.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 VO.
 * boardType: PROGRAM(프로그램 게시판) / FREE(자유게시판)
 */
public class Post {

    private long   id;
    private String boardType;    // PROGRAM / FREE
    private String category;     // CAMP / SPECIAL (PROGRAM 전용)
    private String title;
    private String body;
    private long   memberId;
    private String authorName;   // users.name (JOIN 결과)

    // PROGRAM 전용 선택 컬럼
    private String  startDate;
    private String  endDate;
    private String  location;
    private Integer fee;
    private Integer maxPeople;
    private String  applyUrl;

    private String createdAt;
    private String updatedAt;

    private List<PostFile> files = new ArrayList<>();

    // ── getters / setters ──────────────────────────────────────

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getBoardType() { return boardType; }
    public void setBoardType(String boardType) { this.boardType = boardType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getFee() { return fee; }
    public void setFee(Integer fee) { this.fee = fee; }

    public Integer getMaxPeople() { return maxPeople; }
    public void setMaxPeople(Integer maxPeople) { this.maxPeople = maxPeople; }

    public String getApplyUrl() { return applyUrl; }
    public void setApplyUrl(String applyUrl) { this.applyUrl = applyUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<PostFile> getFiles() { return files; }
    public void setFiles(List<PostFile> files) {
        this.files = (files != null) ? files : new ArrayList<>();
    }
}
