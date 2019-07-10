package com.tarpha.torrssen2.repository;

import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadListRepository extends JpaRepository<DownloadList, Long> {

    public List<DownloadList> findAllById(Long id);

    public List<DownloadList> findByTask(Boolean task);

    public Optional<DownloadList> findByDbid(String dbid);

    public Optional<DownloadList> findFirstByUriAndDoneOrderByCreateDtDesc(String uri, Boolean done);
    
    public Optional<DownloadList> findTopByOrderByIdDesc();
}