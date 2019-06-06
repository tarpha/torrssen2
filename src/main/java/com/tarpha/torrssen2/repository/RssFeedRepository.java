package com.tarpha.torrssen2.repository;

import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.RssFeed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RssFeedRepository extends JpaRepository<RssFeed, Long> {

    public Page<RssFeed> findAll(Pageable pageable);
    public Page<RssFeed> findByTitleContaining(String title, Pageable pageable);
    public Optional<RssFeed> findByLink(String link);

    @Query(
        value = "SELECT * " +
                "FROM   RSS_FEED " +
                "WHERE  REGEXP_LIKE(title, ?1, 'i') " +
                "LIMIT 5"
      , nativeQuery = true)
    public List<RssFeed> testRegexTitle(String regex);

}