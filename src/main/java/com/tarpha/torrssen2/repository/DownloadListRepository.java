package com.tarpha.torrssen2.repository;

import java.util.List;

import com.tarpha.torrssen2.domain.DownloadList;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadListRepository extends JpaRepository<DownloadList, Long> {

    public List<DownloadList> findAllById(Long id);
    
}