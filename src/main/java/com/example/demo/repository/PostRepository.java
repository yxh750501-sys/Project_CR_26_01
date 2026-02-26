package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.Post;

@Mapper
public interface PostRepository {

    /** id로 게시글 단건 조회 (authorName JOIN 포함) */
    Post findById(@Param("id") long id);

    /** 게시판 목록 페이지네이션 */
    List<Post> findPageByBoard(@Param("boardType") String boardType,
                               @Param("offset")    int offset,
                               @Param("size")      int size);

    /** 게시판 총 게시글 수 */
    int countByBoard(@Param("boardType") String boardType);

    /** 게시글 등록 */
    int insertPost(Post post);

    /** 게시글 수정 */
    int updateById(Post post);

    /** 게시글 삭제 */
    int deleteById(@Param("id") long id);

    /** 직전 INSERT의 auto-increment PK */
    long getLastInsertId();
}
