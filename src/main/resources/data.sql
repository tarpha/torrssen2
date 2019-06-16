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
SELECT 'DOWNLOAD_APP' key
     , 'TRANSMISSION' value
     , 'app' type
     , true required
     , '기본 다운로드 앱'
     , '일반'
     , 3
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
UNION ALL
SELECT 'SEASON_PREFIX' key
     , 'SEASON ' value
     , 'text' type
     , true required
     , '시즌 폴더 접두사'
     , '일반'
     , 4
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

INSERT INTO USER(id, username, password) SELECT * FROM (
SELECT 1
     , 'torrssen'
     , '$2a$11$kEot4JlD/rNkkgBxWcCEzeOxOYtN7RGm87eCPkwS0wwJIsbVj.jhy'
) x
WHERE NOT EXISTS(SELECT * FROM USER WHERE username = 'torrssen');