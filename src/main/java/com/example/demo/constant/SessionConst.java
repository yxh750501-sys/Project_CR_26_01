package com.example.demo.constant;

/**
 * HTTP 세션 키 상수 정의.
 *
 * <p>세션에 값을 저장/조회할 때 문자열 리터럴 대신 이 상수를 사용한다.
 */
public class SessionConst {

    /** 로그인된 사용자 PK */
    public static final String LOGINED_USER_ID   = "loginedUserId";

    /** 로그인된 사용자 역할 (GUARDIAN / THERAPIST 등) */
    public static final String LOGINED_USER_ROLE = "loginedUserRole";

    /** 현재 선택된 아이 PK */
    public static final String SELECTED_CHILD_ID = "selectedChildId";

    private SessionConst() { /* 인스턴스화 금지 */ }
}
