package com.tarpha.torrssen2.controller;

import java.util.List;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.service.DownloadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @Autowired
    private DownloadService downloadService;

    @MessageMapping("/rate/{sid}")
    public DownloadList downloadRate(@DestinationVariable String sid) throws Exception {
        return downloadService.getInfo(Long.valueOf(sid));
    }

    @MessageMapping("/rate/list")
    public List<DownloadList> downloadRateList() {
        return downloadService.list();
    }

    @MessageMapping("/remove")
    public DownloadList remove(DownloadList download) {
        if (downloadService.remove(download) >= 0) {
            return download;
        } else {
            return null;
        }
    }
    
}