package com.tarpha.torrssen2.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.WatchList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.WatchListRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private WatchListRepository watchListRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private FileStationService fileStationService;

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

    public long create(DownloadList download) {
        long ret = 0L;

        String app = settingService.getDownloadApp();
        if(StringUtils.equals(app, "DOWNLOAD_STATION")) {
            String[] paths = StringUtils.split(download.getDownloadPath(), "/");

            if(paths.length > 1) {
                StringBuffer path = new StringBuffer();
                String name = null;
                for(int i = 0; i < paths.length; i++) {
                    if(i < paths.length -1) {
                        path.append("/" + paths[i]);
                    } else {
                        name = paths[i];
                    }
                }
                fileStationService.createFolder(path.toString(), name);
            }
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
}