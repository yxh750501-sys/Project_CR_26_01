package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.vo.PostFile;

@Mapper
public interface PostFileRepository {

    /** 첨부파일 등록 */
    int insertFile(PostFile postFile);

    /** 게시글의 첨부파일 목록 조회 */
    List<PostFile> findByPostId(@Param("postId") long postId);

    /** 단건 첨부파일 조회 (다운로드용) */
    PostFile findById(@Param("id") long id);

    /** 게시글 첨부파일 전체 삭제 (게시글 삭제 시 연계) */
    int deleteByPostId(@Param("postId") long postId);

    /** 첨부파일 단건 삭제 */
    int deleteById(@Param("id") long id);
}
