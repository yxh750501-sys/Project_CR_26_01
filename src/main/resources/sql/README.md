# SQL 실행 순서
1. 신규 DB: `demo_schema_latest.sql` (post/post_file 제외)
2. 기존 DB 보강: `demo_patch_latest.sql` (멱등, 반복 실행 안전)
3. 게시판: `create_post.sql` (post/post_file)
4. 구 개별 패치 스크립트는 demo_patch_latest.sql로 통합
