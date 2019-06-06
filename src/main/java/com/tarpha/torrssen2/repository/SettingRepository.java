package com.tarpha.torrssen2.repository;

import java.util.Optional;

import com.tarpha.torrssen2.domain.Setting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {

    public Optional<Setting> findByKey(String key);

}