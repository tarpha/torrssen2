package com.tarpha.torrssen2.config;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.service.RssLoadService;
import com.tarpha.torrssen2.service.SchedulerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RssLoadService rssLoadService;

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    SettingRepository settingRepository;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(10);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();
        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

        scheduledTaskRegistrar.addTriggerTask(() -> rssLoadService.loadRss(), t -> {
            Calendar nextExecutionTime = new GregorianCalendar();
            Date lastActualExecutionTime = t.lastActualExecutionTime();
            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
            Optional<Setting> optionalSetting = settingRepository.findByKey("RSS_LOAD_INTERVAL");
            if (optionalSetting.isPresent()) {
                nextExecutionTime.add(Calendar.MINUTE, Integer.parseInt(optionalSetting.get().getValue()));
            }
            return nextExecutionTime.getTime();
        });

        scheduledTaskRegistrar.addTriggerTask(() -> schedulerService.runTask(), t -> {
            Calendar nextExecutionTime = new GregorianCalendar();
            Date lastActualExecutionTime = t.lastActualExecutionTime();
            nextExecutionTime.setTime(lastActualExecutionTime != null ? lastActualExecutionTime : new Date());
            Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_CHECK_INTERVAL");
            if (optionalSetting.isPresent()) {
                nextExecutionTime.add(Calendar.MINUTE, Integer.parseInt(optionalSetting.get().getValue()));
            }
            return nextExecutionTime.getTime();
        });

        scheduledTaskRegistrar.addTriggerTask(() -> schedulerService.killTask(), t -> {
            String cronExp = "0/5 * * * * ?";
            Optional<Setting> optionalSetting = settingRepository.findByKey("CRON_EXR");
            if (optionalSetting.isPresent()) {
                cronExp = optionalSetting.get().getValue();
            }
            return new CronTrigger(cronExp).nextExecutionTime(t);
        });
    }

}