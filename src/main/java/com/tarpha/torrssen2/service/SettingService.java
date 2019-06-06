package com.tarpha.torrssen2.service;

import java.util.Optional;

import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingService {
    
    @Autowired
    private SettingRepository settingRepository;

    public String getDownloadApp() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_APP");
        if (optionalSetting.isPresent()) {
            return optionalSetting.get().getValue();
        }

        return null;
    }

    public String getSettingValue(String key) {
        Optional<Setting> setting = settingRepository.findByKey(key);
        if(setting.isPresent()) {
            return setting.get().getValue();
        }
        return null;
    }

}