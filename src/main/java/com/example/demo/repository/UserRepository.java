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
            @Param("role")        String role,
            @Param("memberType")  String memberType,
            @Param("displayRole") String displayRole,
            @Param("orgName")     String orgName
    );

    long getLastInsertId();
}
