package com.example.demo.vo;

import java.util.List;

public class Center {
    private long id;
    private String name;
    private String centerType;
    private String phone;
    private String website;
    private String sido;
    private String sigungu;
    private String address;
    private String description;
    private String therapyTypeCodes; // 쉼표로 합쳐진 코드들 (ChecklistResultMapper용)
    private boolean isActive;
    private String regDate;
    private String updateDate;
    private List<CenterService> services;       // CenterRecommendMapper 컬렉션용
    private List<TherapyType> therapyTypes;     // CenterRecommendMapper 컬렉션용

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCenterType() {
        return centerType;
    }

    public void setCenterType(String centerType) {
        this.centerType = centerType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getSido() {
        return sido;
    }

    public void setSido(String sido) {
        this.sido = sido;
    }

    public String getSigungu() {
        return sigungu;
    }

    public void setSigungu(String sigungu) {
        this.sigungu = sigungu;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTherapyTypeCodes() {
        return therapyTypeCodes;
    }

    public void setTherapyTypeCodes(String therapyTypeCodes) {
        this.therapyTypeCodes = therapyTypeCodes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public List<CenterService> getServices() {
        return services;
    }

    public void setServices(List<CenterService> services) {
        this.services = services;
    }

    public List<TherapyType> getTherapyTypes() {
        return therapyTypes;
    }

    public void setTherapyTypes(List<TherapyType> therapyTypes) {
        this.therapyTypes = therapyTypes;
    }
}
