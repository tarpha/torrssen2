package com.tarpha.torrssen2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private TelegramService telegramService;

    public void runTask() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_APP");
        if (optionalSetting.isPresent()) {
            if(StringUtils.equals(optionalSetting.get().getValue(), "TRANSMISSION")) {
                transmissionJob();
            } else if(StringUtils.equals(optionalSetting.get().getValue(), "DOWNLOAD_STATION")) {
                downloadStationJob();
            }
        }
    }

    public void transmissionJob() {
        // 완료 시 삭제 여부
        Optional<Setting> optionalSetting = settingRepository.findByKey("DONE_DELETE");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                logger.info("=== Transmission Stop Seeding ===");
                List<DownloadList> list = transmissionService.torrentGet(null);
                List<Long> ids = new ArrayList<Long>();
                for (DownloadList down: list) {
                    // SEED: 6  # Seeding
                    if(down.getStatus() == 6) {
                        ids.add(down.getId());
                    }
                }

                if(ids.size() > 0) {
                    transmissionService.torrentRemove(ids);
                }
            }
        }
    }
            
    public void downloadStationJob() {
        // 다운로드 스테이션 완료 체크 
        logger.info("=== Download Station Check Done ===");
        List<DownloadList> list = downloadStationService.list();
        
        for(DownloadList down: list) {
            for(DownloadList tdown: downloadListRepository.findAllById(0L)) {
                if(StringUtils.equals(down.getUri(), tdown.getUri())) {
                    downloadListRepository.save(down);
                }
            }

            if(down.getId() > 0) {
                downloadListRepository.save(down);
            }

            if(down.getDone()) {
                Optional<DownloadList> rdown = downloadListRepository.findById(down.getId());
                if(rdown.isPresent()) {
                    if(rdown.get().getIsSentAlert() == false) {
                        //Telegram Message를 발송한다.
                        Optional<Setting> optionalSetting = settingRepository.findByKey("SEND_TELEGRAM");
                        if (optionalSetting.isPresent()) {
                            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                                logger.info("Send Telegram: " + down.getFileName());
                                if(telegramService.sendMessage("<b>" + down.getFileName() + "</b>의 다운로드가 완료되었습니다.")) {
                                    DownloadList sdown = rdown.get();
                                    sdown.setIsSentAlert(true);

                                    downloadListRepository.save(sdown);
                                }
                            }
                        }
                    }

                    // 완료 시 삭제 여부
                    Optional<Setting> optionalSetting = settingRepository.findByKey("DONE_DELETE");
                    if (optionalSetting.isPresent()) {
                        if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                            logger.info("Remove Torrent: " + down.getFileName());
                            
                            List<String> ids = new ArrayList<String>();
                            ids.add(rdown.get().getDbid());
                            downloadStationService.delete(ids);
                        }
                    }
                }
            }
        }
    }
           
}