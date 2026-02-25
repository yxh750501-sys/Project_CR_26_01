package com.example.demo.vo;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ChecklistDomain {
    COMMUNICATION("의사소통"),
    SENSORY_DAILY("감각·일상"),
    BEHAVIOR_EMOTION("행동·정서"),
    MOTOR_FINE("운동·소근육"),
    PLAY_SOCIAL("놀이·사회성");

    private final String label;

    ChecklistDomain(String label) {
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
        for (ChecklistDomain d : values()) {
            map.put(d.name(), d.label);
        }
        return map;
    }
}
