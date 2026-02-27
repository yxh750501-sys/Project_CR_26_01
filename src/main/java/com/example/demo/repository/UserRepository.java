package com.example.demo.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.User;

@Mapper
public interface UserRepository {

    User getUserById(@Param("id") long id);

    User getUserByLoginId(@Param("loginId") String loginId);

    /** loginId 중복 여부 확인 */
    boolean existsByLoginId(@Param("loginId") String loginId);

    /** email 중복 여부 확인 */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 신규 사용자 등록.
     * loginPw는 호출 전에 BCrypt 해싱된 값이어야 한다.
     */
    int createUser(
            @Param("loginId")     String loginId,
            @Param("loginPw")     String loginPw,
            @Param("name")        String name,
            @Param("email")       String email,
            @Param("phone")       String phone,
            @Param("role")        String role,
            @Param("memberType")  String memberType,
            @Param("displayRole") String displayRole,
            @Param("orgName")     String orgName
    );

    /** 프로필(이름, 전화번호) 수정 */
    int updateProfile(@Param("id")    long   id,
                      @Param("name")  String name,
                      @Param("phone") String phone);

    /** 비밀번호 변경 (loginPw 는 BCrypt 해시값) */
    int updatePassword(@Param("id")      long   id,
                       @Param("loginPw") String loginPw);

    /** 프로필 이미지 파일명 저장 */
    int updateProfileImage(@Param("id")           long   id,
                           @Param("profileImage") String profileImage);

    long getLastInsertId();
}
