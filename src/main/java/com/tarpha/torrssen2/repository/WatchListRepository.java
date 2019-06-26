package com.tarpha.torrssen2.repository;

import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.WatchList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {

    public List<WatchList> findByUse(boolean use);

    @Query(value = "SELECT w.* FROM   WATCH_LIST w  " + "WHERE  w.use = true " + "AND  ((w.use_regex = false AND "
            + "       REPLACE(UPPER(?1), ' ', '') LIKE "
            + "       REPLACE(UPPER(CONCAT('%', w.title, '%', IFNULL(w.release_group, ''), '%')), ' ', '')) OR "
            + "      (w.use_regex = true  AND REGEXP_LIKE(?1, w.title, 'i')) AND REPLACE(UPPER(?1), ' ', '') LIKE "
            + "       REPLACE(UPPER(CONCAT('%', IFNULL(w.release_group, ''), '%')), ' ', '')) "
            + "AND  ((IFNULL(w.quality, '100P+') LIKE '%+' AND "
            + "       CAST(REPLACE(UPPER(IFNULL(?2, '')), 'P', '') AS INTEGER) " 
            + "       >= CAST(REPLACE(UPPER(IFNULL(w.quality, '100P+')), 'P+', '' ) AS INTEGER)) OR "
            + "      (UPPER(w.quality) LIKE '%P' AND "
            + "       UPPER(IFNULL(?2, '')) =  UPPER(w.quality))) ", nativeQuery = true)
    public Optional<WatchList> findByTitleRegex(String title, String quality);

}