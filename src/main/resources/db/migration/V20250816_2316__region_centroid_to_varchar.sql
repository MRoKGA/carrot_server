-- region.centroid: GEOMETRY -> VARCHAR(64)

-- 우선 타입 변경 시도
ALTER TABLE region MODIFY COLUMN centroid VARCHAR(64) NULL;

-- 만약 위 구문이 실패하면(환경에 따라 공간타입 변경 제한)
-- 아래 두 줄로 대체해서 다시 커밋/배포하세요 (데이터는 삭제됨)
-- ALTER TABLE region DROP COLUMN centroid;
-- ALTER TABLE region ADD COLUMN centroid VARCHAR(64) NULL;
