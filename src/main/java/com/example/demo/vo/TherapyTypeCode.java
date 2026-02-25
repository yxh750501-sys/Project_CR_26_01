package com.example.demo.vo;

import java.util.LinkedHashMap;
import java.util.Map;

public enum TherapyTypeCode {
    SPEECH_THERAPY("언어치료"),
    AAC_COACHING("AAC 코칭/도구 세팅"),
    OT_SENSORY("작업치료(감각·일상)"),
    OT_FINE("작업치료(미세·협응)"),
    ABA_PARENT("행동상담/부모코칭(ABA)"),
    PLAY_THERAPY("놀이치료·사회성"),
    PSY_COUNSEL("심리·정서 상담");

    private final String label;

    TherapyTypeCode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) return "기타";
        try {
            return valueOf(code.trim().toUpperCase()).label;
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    public static Map<String, String> getLabelMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (TherapyTypeCode t : values()) {
            map.put(t.name(), t.label);
        }
        return map;
    }
}
