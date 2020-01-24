package com.tarpha.torrssen2.repository;

import java.util.Optional;

import com.tarpha.torrssen2.domain.SeenList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SeenListRepository extends JpaRepository<SeenList, Long> {

    @Query(
        value = "SELECT count(1) cnt " + 
                "FROM   SEEN_LIST " +
                "WHERE  link = ?1 " +
                "AND    subtitle = ?5 " +
                "OR    (title = ?2 AND " + 
                "       season = ?3 AND " + 
                "       episode = ?4) "
      , nativeQuery = true)
    public int countByParams(String link, String title, String season, String episode, boolean subtitle);

    @Query(
        value = "SELECT count(1) cnt " + 
                "FROM   SEEN_LIST " +
                "WHERE  link = ?1 " +
                "AND    subtitle = ?5 " +
                "OR    (title = ?2 AND " + 
                "       season = ?3 AND " + 
                "       episode = ?4 AND" +
                "       quality = ?6)"
      , nativeQuery = true)
    public int countByParams(String link, String title, String season, String episode, boolean subtitle, String quality);

    public Optional<SeenList> findFirstByLinkAndSubtitle(String link, boolean subtitle);

    public Optional<SeenList> findFirstByLink(String link);

    public int deleteByTitle(String title);

    @Transactional
    @Modifying 
    @Query(
        value = "DELETE FROM SEEN_LIST A " +
                "WHERE NOT EXISTS (SELECT 1 " +
                "                  FROM   WATCH_LIST B " + 
                "                  WHERE A.title = B.title) "
      , nativeQuery = true)
    public void adjustList();

    // public Optional<SeenList> findFirstByTitleAndSeasonAndEpisodeAndQuality(String title, String season, String episode, String quality);

}