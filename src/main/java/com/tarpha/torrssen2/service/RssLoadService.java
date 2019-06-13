package com.tarpha.torrssen2.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.RssFeed;
import com.tarpha.torrssen2.domain.RssList;
import com.tarpha.torrssen2.domain.SeenList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.domain.WatchList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.DownloadPathRepository;
import com.tarpha.torrssen2.repository.RssFeedRepository;
import com.tarpha.torrssen2.repository.RssListRepository;
import com.tarpha.torrssen2.repository.SeenListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.repository.WatchListRepository;
import com.tarpha.torrssen2.util.CommonUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RssLoadService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private RssFeedRepository rssFeedRepository;

    @Autowired
    private WatchListRepository watchListRepository;

    @Autowired
    private SeenListRepository seenListRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private DownloadPathRepository downloadPathRepository;

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private DaumMovieTvService daumMovieTvService;
    
    public void loadRss() {
        logger.info("=== Load RSS ===");

        deleteFeed();

        List<RssFeed> rssFeedList = new ArrayList<RssFeed>();

        for (RssList rss : rssListRepository.findByUseDb(true)) {
            try {
                URL feedSource = new URL(rss.getUrl());
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feedList = input.build(new XmlReader(feedSource));

                // for (SyndEntry feed : feedList.getEntries()) {
                for(int i = feedList.getEntries().size() -1; i >= 0; i--) {
                    SyndEntry feed = feedList.getEntries().get(i);
                    RssFeed rssFeed = new RssFeed();

                    if (!rssFeedRepository.findByLink(rssFeed.getLinkByKey(rss.getLinkKey(), feed)).isPresent()) {
                        rssFeed.setTitle(feed.getTitle());
                        // rssFeed.setDesc(feed.getDescription().getValue());
                        rssFeed.setRssSite(rss.getName());

                        rssFeed.setRssTitleByTitle(feed.getTitle());
                        rssFeed.setRssEpisodeByTitle(feed.getTitle());
                        rssFeed.setRssSeasonByTitle(feed.getTitle());
                        rssFeed.setRssQualityBytitle(feed.getTitle());
                        rssFeed.setRssReleaseGroupByTitle(feed.getTitle());
                        rssFeed.setRssDateBytitle(feed.getTitle());
                        rssFeed.setLinkByKey(rss.getLinkKey(), feed);
                        rssFeed.setRssPoster(daumMovieTvService.getPoster(rssFeed.getRssTitle()));

                        rssFeedList.add(rssFeed);

                        logger.info("Add Feed: " + rssFeed.getTitle());

                        // Watch List를 체크하여 다운로드 요청한다.
                        checkWatchList(rssFeed);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        rssFeedRepository.saveAll(rssFeedList);
    }

    public void checkWatchListFromDb() {
        for(RssFeed rssFeed: rssFeedRepository.findAll()) {
            checkWatchList(rssFeed);
        }
    }

    @Async
    public void asyncLoadRss() {
        loadRss();
    }

    private void checkWatchList(RssFeed rssFeed) {
        Optional<WatchList> optionalWatchList = watchListRepository.findByTitleRegex(rssFeed.getTitle(),
                rssFeed.getRssQuality());

        if (optionalWatchList.isPresent()) {
            WatchList watchList = optionalWatchList.get();
            logger.info("Matched Feed: " + rssFeed.getTitle());
            if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                    rssFeed.getRssEpisode()) > 0) {
                logger.info("Rejected by Seen: " + rssFeed.getTitle());
            } else {
                try{
                    int startSeason = Integer.parseInt(watchList.getStartSeason());
                    int endSeason = Integer.parseInt(watchList.getEndSeason());
                    int startEpisode = Integer.parseInt(watchList.getStartEpisode());
                    int endEpisode = Integer.parseInt(watchList.getEndEpisode());
                    int currSeason = Integer.parseInt(rssFeed.getRssSeason());
                    int currEpisode = Integer.parseInt(rssFeed.getRssEpisode());

                    if(currSeason < startSeason || currSeason > endSeason) {
                        logger.info("Rejected by Season: Start: " + startSeason + " End: " + endSeason + " Feed: " + currSeason);
                        return;
                    }
                    if(currEpisode < startEpisode || currEpisode > endEpisode) {
                        logger.info("Rejected by Episode: Start: " + startEpisode + " End: " + endEpisode + " Feed: " + currEpisode);
                        return;
                    }
                } catch (NumberFormatException e) {
                    logger.info(e.getMessage());
                }

                logger.info("Download Repuest: " + rssFeed.getTitle());

                String path = downloadPathRepository.computedPath(watchList.getDownloadPath(), rssFeed.getRssTitle(), rssFeed.getRssSeason());
                if(StringUtils.isBlank(path)) {
                    path = watchList.getDownloadPath();
                }

                Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_APP");
                if (optionalSetting.isPresent()) {
                    if(StringUtils.equals(optionalSetting.get().getValue(), "TRANSMISSION")) {
                        // Request Download to Transmission
                        int torrentAddedId = transmissionService.torrentAdd(rssFeed.getLink(), path);
                        logger.info("Transmission ID: " + torrentAddedId);

                        if (torrentAddedId > 0) {
                            // Add to Seen
                            addToSeenList(rssFeed, path);

                            // Add to Download List
                            addToDownloadList((long) torrentAddedId, rssFeed, watchList, path);
                        }
                    } else if(StringUtils.equals(optionalSetting.get().getValue(), "DOWNLOAD_STATION")) {
                        // Request Download to Download Station
                        if(downloadStationService.create(rssFeed.getLink(), path)) {
                            // Add to Seen
                            addToSeenList(rssFeed, path);

                            // Add to Download List
                            boolean isExist = false;
                            for(DownloadList down: downloadStationService.list()) {
                                if(StringUtils.equals(rssFeed.getLink(), down.getUri())) {
                                    isExist = true;
                                    downloadListRepository.save(down);
                                }
                            }

                            if(isExist == false) {
                                addToDownloadList(0L, rssFeed, watchList, path);
                            }
                        } 
                    }
                }
            }
        }
    }

    private void addToSeenList(RssFeed rssFeed, String path) {
        SeenList seenList = new SeenList();
        seenList.setTitle(rssFeed.getRssTitle());
        seenList.setLink(rssFeed.getLink());
        seenList.setDownloadPath(path);
        seenList.setSeason(rssFeed.getRssSeason());
        seenList.setEpisode(rssFeed.getRssEpisode());

        seenListRepository.save(seenList);
    }

    private void addToDownloadList(Long id, RssFeed rssFeed, WatchList watchList, String path) {
        DownloadList download = new DownloadList();
        download.setId(id);
        download.setName(rssFeed.getTitle());
        download.setUri(rssFeed.getLink());
        download.setDownloadPath(path);
        if(!StringUtils.isBlank(watchList.getRename())) {
            download.setRename(
                CommonUtils.getRename(watchList.getRename()
                    , rssFeed.getRssTitle()
                    , rssFeed.getRssSeason()
                    , rssFeed.getRssEpisode()
                    , rssFeed.getRssQuality()
                    , rssFeed.getRssReleaseGroup()));
        }

        downloadListRepository.save(download);
    }

    private void deleteFeed() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("USE_LIMIT");
        if(optionalSetting.isPresent()) {
            if(Boolean.parseBoolean(optionalSetting.get().getValue())) {
                Optional<Setting> limitCnt = settingRepository.findByKey("LIMIT_COUNT");
                if(limitCnt.isPresent()) {
                    rssFeedRepository.deleteByLimitCount(Integer.parseInt(limitCnt.get().getValue()));
                }
            }
        }
    }
    
}