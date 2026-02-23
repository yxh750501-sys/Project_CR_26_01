package com.example.demo.vo;

public class CenterService {
	private long id;
	private long centerId;
	private String therapyTypeCode;
	private String serviceName;
	private Integer targetAgeMin;
	private Integer targetAgeMax;
	private String priceType;
	private String waitlist;
	private String waitlistNote;
	private String notes;
	private String regDate;
	private String updateDate;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCenterId() {
		return centerId;
	}

	public void setCenterId(long centerId) {
		this.centerId = centerId;
	}

	public String getTherapyTypeCode() {
		return therapyTypeCode;
	}

	public void setTherapyTypeCode(String therapyTypeCode) {
		this.therapyTypeCode = therapyTypeCode;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Integer getTargetAgeMin() {
		return targetAgeMin;
	}

	public void setTargetAgeMin(Integer targetAgeMin) {
		this.targetAgeMin = targetAgeMin;
	}

	public Integer getTargetAgeMax() {
		return targetAgeMax;
	}

	public void setTargetAgeMax(Integer targetAgeMax) {
		this.targetAgeMax = targetAgeMax;
	}

	public String getPriceType() {
		return priceType;
	}

	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	public String getWaitlist() {
		return waitlist;
	}

	public void setWaitlist(String waitlist) {
		this.waitlist = waitlist;
	}

	public String getWaitlistNote() {
		return waitlistNote;
	}

	public void setWaitlistNote(String waitlistNote) {
		this.waitlistNote = waitlistNote;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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
}