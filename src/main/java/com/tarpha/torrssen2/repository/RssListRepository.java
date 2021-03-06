package com.tarpha.torrssen2.repository;

import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.RssList;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RssListRepository extends JpaRepository<RssList, Long> {

    public Optional<RssList> findByName(String name);

    public List<RssList> findByUseDb(boolean useDb);

    public List<RssList> findByUseDbAndInternal(boolean useDb, boolean internal);

    public List<RssList> findByShow(boolean show);

}