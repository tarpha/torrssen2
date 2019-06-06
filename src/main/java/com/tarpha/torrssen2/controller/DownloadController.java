package com.tarpha.torrssen2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.WatchList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.WatchListRepository;
import com.tarpha.torrssen2.service.DownloadStationService;
import com.tarpha.torrssen2.service.SettingService;
import com.tarpha.torrssen2.service.TransmissionService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/api/download/")
@Api
public class DownloadController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private WatchListRepository watchListRepository;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private SettingService settingService;

    @CrossOrigin("*")
    @GetMapping(value = "/app")
    public String getApp() {
        return settingService.getDownloadApp();
    }

    @CrossOrigin("*")
    @GetMapping(value = "/id/{id}")
    public DownloadList feedList(@PathVariable("id") long id) {
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

    @CrossOrigin("*")
    @PostMapping(value = "/create")
    public long create(@RequestBody DownloadList download) {
        long ret = 0L;

        String app = settingService.getDownloadApp();
        if(StringUtils.equals(app, "DOWNLOAD_STATION")) {
            if(downloadStationService.create(download.getUri(), download.getDownloadPath())) {
                for(DownloadList down: downloadStationService.list()) {
                    if(StringUtils.equals(download.getUri(), down.getUri())) {
                        ret = down.getId();
                        download.setDbid(down.getDbid());
                    }
                }
            }
        } else if(StringUtils.equals(app, "TRANSMISSION")) {
            ret = (long)transmissionService.torrentAdd(download.getUri(), download.getDownloadPath());
        }

        if(ret > 0L) {
            download.setId(ret);
            downloadListRepository.save(download);
        }

        if(download.getAuto()) {
            WatchList watchList = new WatchList();
            watchList.setTitle(download.getRssTitle());
            watchList.setDownloadPath(download.getDownloadPath());
            watchList.setReleaseGroup(download.getRssReleaseGroup());

            watchListRepository.save(watchList);
        }

        return ret;
    }

    @CrossOrigin("*")
    @PostMapping(value = "/remove")
    public boolean remove(@RequestBody DownloadList download) {
        boolean ret = false;

        String app = settingService.getDownloadApp();
        if(StringUtils.equals(app, "DOWNLOAD_STATION")) {
            List<String> ids = new ArrayList<>();
            ids.add(downloadStationService.getDbId(download.getId()));
            ret = downloadStationService.delete(ids);
        } else if(StringUtils.equals(app, "TRANSMISSION")) {
            List<Long> ids = new ArrayList<>();
            ids.add(download.getId());
            ret = transmissionService.torrentRemove(ids);
        }

        return ret;
    }
    
}