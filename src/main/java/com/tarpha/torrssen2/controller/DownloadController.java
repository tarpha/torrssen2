package com.tarpha.torrssen2.controller;

import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.service.DownloadService;
import com.tarpha.torrssen2.service.SettingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
// @CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "*")
@Api
public class DownloadController {
    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private DownloadService downloadService;

    @GetMapping(value = "/app")
    public String getApp() {
        return settingService.getDownloadApp();
    }

    @GetMapping(value = "/id/{id}")
    public DownloadList getDownload(@PathVariable("id") long id) {
        return downloadService.getInfo(id);
    }

    @PostMapping(value = "/create")
    public long create(@RequestBody DownloadList download) {
        return downloadService.create(download);
    }

    @PostMapping(value = "/remove")
    public int remove(@RequestBody DownloadList download) {
        return downloadService.remove(download);
    }

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

    @GetMapping(value = "/conn-test")
    public boolean connTest(@RequestParam("app") String app, @RequestParam("host") String host,
        @RequestParam("port") String port, @RequestParam("id") String id, @RequestParam("pwd") String pwd) {
        return downloadService.connTest(app, host, port, id, pwd);
    }

}