package com.tarpha.torrssen2.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.tarpha.torrssen2.domain.RssFeed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RssFeedRepository extends JpaRepository<RssFeed, Long> {

    public Page<RssFeed> findAll(Pageable pageable);

    public Page<RssFeed> findByTitleContaining(String title, Pageable pageable);

    public Page<RssFeed> findByRssSiteIn(List<String> rssSite, Pageable pageable);

    public Page<RssFeed> findByTitleContainingAndRssSiteIn(String title, List<String> rssSite, Pageable pageable);

    public Optional<RssFeed> findByLink(String link);

    @Transactional
    public void deleteByRssSite(String rssSite);

    @Query(
        value = "SELECT * " +
                "FROM   RSS_FEED " +
                "WHERE  REGEXP_LIKE(title, ?1, 'i') " +
                "LIMIT 5"
      , nativeQuery = true)
    public List<RssFeed> testRegexTitle(String regex);

    @Query(
        value = "SELECT DISTINCT rss_site " +
                "FROM   RSS_FEED "
      , nativeQuery = true)
    public List<String> distinctRssSite();

    @Transactional
    @Modifying 
    @Query(
        value = "DELETE FROM RSS_FEED " +
                "WHERE create_dt < (SELECT create_dt " +
                "                   FROM   RSS_FEED " +
                "                   ORDER BY create_dt " +
                "                   LIMIT ?1, 1          )"
      , nativeQuery = true)
    public void deleteByLimitCount(int limitCount);

}