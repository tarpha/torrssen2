package com.tarpha.torrssen2.controller;

// import java.util.ArrayList;
import java.util.List;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.service.DownloadService;
// import com.tarpha.torrssen2.service.DownloadStationService;
// import com.tarpha.torrssen2.service.SettingService;
// import com.tarpha.torrssen2.service.TransmissionService;

// import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    // @Autowired
    // private SettingService settingService;

    @Autowired
    private DownloadService downloadService;

    // @Autowired
    // private DownloadStationService downloadStationService;

    // @Autowired
    // private TransmissionService transmissionService;

    @MessageMapping("/rate/{sid}")
    public DownloadList downloadRate(@DestinationVariable String sid) throws Exception {
        // DownloadList ret = null;
        // Long id = Long.valueOf(sid);

        // logger.debug("id:" + sid);

        // String app = settingService.getDownloadApp();
        // if (StringUtils.equals(app, "DOWNLOAD_STATION")) {
        //     ret = downloadStationService.getInfo(downloadStationService.getDbId(id));
        // } else if (StringUtils.equals(app, "TRANSMISSION")) {
        //     List<Long> ids = new ArrayList<>();
        //     ids.add(id);
        //     List<DownloadList> list = transmissionService.torrentGet(ids);

        //     if (list.size() > 0) {
        //         ret = list.get(0);
        //     }
        // }

        // return ret;
        return downloadService.getInfo(Long.valueOf(sid));
    }

    @MessageMapping("/rate/list")
    public List<DownloadList> downloadRateList() {
        // List<DownloadList> ret = null;
        // logger.debug("downloadList");

        // String app = settingService.getDownloadApp();
        // if (StringUtils.equals(app, "DOWNLOAD_STATION")) {
        //     ret = downloadStationService.list();
        // } else if (StringUtils.equals(app, "TRANSMISSION")) {
        //     ret = transmissionService.torrentGet(null);
        // }

        // return ret;
        return downloadService.list();
    }

    @MessageMapping("/remove")
    public DownloadList remove(DownloadList download) {
        // boolean ret = false;

        // String app = settingService.getDownloadApp();
        // if (StringUtils.equals(app, "DOWNLOAD_STATION")) {
        //     List<String> ids = new ArrayList<>();
        //     ids.add(downloadStationService.getDbId(download.getId()));
        //     ret = downloadStationService.delete(ids);
        // } else if (StringUtils.equals(app, "TRANSMISSION")) {
        //     List<Long> ids = new ArrayList<>();
        //     ids.add(download.getId());
        //     ret = transmissionService.torrentRemove(ids);
        // }

        // if (ret) {
        if (downloadService.remove(download) > 0) {
            return download;
        } else {
            return null;
        }
    }
    
}