package com.tarpha.torrssen2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private TransmissionService transmissionService;

    public DownloadList getInfo(long id) {
        String app = settingService.getDownloadApp();
        if(StringUtils.equals(app, "DOWNLOAD_STATION")) {
            Optional<DownloadList> down = downloadListRepository.findById(id);
            if(down.isPresent()) {
                return downloadStationService.getInfo(down.get().getDbid());
            }
        } else if(StringUtils.equals(app, "TRANSMISSION")) {
            List<Long> ids = new ArrayList<Long>();
            ids.add(id);
            List<DownloadList> list = transmissionService.torrentGet(ids);
            if(list.size() > 0) {
                return list.get(0);
            }
        }

        return null;
    }
}