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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private SettingRepository settingRepository;
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> allowUrls = new ArrayList<>();
        allowUrls.add("http://localhost:3000");
        Optional<Setting> optionalSetting = settingRepository.findByKey("CORS_URL");
        if(optionalSetting.isPresent()) {
            if(!StringUtils.isEmpty(optionalSetting.get().getValue())) {
                allowUrls.add(optionalSetting.get().getValue());
            }
        }
        registry.addMapping("/**").allowedOrigins(StringUtils.join(allowUrls, ","));
    }
}