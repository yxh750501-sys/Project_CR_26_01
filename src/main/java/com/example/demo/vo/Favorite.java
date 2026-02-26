package com.example.demo.vo;

public class Favorite {

    private long   id;
    private long   memberId;
    private long   centerId;
    private String regDate;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }

    public long getCenterId() { return centerId; }
    public void setCenterId(long centerId) { this.centerId = centerId; }

    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }
}
