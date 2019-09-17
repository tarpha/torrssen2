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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RssLoadService {
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
    private HttpDownloadService httpDownloadService;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private BtService btService;

    @Autowired
    private DaumMovieTvService daumMovieTvService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void loadRss() {
        log.info("=== Load RSS ===");

        deleteFeed();

        List<RssFeed> rssFeedList = new ArrayList<RssFeed>();

        for (RssList rss : rssListRepository.findByUseDb(true)) {
            try {
                URL feedSource = new URL(rss.getUrl());
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feedList = input.build(new XmlReader(feedSource));

                // for (SyndEntry feed : feedList.getEntries()) {
                for (int i = feedList.getEntries().size() - 1; i >= 0; i--) {
                    SyndEntry feed = feedList.getEntries().get(i);
                    RssFeed rssFeed = new RssFeed();

                    if (!rssFeedRepository.findByLink(rssFeed.getLinkByKey(rss.getLinkKey(), feed)).isPresent()) {
                        rssFeed.setTitle(feed.getTitle());
                        rssFeed.setRssSite(rss.getName());
                        rssFeed.setTvSeries(rss.getTvSeries());

                        rssFeed.setRssTitleByTitle(feed.getTitle());
                        rssFeed.setRssEpisodeByTitle(feed.getTitle());
                        rssFeed.setRssSeasonByTitle(feed.getTitle());
                        rssFeed.setRssQualityBytitle(feed.getTitle());
                        rssFeed.setRssReleaseGroupByTitle(feed.getTitle());
                        rssFeed.setRssDateBytitle(feed.getTitle());
                        rssFeed.setLinkByKey(rss.getLinkKey(), feed);

                        try {
                            if(!StringUtils.isEmpty(feed.getDescription().getValue())) {
                                rssFeed.setDesc(feed.getDescription().getValue());
                            }
                        } catch (NullPointerException ne) {
                            log.error(ne.toString());
                        }

                        String[] rssTitleSplit = StringUtils.split(rssFeed.getRssTitle());
                        if (rssTitleSplit.length == 1) {
                            rssFeed.setRssPoster(daumMovieTvService.getPoster(rssFeed.getRssTitle()));
                        } else {
                            for (int j = rssTitleSplit.length - 1; j > 0; j--) {
                                StringBuffer posterTitle = new StringBuffer();
                                for (int k = 0; k <= j; k++) {
                                    posterTitle.append(rssTitleSplit[k] + " ");
                                }
                                String posterUrl = daumMovieTvService
                                        .getPoster(StringUtils.trim(posterTitle.toString()));
                                if (!StringUtils.isEmpty(posterUrl)) {
                                    rssFeed.setRssPoster(posterUrl);
                                    break;
                                }
                            }
                        }
                        // rssFeed.setRssPoster(daumMovieTvService.getPoster(rssFeed.getRssTitle()));
                        rssFeedList.add(rssFeed);

                        log.info("Add Feed: " + rssFeed.getTitle());

                        if(rss.getDownloadAll()) {
                            log.info("RSS Download Repuest All: " + rssFeed.getTitle());
                            download(rssFeed, rss);
                        }

                        // Watch List를 체크하여 다운로드 요청한다.
                        checkWatchList(rssFeed);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        rssFeedRepository.saveAll(rssFeedList);
        simpMessagingTemplate.convertAndSend("/topic/feed/update", true);
    }

    public void checkWatchListFromDb() {
        for (RssFeed rssFeed : rssFeedRepository.findAll()) {
            checkWatchList(rssFeed);
        }
    }

    @Async
    public void asyncLoadRss() {
        loadRss();
    }

    private void checkWatchList(RssFeed rssFeed) {
        if (StringUtils.isBlank(rssFeed.getRssQuality())) {
            rssFeed.setRssQuality("100p");
        }
        Optional<WatchList> optionalWatchList = watchListRepository.findByTitleRegex(rssFeed.getTitle(),
                rssFeed.getRssQuality());

        if (optionalWatchList.isPresent()) {
            WatchList watchList = optionalWatchList.get();
            log.info("Matched Feed: " + rssFeed.getTitle());

            boolean seenDone = false;
            boolean subtitleDone = false;
            boolean checkQuality = false;

            if (StringUtils.isBlank(watchList.getQuality())) {
                watchList.setQuality("100p");
            }

            try {
                if (StringUtils.endsWithIgnoreCase(watchList.getQuality(), "P+")) {
                    checkQuality = Integer.parseInt(StringUtils.removeIgnoreCase(rssFeed.getRssQuality(),
                            "P")) >= Integer.parseInt(StringUtils.removeIgnoreCase(watchList.getQuality(), "P+"));
                } else {
                    checkQuality = Integer.parseInt(StringUtils.removeIgnoreCase(rssFeed.getRssQuality(),
                            "P")) == Integer.parseInt(StringUtils.removeIgnoreCase(watchList.getQuality(), "P"));
                }
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }

            if (!checkQuality) {
                log.info("Rejected by Quality: " + rssFeed.getTitle());
                return;
            }

            if (watchList.getSeries()) {
                if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                        rssFeed.getRssEpisode(), false) > 0) {
                    seenDone = true;
                }

                if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                        rssFeed.getRssEpisode(), true) > 0 || !watchList.getSubtitle()) {
                    subtitleDone = true;
                }
            } else {
                if (seenListRepository.findFirstByLinkAndSubtitle(rssFeed.getLink(), false).isPresent()) {
                    seenDone = true;
                }

                if (seenListRepository.findFirstByLinkAndSubtitle(rssFeed.getLink(), true).isPresent()
                        || !watchList.getSubtitle()) {
                    subtitleDone = true;
                }
            }

            if (seenDone && subtitleDone) {
                log.info("Rejected by Seen: " + rssFeed.getTitle());
            } else {
                if (!watchList.getSubtitle() && StringUtils.contains(rssFeed.getTitle(), "자막")
                        && !StringUtils.startsWith(rssFeed.getLink(), "magnet")) {
                    log.info("Rejected by Subtitle: " + rssFeed.getTitle());
                    return;
                }
                if (watchList.getSeries()) {
                    try {
                        int startSeason = Integer.parseInt(watchList.getStartSeason());
                        int endSeason = Integer.parseInt(watchList.getEndSeason());
                        int startEpisode = Integer.parseInt(watchList.getStartEpisode());
                        int endEpisode = Integer.parseInt(watchList.getEndEpisode());
                        int currSeason = Integer.parseInt(rssFeed.getRssSeason());
                        int currEpisode = Integer.parseInt(rssFeed.getRssEpisode());

                        if (currSeason < startSeason || currSeason > endSeason) {
                            log.info("Rejected by Season: Start: " + startSeason + " End: " + endSeason + " Feed: "
                                    + currSeason);
                            return;
                        }
                        if (currEpisode < startEpisode || currEpisode > endEpisode) {
                            log.info("Rejected by Episode: Start: " + startEpisode + " End: " + endEpisode + " Feed: "
                                    + currEpisode);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        log.info(e.getMessage());
                    }
                }

                log.info("Download Repuest: " + rssFeed.getTitle());

                download(rssFeed, watchList);
            }
        }
    }

    private void addToSeenList(RssFeed rssFeed, String path, String rename) {
        SeenList seenList = new SeenList();
        seenList.setTitle(rssFeed.getRssTitle());
        seenList.setLink(rssFeed.getLink());
        seenList.setDownloadPath(path);
        seenList.setSeason(rssFeed.getRssSeason());
        seenList.setEpisode(rssFeed.getRssEpisode());
        seenList.setRenameStatus(StringUtils.isBlank(rename) ? "N/A" : "false");

        if (StringUtils.contains(rssFeed.getTitle(), "자막") && !StringUtils.startsWith(rssFeed.getLink(), "magnet")) {
            seenList.setSubtitle(true);
            seenList.setTitle("[자막]" + rssFeed.getRssTitle());
        }

        seenListRepository.save(seenList);
    }

    private void addToDownloadList(Long id, RssFeed rssFeed, WatchList watchList, String path) {
        DownloadList download = new DownloadList();
        download.setId(id);
        download.setName(rssFeed.getTitle());
        download.setUri(rssFeed.getLink());
        download.setDownloadPath(path);
        if (!StringUtils.isBlank(watchList.getRename())) {
            download.setRename(CommonUtils.getRename(watchList.getRename(), rssFeed.getRssTitle(),
                    rssFeed.getRssSeason(), rssFeed.getRssEpisode(), rssFeed.getRssQuality(),
                    rssFeed.getRssReleaseGroup(), rssFeed.getRssDate()));
        }

        downloadListRepository.save(download);
    }

    private void addToDownloadList(Long id, RssFeed rssFeed, String path) {
        DownloadList download = new DownloadList();
        download.setId(id);
        download.setName(rssFeed.getTitle());
        download.setUri(rssFeed.getLink());
        download.setDownloadPath(path);

        downloadListRepository.save(download);
    }

    private void addToDownloadList(DownloadList download, RssFeed rssFeed, WatchList watchList) {
        if (!StringUtils.isBlank(watchList.getRename())) {
            download.setRename(CommonUtils.getRename(watchList.getRename(), rssFeed.getRssTitle(),
                    rssFeed.getRssSeason(), rssFeed.getRssEpisode(), rssFeed.getRssQuality(),
                    rssFeed.getRssReleaseGroup(), rssFeed.getRssDate()));
        }

        downloadListRepository.save(download);
    }

    private void deleteFeed() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("USE_LIMIT");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                Optional<Setting> limitCnt = settingRepository.findByKey("LIMIT_COUNT");
                if (limitCnt.isPresent()) {
                    rssFeedRepository.deleteByLimitCount(Integer.parseInt(limitCnt.get().getValue()));
                }
            }
        }
    }

    private void download(RssFeed rssFeed, WatchList watchList) {
        String path = downloadPathRepository.computedPath(watchList.getDownloadPath(), rssFeed.getRssTitle(),
            rssFeed.getRssSeason());
        if (StringUtils.isBlank(path)) {
            path = watchList.getDownloadPath();
        }

        Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_APP");
        if (optionalSetting.isPresent()) {
            if (StringUtils.equals(optionalSetting.get().getValue(), "TRANSMISSION")) {
                // Request Download to Transmission
                if (StringUtils.startsWith(rssFeed.getLink(), "magnet")
                    || StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(rssFeed.getLink()), "torrent")) {
                    int torrentAddedId = transmissionService.torrentAdd(rssFeed.getLink(), path);
                    log.info("Transmission ID: " + torrentAddedId);
    
                    if (torrentAddedId > 0) {
                        // Add to Seen
                        addToSeenList(rssFeed, path, watchList.getRename());
    
                        // Add to Download List
                        addToDownloadList((long) torrentAddedId, rssFeed, watchList, path);
                    }
                } else {
                    Optional<DownloadList> optionalSeq = downloadListRepository.findTopByOrderByIdDesc();
                    long tempId;
                    if (optionalSeq.isPresent()) {
                        Long id = optionalSeq.get().getId() + 100L;                    
                        log.debug("id: " + id);
                        tempId = id;
                    } else {
                        tempId = 100L;
                    }
                    DownloadList download = new DownloadList();
                    download.setUri(rssFeed.getLink());
                    download.setDownloadPath(path);
                    download.setId(tempId);
                    httpDownloadService.createTransmission(download);
                    addToSeenList(rssFeed, path, watchList.getRename());
                }
            } else if (StringUtils.equals(optionalSetting.get().getValue(), "DOWNLOAD_STATION")) {
                // Request Download to Download Station
                if (downloadStationService.create(rssFeed.getLink(), path)) {
                    // Add to Seen
                    addToSeenList(rssFeed, path, watchList.getRename());

                    // Add to Download List
                    boolean isExist = false;
                    for (DownloadList down : downloadStationService.list()) {
                        if (StringUtils.equals(rssFeed.getLink(), down.getUri())) {
                            isExist = true;
                            // downloadListRepository.save(down);
                            addToDownloadList(down, rssFeed, watchList);
                        }
                    }

                    if (isExist == false) {
                        addToDownloadList(0L, rssFeed, watchList, path);
                    }
                }
            } else if (StringUtils.equals(optionalSetting.get().getValue(), "EMBEDDED")) {
                Long torrentAddedId = btService.create(rssFeed.getLink(), path, rssFeed.getTitle());
                log.info("Embeded ID: " + torrentAddedId);

                if (torrentAddedId > 0) {
                    // Add to Seen
                    addToSeenList(rssFeed, path, watchList.getRename());

                    // Add to Download List
                    addToDownloadList((long) torrentAddedId, rssFeed, watchList, path);
                }
            }
        }
    }

    private void download(RssFeed rssFeed, RssList rssList) {
        String path = downloadPathRepository.computedPath(rssList.getDownloadPath(), rssFeed.getRssTitle(),
            rssFeed.getRssSeason());
        if (StringUtils.isBlank(path)) {
            path = rssList.getDownloadPath();
        }

        Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_APP");
        if (optionalSetting.isPresent()) {
            if (StringUtils.equals(optionalSetting.get().getValue(), "TRANSMISSION")) {
                // Request Download to Transmission
                if (StringUtils.startsWith(rssFeed.getLink(), "magnet")
                    || StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(rssFeed.getLink()), "torrent")) {
                    int torrentAddedId = transmissionService.torrentAdd(rssFeed.getLink(), path);
                    log.info("Transmission ID: " + torrentAddedId);
    
                    if (torrentAddedId > 0) {
                        // Add to Seen
                        // addToSeenList(rssFeed, path);
    
                        // Add to Download List
                        addToDownloadList((long) torrentAddedId, rssFeed, path);
                    }
                } else {
                    Optional<DownloadList> optionalSeq = downloadListRepository.findTopByOrderByIdDesc();
                    long tempId;
                    if (optionalSeq.isPresent()) {
                        Long id = optionalSeq.get().getId() + 100L;                    
                        log.debug("id: " + id);
                        tempId = id;
                    } else {
                        tempId = 100L;
                    }
                    DownloadList download = new DownloadList();
                    download.setUri(rssFeed.getLink());
                    download.setDownloadPath(path);
                    download.setId(tempId);
                    httpDownloadService.createTransmission(download);
                }
            } else if (StringUtils.equals(optionalSetting.get().getValue(), "DOWNLOAD_STATION")) {
                // Request Download to Download Station
                if (downloadStationService.create(rssFeed.getLink(), path)) {
                    // Add to Seen
                    // addToSeenList(rssFeed, path);

                    // Add to Download List
                    boolean isExist = false;
                    for (DownloadList down : downloadStationService.list()) {
                        if (StringUtils.equals(rssFeed.getLink(), down.getUri())) {
                            isExist = true;
                            downloadListRepository.save(down);
                        }
                    }

                    if (isExist == false) {
                        addToDownloadList(0L, rssFeed, path);
                    }
                }
            } else if (StringUtils.equals(optionalSetting.get().getValue(), "EMBEDDED")) {
                Long torrentAddedId = btService.create(rssFeed.getLink(), path, rssFeed.getTitle());
                log.info("Embeded ID: " + torrentAddedId);

                if (torrentAddedId > 0) {
                    // Add to Seen
                    // addToSeenList(rssFeed, path);

                    // Add to Download List
                    addToDownloadList((long) torrentAddedId, rssFeed, path);
                }
            }
        }
    }

}