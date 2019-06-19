package com.tarpha.torrssen2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.DownloadPath;
import com.tarpha.torrssen2.domain.RssList;
import com.tarpha.torrssen2.domain.SeenList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.domain.WatchList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.DownloadPathRepository;
import com.tarpha.torrssen2.repository.RssListRepository;
import com.tarpha.torrssen2.repository.SeenListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.repository.WatchListRepository;
import com.tarpha.torrssen2.service.CryptoService;
import com.tarpha.torrssen2.service.RssLoadService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
// import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(value = "/api/setting/")
@Api
public class SettingController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private DownloadPathRepository downloadPathRepository;

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private WatchListRepository watchListRepository;

    @Autowired
    private SeenListRepository seenListRepository;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private RssLoadService rssLoadService;

    @Autowired
    private CryptoService cryptoService;

    // @CrossOrigin("*")
    @GetMapping(value = "/list")
    public List<Setting> list(Sort sort) {
        return settingRepository.findAll(sort);
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/download-path")
    public List<DownloadPath> downloadPath() {
        return downloadPathRepository.findAll();
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/download-path/compute")
    public List<DownloadPath> computedDownloadPath(@RequestParam("title") String title, @RequestParam("season") String season) {
        List<DownloadPath> pathList = downloadPathRepository.findAll();
        Optional<Setting> seasonPrefix = settingRepository.findByKey("SEASON_PREFIX");

        for(DownloadPath path: pathList) {
            if(path.getUseTitle()) {
                path.setPath(path.getPath() + "/" + title);
            }
            if(path.getUseSeason() && seasonPrefix.isPresent()) {
                path.setPath(path.getPath() + "/" + seasonPrefix.get().getValue() + season);
            }
        }

        return pathList;
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/{key}")
    public String value(@PathVariable("key") String key) {
        Optional<Setting> setting = settingRepository.findByKey(key);

        if(setting.isPresent()) {
            return setting.get().getValue();
        }

        return null;
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/save")
    public void postMethodName(@RequestBody List<Setting> settings) throws Exception {
        for(Setting setting: settings) {
            if(StringUtils.equals(setting.getType(), "password") && !StringUtils.isEmpty(setting.getValue())) {
                setting.setValue(cryptoService.encrypt(setting.getValue()));
            }
        }

        settingRepository.saveAll(settings);
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/rss-list")
    public List<RssList> getRssList() {
        return rssListRepository.findAll();
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/rss-list")
    public void setRssList(@RequestBody List<RssList> list) {
        List<RssList> deleteList = new ArrayList<>();
       
        for(RssList ori: rssListRepository.findAll()) {
            boolean delete = true;
            for(RssList rss: list) {
                if(StringUtils.equals(ori.getName(), rss.getName())) {
                    delete = false;
                    break;
                }
            }

            if(delete) {
                deleteList.add(ori);
            }
        }

        if(deleteList.size() > 0) {
            rssListRepository.deleteAll(deleteList);
        }
        
        rssListRepository.saveAll(list);
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/path")
    public List<DownloadPath> getPathList() {
        return downloadPathRepository.findAll();
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/path")
    public void setPathList(@RequestBody List<DownloadPath> list) {
        List<DownloadPath> deleteList = new ArrayList<>();
       
        for(DownloadPath ori: downloadPathRepository.findAll()) {
            boolean delete = true;
            for(DownloadPath rss: list) {
                if(StringUtils.equals(ori.getName(), rss.getName())) {
                    delete = false;
                    break;
                }
            }

            if(delete) {
                deleteList.add(ori);
            }
        }

        if(deleteList.size() > 0) {
            downloadPathRepository.deleteAll(deleteList);
        }
        
        downloadPathRepository.saveAll(list);
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/watch-list")
    public List<WatchList> getWatchList(Sort sort) {
        return watchListRepository.findAll(sort);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/watch-list")
    public void setWatchList(@RequestBody WatchList watchList) {
        watchListRepository.save(watchList);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/watch-list/execute")
    public void executeWatchList() {
        rssLoadService.checkWatchListFromDb();
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/watch-list/delete")
    public void deleteWatchList(@RequestBody WatchList watchList) {
        watchListRepository.delete(watchList);
    }
    
    // @CrossOrigin("*")
    @GetMapping(value = "/seen-list")
    public List<SeenList> getSeenList(Sort sort) {
        return seenListRepository.findAll(sort);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/seen-list/delete")
    public void deleteSeenList(@RequestBody SeenList seenList) {
        seenListRepository.delete(seenList);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/seen-list/delete/all")
    public void deleteAllSeenList() {
        seenListRepository.deleteAll();
    }

    // @CrossOrigin("*")
    @GetMapping(value = "/download-list")
    public List<DownloadList> getDownloadList(Sort sort) {
        return downloadListRepository.findAll(sort);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/download-list/delete")
    public void deleteDownloadList(@RequestBody DownloadList downloadList) {
        downloadListRepository.delete(downloadList);
    }

    // @CrossOrigin("*")
    @PostMapping(value = "/download-list/delete/all")
    public void deleteAllDownloadList() {
        downloadListRepository.deleteAll();
    }
}