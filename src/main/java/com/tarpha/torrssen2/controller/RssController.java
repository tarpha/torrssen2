package com.tarpha.torrssen2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.RssFeed;
import com.tarpha.torrssen2.domain.RssList;
import com.tarpha.torrssen2.domain.WatchList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.RssFeedRepository;
import com.tarpha.torrssen2.repository.RssListRepository;
import com.tarpha.torrssen2.repository.WatchListRepository;
import com.tarpha.torrssen2.service.DownloadService;
import com.tarpha.torrssen2.service.RssLoadService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/api/rss/")
// @CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "*")
@Api
public class RssController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RssFeedRepository rssFeedRepository;

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private WatchListRepository watchListRepository;

    @Autowired
    private RssLoadService rssLoadService;

    @Autowired
    private DownloadService downloadService;

    private void setInfo(RssFeed feed) {
        Optional<DownloadList> download = downloadListRepository.findFirstByUriAndDoneOrderByCreateDtDesc(feed.getLink(), false);
        if(download.isPresent()) {
            if(downloadService.getInfo(download.get().getId()) != null) {
                feed.setDownloadId(download.get().getId());
                feed.setDownloading(true);
            }
        }

        Optional<DownloadList> downloaded = downloadListRepository.findFirstByUriAndDoneOrderByCreateDtDesc(feed.getLink(), true);
        if(downloaded.isPresent()) {
            feed.setDownloaded(downloaded.get().getDone());
        }

        Optional<WatchList> optionalWatchList = watchListRepository.findByTitleRegex(feed.getTitle(), feed.getRssQuality());
        if(optionalWatchList.isPresent()) {
            feed.setWatch(true);
        }
    }

    private List<String> rssSite() {
        List<String> ret = new ArrayList<>();
        List<RssList> list = rssListRepository.findByShow(true);
        for(RssList rss: list) {
            if(rss.getShow()) {
                ret.add(rss.getName());
            }
        }
        return ret;
    }

    @GetMapping(value = "/feed/list")
    public Page<RssFeed> feedList(Pageable pageable) {
        Page<RssFeed> feedList = rssFeedRepository.findByRssSiteIn(rssSite(), pageable);
        for(RssFeed feed: feedList) {
            setInfo(feed);
        }
        return feedList;
    }

    @GetMapping(value = "/feed/search")
    public Page<RssFeed> searchList(@RequestParam("title") String title, Pageable pageable) {
        Page<RssFeed> feedList = rssFeedRepository.findByTitleContainingAndRssSiteIn(title, rssSite(), pageable);
        for(RssFeed feed: feedList) {
            setInfo(feed);
        }
        return feedList;
    }

    @PostMapping(value = "/feed/delete")
    public void deleteFeed() {
        rssFeedRepository.deleteAll();
    }

    @PostMapping(value = "/feed/delete/rss-site")
    public void deleteFeedByRssSite(@RequestBody RssList rssList) {
        rssFeedRepository.deleteByRssSite(rssList.getName());
    }

    @PostMapping(value = "/feed/delete/rss-site/list")
    public void deleteFeedByRssSiteList(@RequestBody List<String> rssSiteList) {
        for(String rssSite: rssSiteList) {
            rssFeedRepository.deleteByRssSite(rssSite);
        }
    }

    @PostMapping(value = "/reload")
    public String reLoad() {
        rssLoadService.asyncLoadRss();
        return "success";
    }

    @GetMapping(value = "/feed/regex/test")
    public List<RssFeed> regexTest(@RequestParam("title") String title) {
        return rssFeedRepository.testRegexTitle(title);
    }

    @GetMapping(value = "/rss-site/distinct")
    public List<String> distinctRssSite() {
        return rssFeedRepository.distinctRssSite();
    }
      
}