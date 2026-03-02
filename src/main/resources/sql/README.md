# SQL 실행 순서

1. **신규 DB**: `demo_schema_latest.sql` — 전체 스키마 + 시드 데이터 생성 (post/post_file 제외)
2. **기존 DB 보강**: `demo_patch_latest.sql` — 누락 컬럼·인덱스·백필 (멱등, 반복 실행 안전)
3. **게시판**: `create_post.sql` — post / post_file 테이블 생성 (멱등)

> 구 파일(alter_users_*, ensure_users_schema.sql, create_favorites.sql)은 위 3개로 통합되었으므로 실행 불필요.
