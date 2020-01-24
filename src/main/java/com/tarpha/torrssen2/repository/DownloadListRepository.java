package com.tarpha.torrssen2.repository;

import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface DownloadListRepository extends JpaRepository<DownloadList, Long> {

    public List<DownloadList> findAllById(Long id);

    public List<DownloadList> findByTask(Boolean task);

    public Optional<DownloadList> findByDbid(String dbid);

    public Optional<DownloadList> findFirstByUriAndDoneOrderByCreateDtDesc(String uri, Boolean done);
    
    public Optional<DownloadList> findTopByOrderByIdDesc();

    @Transactional
    @Modifying 
    @Query(
        value = "DELETE FROM DOWNLOAD_LIST " +
                "WHERE create_dt < (SELECT create_dt " +
                "                   FROM   DOWNLOAD_LIST " +
                "                   ORDER BY create_dt " +
                "                   LIMIT ?1, 1          )"
      , nativeQuery = true)
    public void deleteByLimitCount(int limitCount);
}