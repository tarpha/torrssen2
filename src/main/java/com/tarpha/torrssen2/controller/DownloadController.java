package com.tarpha.torrssen2.controller;

import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.service.DownloadService;
import com.tarpha.torrssen2.service.SettingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private SettingService settingService;

    @Autowired
    private DownloadService downloadService;

    // @CrossOrigin("*")
    @GetMapping(value = "/app")
    public String getApp() {
        return settingService.getDownloadApp();
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/id/{id}")
    public DownloadList getDownload(@PathVariable("id") long id) {
        return downloadService.getInfo(id);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/create")
    public long create(@RequestBody DownloadList download) {
        return downloadService.create(download);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/remove")
    public int remove(@RequestBody DownloadList download) {
        return downloadService.remove(download);
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/magnet")
    public DownloadList downloadStatus(@RequestParam("magnet") String magnet) {
        Optional<DownloadList> download = downloadListRepository.findFirstByUriAndDoneOrderByCreateDtDesc(magnet, false);
        if(download.isPresent()) {
            if(getDownload(download.get().getId()) != null) {
                return download.get();
            }
        }
        return null;
    }
    
}