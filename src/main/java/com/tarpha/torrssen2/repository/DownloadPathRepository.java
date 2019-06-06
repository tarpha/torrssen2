package com.tarpha.torrssen2.repository;

import java.util.List;

import com.tarpha.torrssen2.domain.DownloadPath;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DownloadPathRepository extends JpaRepository<DownloadPath, Long> {

    @Query(
        value = "SELECT name " +
                "     , use_title " +
                "     , use_season " + 
                "     , create_dt " + 
                "     , CONCAT(path " + 
                "            , CASE use_title WHEN true THEN CONCAT('/', ?1) ELSE '' END " +
                "            , CASE use_season WHEN true THEN CONCAT('/' " + 
                "               , (SELECT value FROM SETTING WHERE key = 'SEASON_PREFIX') " + 
                "               , ?2) ELSE '' END) path " +
                "FROM   DOWNLOAD_PATH "
      , nativeQuery = true)
    public List<DownloadPath> findByParams(String title, String season);

    @Query(
        value = "SELECT CONCAT(path " + 
                "            , CASE use_title WHEN true THEN CONCAT('/', ?2) ELSE '' END " +
                "            , CASE use_season WHEN true THEN CONCAT('/' " + 
                "               , (SELECT value FROM SETTING WHERE key = 'SEASON_PREFIX') " + 
                "               , ?3) ELSE '' END) path " +
                "FROM   DOWNLOAD_PATH " +
                "WHERE  name = ?1"
      , nativeQuery = true)
    public String computedPath(String name, String title, String season);

}