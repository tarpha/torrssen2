package com.tarpha.torrssen2.controller;

import java.util.List;

import com.tarpha.torrssen2.domain.RssFeed;
import com.tarpha.torrssen2.repository.RssFeedRepository;
import com.tarpha.torrssen2.service.RssLoadService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/api/rss/")
@Api
public class RssController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RssFeedRepository rssFeedRepository;

    @Autowired
    private RssLoadService rssLoadService;

    @CrossOrigin("*")
    @GetMapping(value = "/feed/list")
    public Page<RssFeed> feedList(Pageable pageable) {
        return rssFeedRepository.findAll(pageable);
    }

    @CrossOrigin("*")
    @GetMapping(value = "/feed/search")
    public Page<RssFeed> searchList(@RequestParam("title") String title, Pageable pageable) {
        return rssFeedRepository.findByTitleContaining(title, pageable);
    }

    @CrossOrigin("*")
    @PostMapping(value = "/feed/delete")
    public void deleteFeed() {
        rssFeedRepository.deleteAll();
    }

    @CrossOrigin("*")
    @PostMapping(value = "/reload")
    public String reLoad() {
        rssLoadService.asyncLoadRss();
        return "success";
    }

    @CrossOrigin("*")
    @GetMapping(value = "/feed/regex/test")
    public List<RssFeed> regexTest(@RequestParam("title") String title) {
        return rssFeedRepository.testRegexTitle(title);
    }
      
}