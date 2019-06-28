package com.tarpha.torrssen2.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SettingRepository settingRepository;
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> urls = new ArrayList<>();
        urls.add("http://localhost:3000");
        Optional<Setting> setting = settingRepository.findByKey("CORS_URL");
        if(setting.isPresent()) {
            if(!StringUtils.isEmpty(setting.get().getValue())) {
                urls.add(setting.get().getValue());
            }
        }
        registry.addMapping("/**").allowedOrigins(StringUtils.join(urls, ","));
    }
}