# SQL 실행 순서
1. 신규 DB: `demo_schema_latest.sql` (post/post_file 제외)
2. 기존 DB 보강: `demo_patch_latest.sql` (멱등, 반복 실행 안전)
3. 게시판: `create_post.sql` (post/post_file)
4. 구 파일(alter_users_*, ensure_users_schema.sql, create_favorites.sql)은 통합되어 실행 불필요
