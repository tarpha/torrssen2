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
import org.springframework.transaction.annotation.Transactional;

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
    private RssMakeService rssMakeService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void loadRss() {
        log.info("=== Load RSS ===");

        deleteFeed();

        List<RssFeed> rssFeedList = new ArrayList<RssFeed>();

        for(RssFeed rssFeed: rssMakeService.makeRss()) {
            if (!rssFeedRepository.findByLink(rssFeed.getLink()).isPresent()) {
                rssFeedList.add(rssFeed);

                Optional<RssList> optioalRss = rssListRepository.findByName(rssFeed.getRssSite());
                if(optioalRss.isPresent()) {
                    if(optioalRss.get().getDownloadAll()) {
                        download(rssFeed, optioalRss.get());
                    }
                }

                // Watch List를 체크하여 다운로드 요청한다.
                checkWatchList(rssFeed, null);
            }
        }

        for (RssList rss : rssListRepository.findByUseDbAndInternal(true, false)) {
            log.info("Load RSS Site : " + rss.getName());
            try {
                URL feedSource = new URL(rss.getUrl());
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feedList = input.build(new XmlReader(feedSource));

                for (int i = feedList.getEntries().size() - 1; i >= 0; i--) {
                    //Feed 한 건이 오류가 발생하여도 전체 로드에는 문제 없도록 예외처리를 추가한다.
                    try {
                        SyndEntry feed = feedList.getEntries().get(i);
                        RssFeed rssFeed = new RssFeed();

                        // log.debug(feed.toString());

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
                                log.debug("description: " + ne.toString());
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

                            log.debug("Add Feed: " + rssFeed.getTitle());

                            if(rss.getDownloadAll()) {
                                log.debug("RSS Download Repuest All: " + rssFeed.getTitle());
                                download(rssFeed, rss);
                            }

                            // Watch List를 체크하여 다운로드 요청한다.
                            checkWatchList(rssFeed, null);
                        }
                    } catch (Exception e) {
                        //Feed 개별 건에 대한 Exception 처리
                        log.error(e.getMessage());
                    }
                }
            } catch (Exception e) {
                //Feed Site에 대한 Exception 처리
                log.error(e.getMessage());
            }
        }

        rssFeedRepository.saveAll(rssFeedList);
        // rssFeedRepository.saveAll(rssMakeService.makeRss());

        simpMessagingTemplate.convertAndSend("/topic/feed/update", true);
    }

    public void checkWatchListFromDb(List<WatchList> list) {
        for (RssFeed rssFeed : rssFeedRepository.findAll()) {
            checkWatchList(rssFeed, list);
        }
    }

    @Async
    public void asyncLoadRss() {
        loadRss();
    }

    public boolean checkWatchListQuality(RssFeed rssFeed, WatchList watchList) {
        boolean checkQuality = false;

        if (StringUtils.isBlank(watchList.getQuality())) {
            watchList.setQuality("100p");
        }

        try {
            if (StringUtils.contains(watchList.getQuality(), ',')) {
                checkQuality = StringUtils.containsIgnoreCase(watchList.getQuality(), rssFeed.getRssQuality());

            } else if (StringUtils.endsWithIgnoreCase(watchList.getQuality(), "P+")) {
                checkQuality = Integer.parseInt(StringUtils.removeIgnoreCase(rssFeed.getRssQuality(), "P")) 
                    >= Integer.parseInt(StringUtils.removeIgnoreCase(watchList.getQuality(), "P+"));
            } else {
                checkQuality = Integer.parseInt(StringUtils.removeIgnoreCase(rssFeed.getRssQuality(), "P")) 
                    == Integer.parseInt(StringUtils.removeIgnoreCase(watchList.getQuality(), "P"));
            }
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
        }

        return checkQuality;
    }
 
    /**
     * Feed가 WatchList에 존재할 경우 다운로드 요청을 한다.
     * 
     * @param rssFeed   WatchList에 존재하는지 검사할 대상이 되는 개별 RSS Feed 
     * @param list      사용자가 화면에서 WatchList 개별 건을 선택해서 직접 실행 했을 때 그 List를 받아올 인자
     */
    private void checkWatchList(RssFeed rssFeed, List<WatchList> list) {

        if (StringUtils.isBlank(rssFeed.getRssQuality())) {
            rssFeed.setRssQuality("100p");
        }
        Optional<WatchList> optionalWatchList = watchListRepository.findByTitleRegex(rssFeed.getTitle(),
            rssFeed.getRssQuality());

        if (optionalWatchList.isPresent()) {
            WatchList watchList = optionalWatchList.get();
            log.info("Matched Feed: " + rssFeed.getTitle());

            try {
                if (watchList.getRssList() != null && watchList.getRssList().size() > 0) {
                    if (!watchList.getRssList().contains(rssFeed.getRssSite())) {
                        log.info("Skipped by RSS List");
                        return;
                    }
                }
            } catch (NullPointerException e) {
                log.error(e.toString());
            }

            if(list != null) {
                log.info("Custom Execute");
                boolean isExists = false;
                for(WatchList wl : list) {
                    if(StringUtils.equals(watchList.getTitle(), wl.getTitle())) {
                        log.info("Custom Execute Matched: " + rssFeed.getTitle());
                        isExists = true;
                        break;
                    }
                }

                if(!isExists) {
                    log.info("Custom Execute Not Matched: " + rssFeed.getTitle());
                    return;
                }
            }

            boolean seenDone = false;
            boolean subtitleDone = false;
            // boolean checkQuality = false;

            // if (StringUtils.isBlank(watchList.getQuality())) {
            //     watchList.setQuality("100p");
            // }

            // try {
            //     if (StringUtils.endsWithIgnoreCase(watchList.getQuality(), "P+")) {
            //         checkQuality = Integer.parseInt(StringUtils.removeIgnoreCase(rssFeed.getRssQuality(), "P")) 
            //             >= Integer.parseInt(StringUtils.removeIgnoreCase(watchList.getQuality(), "P+"));
            //     } else {
            //         checkQuality = Integer.parseInt(StringUtils.removeIgnoreCase(rssFeed.getRssQuality(), "P")) 
            //             == Integer.parseInt(StringUtils.removeIgnoreCase(watchList.getQuality(), "P"));
            //     }
            // } catch (NumberFormatException e) {
            //     log.error(e.getMessage());
            // }

            // if (!checkQuality) {
            if(!checkWatchListQuality(rssFeed, watchList)) {
                log.info("Rejected by Quality: " + rssFeed.getTitle());
                return;
            }

            if (watchList.getSeries()) {
                if (StringUtils.contains(watchList.getQuality(), ',')) {
                    if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                        rssFeed.getRssEpisode(), false, rssFeed.getRssQuality()) > 0) {
                        seenDone = true;
                    }

                    if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                            rssFeed.getRssEpisode(), true, rssFeed.getRssQuality()) > 0 || !watchList.getSubtitle()) {
                        subtitleDone = true;
                    }
                } else {
                    if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                        rssFeed.getRssEpisode(), false) > 0) {
                        seenDone = true;
                    }

                    if (seenListRepository.countByParams(rssFeed.getLink(), rssFeed.getRssTitle(), rssFeed.getRssSeason(),
                            rssFeed.getRssEpisode(), true) > 0 || !watchList.getSubtitle()) {
                        subtitleDone = true;
                    }
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
                        log.error(e.toString());
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
        seenList.setQuality(rssFeed.getRssQuality());

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

    @Transactional
    private void deleteFeed() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("USE_LIMIT");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                Optional<Setting> limitCnt = settingRepository.findByKey("LIMIT_COUNT");
                if (limitCnt.isPresent()) {
                    int cnt = Integer.parseInt(limitCnt.get().getValue());
                    rssFeedRepository.deleteByLimitCount(cnt);
                    downloadListRepository.deleteByLimitCount(cnt);
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
                    log.debug("Transmission ID: " + torrentAddedId);
    
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
                log.debug("Embeded ID: " + torrentAddedId);

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
                    log.debug("Transmission ID: " + torrentAddedId);
    
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
                log.debug("Embeded ID: " + torrentAddedId);

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