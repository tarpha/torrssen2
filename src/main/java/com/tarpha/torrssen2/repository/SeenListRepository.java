package com.tarpha.torrssen2.repository;

import java.util.Optional;

import com.tarpha.torrssen2.domain.SeenList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SeenListRepository extends JpaRepository<SeenList, Long> {

    @Query(
        value = "SELECT count(1) cnt " + 
                "FROM   SEEN_LIST " +
                "WHERE  link = ?1 " +
                "AND    subtitle = ?5 " +
                "OR    (title = ?2 AND " + 
                "       season = ?3 AND " + 
                "       episode = ?4)"
      , nativeQuery = true)
    public int countByParams(String link, String title, String season, String episode, boolean subtitle);

    public Optional<SeenList> findFirstByLinkAndSubtitle(String link, boolean subtitle);
}