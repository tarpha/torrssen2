/* RSS Feed List DB 기초 데이터 생성 */
-- INSERT INTO RSS_LIST(name, url, use_db, link_key, create_dt)
-- SELECT 'TORRENTWAL' name
--     , 'https://torrentwal.net/bbs//rss.php?b=torrent_tv' url
--      , true use_db
--      , 'link' link_key
--      , CURRENT_TIMESTAMP create_dt
-- WHERE  NOT EXISTS(SELECT * FROM RSS_LIST);

/* SETTING 기초 데이터 생성 */
INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'INIT' key
     , 'FALSE' value
     , 'init' type
     , true required
     , '초기 설정 완료 여부'
     , 'false'
     , 0
     , CURRENT_TIMESTAMP create_dt
UNION ALL   
SELECT 'RSS_LOAD_INTERVAL' key
     , '30' value
     , 'number' type
     , true required
     , 'RSS 갱신 주기 (분)'
     , '일반'
     , 0
     , CURRENT_TIMESTAMP create_dt 
UNION ALL
SELECT 'DOWNLOAD_CHECK_INTERVAL' key
     , '1' value
     , 'number' type
     , true required
     , '다운로드 완료 검사 주기 (분)'
     , '일반'
     , 1
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DONE_DELETE' key
     , 'TRUE' value
     , 'boolean' type
     , true required
     , '완료 시 삭제'
     , '일반'
     , 2
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DOWNLOAD_APP' key
     , 'TRANSMISSION' value
     , 'app' type
     , true required
     , '기본 다운로드 앱'
     , '일반'
     , 3
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'SEASON_PREFIX' key
     , 'SEASON ' value
     , 'text' type
     , true required
     , '시즌 폴더 접두사'
     , '일반'
     , 4
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DARK_THEME' key
     , 'FALSE' value
     , 'boolean' type
     , true required
     , '다크 테마'
     , '일반'
     , 5
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'SEND_TELEGRAM' key
     , 'TRUE' value
     , 'boolean' type
     , true required
     , '다운로드 완료 시 발송'
     , '텔레그램'
     , 10
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TELEGRAM_TOKEN' key
     , '' value
     , 'text' type
     , false required
     , '토큰'
     , '텔레그램'
     , 11
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TELEGRAM_CHAT_ID' key
     , '' value
     , 'text' type
     , false required
     , '아이디 (쉼표로 복수 가능)'
     , '텔레그램'
     , 12
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TRANSMISSION_HOST' key
     , '127.0.0.1' value
     , 'text' type
     , false required
     , '호스트'
     , '트랜스미션'
     , 20
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TRANSMISSION_PORT' key
     , '9091' value
     , 'number' type
     , false required
     , '포트'
     , '트랜스미션'
     , 21
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TRANSMISSION_USERNAME' key
     , '' value
     , 'text' type
     , false required
     , '아이디'
     , '트랜스미션'
     , 22
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TRANSMISSION_PASSWORD' key
     , '' value
     , 'password' type
     , false required
     , '비밀번호'
     , '트랜스미션'
     , 23
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DS_HOST' key
     , '127.0.0.1' value
     , 'text' type
     , false required
     , '호스트'
     , '다운로드 스테이션'
     , 30
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DS_PORT' key
     , '5000' value
     , 'number' type
     , false required
     , '포트'
     , '다운로드 스테이션'
     , 31
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DS_USERNAME' key
     , '' value
     , 'text' type
     , false required
     , '아이디'
     , '다운로드 스테이션'
     , 32
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'DS_PASSWORD' key
     , '' value
     , 'password' type
     , false required
     , '비밀번호'
     , '다운로드 스테이션'
     , 33
     , CURRENT_TIMESTAMP create_dt
) x
WHERE  NOT EXISTS(SELECT * FROM SETTING);

/* DOWNLOAD PATH 기초 데이터 생성 */
INSERT INTO DOWNLOAD_PATH(name, path, use_title, use_season, create_dt) SELECT * FROM (
SELECT 'DOWNLOAD'   name
     , '/download'  path
     , false use_title
     , false use_season
     , CURRENT_TIMESTAMP create_dt 
UNION ALL
SELECT 'TV_TITLE' name
     , '/video/TV' path
     , true  use_title
     , false use_season
     , CURRENT_TIMESTAMP create_dt
UNION ALL
SELECT 'TV_TITLE_SEASON' name
     , '/video/TV' path
     , true  use_title
     , true use_season
     , CURRENT_TIMESTAMP create_dt
) x
WHERE  NOT EXISTS(SELECT * FROM DOWNLOAD_PATH);

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'USE_LIMIT' key
     , 'TRUE' value
     , 'boolean' type
     , true required
     , '건 수 제한 여부'
     , 'FEED 관리'
     , 40
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'USE_LIMIT');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'LIMIT_COUNT' key
     , '10000' value
     , 'number' type
     , true required
     , '제한 건 수'
     , 'FEED 관리'
     , 41
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'LIMIT_COUNT');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'USE_LOGIN' key
     , 'FALSE' value
     , 'boolean' type
     , true required
     , '사용여부'
     , '로그인'
     , 50
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'USE_LOGIN');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'TRANSMISSION_CALLBACK' key
     , 'FALSE' value
     , 'boolean' type
     , true required
     , '콜백 사용'
     , '트랜스미션'
     , 24
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'TRANSMISSION_CALLBACK');

-- INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
-- SELECT 'EMBEDDED_LIMIT' key
--      , '4' value
--      , 'number' type
--      , true required
--      , '동시 다운로드 수 (EMBEDDED만 적용)'
--      , '일반'
--      , 6
--      , CURRENT_TIMESTAMP create_dt
-- ) x
-- WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'EMBEDDED_LIMIT');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'USE_CRON' key
     , 'TRUE' value
     , 'boolean' type
     , true required
     , '자동 재시작 사용여부'
     , '일반'
     , 7
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'USE_CRON');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'CRON_EXR' key
     , '0 0 4 * * ?' value
     , 'text' type
     , true required
     , '자동 재시작 스케줄 (CRON)'
     , '일반'
     , 8
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'CRON_EXR');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'DEL_DIR' key
     , 'TRUE' value
     , 'boolean' type
     , true required
     , '폴더 파일 추출'
     , '다운로드'
     , 9
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'DEL_DIR');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'EXCEPT_EXT' key
     , '' value
     , 'text' type
     , true required
     , '추출 제외 확장자 (쉼표로 복수 가능)'
     , '다운로드'
     , 9
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'EXCEPT_EXT');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'TRANSMISSION_TEST' key
     , 'transmissionTest' value
     , 'button' type
     , false required
     , '접속 테스트'
     , '트랜스미션'
     , 25
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'TRANSMISSION_TEST');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'DS_TEST' key
     , 'dsTest' value
     , 'button' type
     , false required
     , '접속 테스트'
     , '시놀로지'
     , 34
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'DS_TEST');

INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
SELECT 'TELEGRAM_TEST' key
     , 'telegramTest' value
     , 'button' type
     , false required
     , '접속 테스트'
     , '텔레그램'
     , 13
     , CURRENT_TIMESTAMP create_dt
) x
WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'TELEGRAM_TEST');

-- INSERT INTO SETTING(key, value, type, required, label, group_label, order_id, create_dt) SELECT * FROM (
-- SELECT 'CORS_URL' key
--      , '' value
--      , 'text' type
--      , true required
--      , 'CORS 허용 URL'
--      , '일반'
--      , 6
--      , CURRENT_TIMESTAMP create_dt
-- ) x
-- WHERE NOT EXISTS(SELECT * FROM SETTING WHERE key = 'CORS_URL');

DELETE FROM USER WHERE id = 1 AND username = 'torrssen';

ALTER TABLE IF EXISTS RSS_FEED ALTER COLUMN LINK VARCHAR(2048);

UPDATE RSS_LIST SET tv_series = true WHERE tv_series IS NULL;

UPDATE SETTING SET order_id = 1 WHERE key = 'DARK_THEME';
UPDATE SETTING SET order_id = 2 WHERE key = 'DOWNLOAD_APP';
UPDATE SETTING SET order_id = 3 WHERE key = 'SEASON_PREFIX';
UPDATE SETTING SET order_id = 4 WHERE key = 'USE_CRON';
UPDATE SETTING SET order_id = 5, type='text' WHERE key = 'CRON_EXR';

UPDATE SETTING SET order_id = 6, group_label = '다운로드' WHERE key = 'DOWNLOAD_CHECK_INTERVAL';
-- UPDATE SETTING SET order_id = 7, group_label = '다운로드' WHERE key = 'EMBEDDED_LIMIT';
UPDATE SETTING SET order_id = 7, group_label = '다운로드' WHERE key = 'DONE_DELETE';

UPDATE SETTING SET order_id = 20 WHERE key = 'TRANSMISSION_HOST';
UPDATE SETTING SET order_id = 21 WHERE key = 'TRANSMISSION_PORT';
UPDATE SETTING SET order_id = 22 WHERE key = 'TRANSMISSION_CALLBACK';
UPDATE SETTING SET order_id = 23 WHERE key = 'TRANSMISSION_USERNAME';
UPDATE SETTING SET order_id = 24 WHERE key = 'TRANSMISSION_PASSWORD';

UPDATE SETTING SET group_label = '시놀로지' WHERE group_label = '다운로드 스테이션';
DELETE FROM SETTING WHERE key = 'CORS_URL';
DELETE FROM SETTING WHERE key = 'EMBEDDED_LIMIT';
UPDATE RSS_FEED SET rss_poster = 'http://t1.daumcdn.net/contentshub/sdb/bef6099c0dd6098dc2b5329ead5e0954787466043d1eaa1b2644f3ae886201c8' where rss_poster = 'http://t1.daumcdn.net/movie/265872b8652b4c8f8ca006653cd153ab1560756176069';
UPDATE WATCH_LIST SET subtitle = false WHERE subtitle IS NULL;
UPDATE WATCH_LIST SET series = true WHERE series IS NULL;
UPDATE SEEN_LIST SET subtitle = false WHERE subtitle IS NULL;
UPDATE SEEN_LIST SET rename_status = 'N/A' WHERE rename_status IS NULL;
UPDATE RSS_LIST SET download_all = false WHERE download_all IS NULL;

ALTER TABLE IF EXISTS DOWNLOAD_LIST ALTER COLUMN URI VARCHAR(2048);