package com.tarpha.torrssen2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.service.TelegramService;
import com.tarpha.torrssen2.service.TransmissionService;
import com.tarpha.torrssen2.util.CommonUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/api/transmission/")
// @CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "*")
@Api
public class TransmissionController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private TelegramService telegramService;

    @PostMapping(value = "/download-done")
    public int downloadDone(@RequestBody DownloadList downloadList) throws Exception {
        int ret = 0;
        logger.debug("download-done");
        logger.debug(downloadList.toString());
        
        // 다운로드 정보를 가져온다.
        Optional<DownloadList> optionalInfo = downloadListRepository.findById(downloadList.getId());
        if (optionalInfo.isPresent()) {
            DownloadList info = optionalInfo.get();

            // 파일명을 변경한다.
            if (!StringUtils.isBlank(info.getRename())) {
                if(!CommonUtils.renameFile(
                    downloadList.getDownloadPath(), 
                    downloadList.getFileName(), 
                    info.getRename())) {
                    ret = -1;
                }
            }

            // Download List 완료 처리
            info.setDone(true);
            info.setFileName(downloadList.getFileName());
            info.setDownloadPath(downloadList.getDownloadPath());
            info.setIsFake(downloadList.getIsFake());
            info.setPercentDone(100);

            //Telegram Message를 발송한다.
            Optional<Setting> optionalSetting = settingRepository.findByKey("SEND_TELEGRAM");
            if (optionalSetting.isPresent()) {
                if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                    logger.info("Send Telegram: " + info.getName());
                    if(telegramService.sendMessage("<b>" + info.getName() + "</b>의 다운로드가 완료되었습니다.")) {
                        info.setIsSentAlert(true);
                    }

                }
            }

            downloadListRepository.save(info);
        }

        // 완료 시 삭제 여부
        Optional<Setting> optionalSetting = settingRepository.findByKey("DONE_DELETE");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                Thread.sleep(10000);
                logger.info("Remove Torrent: " + downloadList.getFileName());
                
                List<Long> ids = new ArrayList<Long>();
                ids.add(downloadList.getId());
                transmissionService.torrentRemove(ids);
            }
        }

        logger.debug("removeDirectory");
        List<String> inners = CommonUtils.removeDirectory(downloadList.getDownloadPath(), downloadList.getFileName(), settingRepository);
        
        if (!StringUtils.isBlank(downloadList.getRename())) {
            logger.debug("getRename: " + downloadList.getRename());
            if(inners == null) {
                CommonUtils.renameFile(downloadList.getDownloadPath(), downloadList.getFileName(), downloadList.getRename());
            } else {
                for(String name: inners) {
                    if(StringUtils.contains(downloadList.getFileName(), name)) {
                        CommonUtils.renameFile(downloadList.getDownloadPath(), name, downloadList.getRename());
                    }
                }
            }
        }

        return ret;
    }
    
}