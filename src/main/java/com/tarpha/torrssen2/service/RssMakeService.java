package com.tarpha.torrssen2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.RssFeed;
import com.tarpha.torrssen2.domain.RssList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.RssListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
@Slf4j
public class RssMakeService {

    @Value("${internal-rss1.tv-boards}")
    private String[] tvBoards1;

    @Value("${internal-rss6.base-url}")
    private String baseUrl6;

    @Value("${internal-rss6.page-html}")
    private String pageHtml6;

    @Value("${internal-rss6.max-page}")
    private int maxPage6;

    @Value("${internal-rss6.tv-boards}")
    private String[] tvBoards6;

    @Value("${internal-rss7.base-url}")
    private String baseUrl7;

    @Value("${internal-rss7.page-html}")
    private String pageHtml7;

    @Value("${internal-rss7.max-page}")
    private int maxPage7;

    @Value("${internal-rss7.tv-boards}")
    private String[] tvBoards7;

    @Value("${internal-rss8.base-url}")
    private String baseUrl8;

    @Value("${internal-rss8.page-html}")
    private String pageHtml8;

    @Value("${internal-rss8.max-page}")
    private int maxPage8;

    @Value("${internal-rss8.tv-boards}")
    private String[] tvBoards8;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private DaumMovieTvService daumMovieTvService;

    private String sessionId;

    private final String SESSION_KEY = "PHPSESSID";

    private final int SLEEP_SECOND = 10;

    private final int TIMEOUT_SECOND = 60;

    public List<RssFeed> makeRss() {
        List<RssFeed> rssFeedList = new ArrayList<>();

//        for (RssList rss : rssListRepository.findByUseDbAndInternal(true, true)) {
//            rssFeedList.addAll(makeRss8(rss));
//        }

        return rssFeedList;
    }

    private RssFeed makeFeed(String title, String magnet, RssList rss) {
        RssFeed rssFeed = new RssFeed();
        rssFeed.setTitle(title);
        rssFeed.setRssSite(rss.getName());
        rssFeed.setLink(magnet);
        rssFeed.setTvSeries(rss.getTvSeries());
        rssFeed.setRssTitleByTitle(title);
        rssFeed.setRssEpisodeByTitle(title);
        rssFeed.setRssSeasonByTitle(title);
        rssFeed.setRssQualityBytitle(title);
        rssFeed.setRssReleaseGroupByTitle(title);
        rssFeed.setRssDateBytitle(title);

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

        return rssFeed;
    }

    private Document getDoc(String urlString) {
        
        try {
            Optional<Setting> optionalHost = settingRepository.findByKey("PROXY_HOST");
            Optional<Setting> optionalPort = settingRepository.findByKey("PROXY_PORT");

            if (optionalHost.isPresent() && optionalPort.isPresent()) {
                log.debug("Use Proxy");

                String proxyHost = optionalHost.get().getValue();
                String strPort = optionalPort.get().getValue();

                if(StringUtils.isEmpty(proxyHost) || StringUtils.isEmpty(strPort)) {
                    log.error("Proxy info is EMPTY {}:{}", proxyHost, strPort);

                    return null;
                }
                
                int proxyPort = Integer.parseInt(strPort);

                Response res;

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

                if(StringUtils.isNotEmpty(sessionId)) {
                    res = Jsoup.connect(urlString).cookie(SESSION_KEY, sessionId).proxy(proxy).timeout(TIMEOUT_SECOND * 1000).execute();

                    log.debug("set sessionId: {}", sessionId);
                } else {
                    res = Jsoup.connect(urlString).proxy(proxy).timeout(TIMEOUT_SECOND * 1000).execute();
                    sessionId = res.cookie(SESSION_KEY);

                    log.debug("get sessionId: {}", sessionId);
                }

                log.debug("PHPSESSID: {}", sessionId);

                return res.parse();
            } else {
                log.debug("No Proxy {}", urlString);

                Response res;

                if(StringUtils.isNotEmpty(sessionId)) {
                    res = Jsoup.connect(urlString).cookie(SESSION_KEY, sessionId).timeout(TIMEOUT_SECOND * 1000).execute();

                    log.debug("res: {}", res);
                    log.debug("set sessionId: {}", sessionId);
                } else {
                    res = Jsoup.connect(urlString).timeout(TIMEOUT_SECOND * 1000).execute();
                    sessionId = res.cookie(SESSION_KEY);

                    log.debug("get sessionId: {}", sessionId);
                }

                log.debug("PHPSESSID: {}", sessionId);

                return res.parse();
            }
        } catch(Exception e) {
            //log.debug("error: {}", e.);
            log.error(urlString + " / " + e.toString());

            return null;
        } 

    }
   
    private List<RssFeed> makeRss6(RssList rss) {
        
        log.info("Load RSS Site6 : {}, {} ", rss.getName(), rss.getUrl());
        
        sessionId = null;

        List<RssFeed> rssFeedList = new ArrayList<>();
        
        try {
            for(int page = 1; page <= maxPage6; page++ ) {
                String targetBoard = null;

                for(int i = 0; i < tvBoards1.length; i++) {
                    if(StringUtils.equals(tvBoards1[i], rss.getUrl())) {                    
                        targetBoard = tvBoards6[i];
                    }
                }

                if(StringUtils.isBlank(targetBoard)) {
                    return rssFeedList;
                }

                String url = baseUrl6 + "/" + targetBoard + "/" + pageHtml6 + page;
                Document doc = getDoc(url);

                Elements els = null;

                try {
                    els = doc.select("div.wr-subject");

                    log.debug(els.toString());

                    for(int i = els.size() -1; i >= 0; i--) {
                        Element item = els.get(i).select("a").get(0);
                        String title = item.text();

                        log.debug(item.absUrl("href"));

                        String magnet = getTorrentLink6(item.absUrl("href"));

                        log.debug("rss6: {}, {}", new Object[]{title, magnet});

                        rssFeedList.add(makeFeed(title, magnet, rss));

                        Thread.sleep(SLEEP_SECOND * 1000);
                    }

                } catch ( Exception e) {
                    log.error(baseUrl6+ " / " + e.toString());
                }
            }        
        } catch (Exception e) {
            log.error(e.toString());
        }

        return rssFeedList;
    }

    private String getTorrentLink6(String urlString) throws Exception {
        Document doc = getDoc(urlString);

        // <li class="list-group-item en font-14 break-word">
        Element el = doc.select("li.list-group-item a[href]").first();

        log.debug("getTorrentLink6 {} {}", urlString, el.toString());

        return el.attr("href");
    }

    private List<RssFeed> makeRss7(RssList rss) {
        log.info("Load RSS Site7 : {}, {} ", rss.getName(), rss.getUrl());

        sessionId = null;

        List<RssFeed> rssFeedList = new ArrayList<>();

        try {
            for(int page = 1; page <= maxPage7; page++ ) {
                String targetBoard = null;

                for(int i = 0; i < tvBoards1.length; i++) {
                    if(StringUtils.equals(tvBoards1[i], rss.getUrl())) {                    
                        targetBoard = tvBoards7[i];
                    }
                }

                if(StringUtils.isBlank(targetBoard)) {
                    return rssFeedList;
                }

                String url = baseUrl7 + targetBoard + "&" + pageHtml7 + "=" + page;
                Document doc = getDoc(url);

                Elements els = null;
                
                els = doc.select("li.tit");

                log.debug(els.toString());

                for(int i = els.size() -1; i >= 0; i--) {
                    try {
                        Element item = els.get(i).select("a").get(0);
                        // String title = StringUtils.removeEnd(item.text(), "_");
                        String title = item.text().replaceAll("_", ".").replaceAll("토렌트씨", "");

                        String magnet = getTorrentLink7(item.absUrl("href"));
                        log.debug("rss7: {}, {}", new Object[]{title, magnet});

                        if(StringUtils.isNotBlank(magnet)) {
                            rssFeedList.add(makeFeed(title, magnet, rss));
                        }

                        Thread.sleep(SLEEP_SECOND * 1000);
                    } catch ( Exception e) {
                        log.error(baseUrl7+ " / " + e.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }

        return rssFeedList;
    }

    private String getTorrentLink7(String urlString) throws Exception {
        log.debug("getTorrentLink7: {}", urlString);

        Document doc = getDoc(urlString);

        Element el = doc.select("table.notice_table a[href]").last();

        log.debug("getTorrentLink7 {} {}", urlString, el.toString());

        return el.attr("href");
    }

    private List<RssFeed> makeRss8(RssList rss) {
        log.info("Load RSS Site8 : {}, {} ", rss.getName(), rss.getUrl());

        sessionId = null;

        List<RssFeed> rssFeedList = new ArrayList<>();

        try {
            for(int page = 1; page <= maxPage8; page++ ) {
                String targetBoard = null;

                for(int i = 0; i < tvBoards1.length; i++) {
                    if(StringUtils.equals(tvBoards1[i], rss.getUrl())) {                    
                        targetBoard = tvBoards8[i];
                    }
                }

                if(StringUtils.isBlank(targetBoard)) {
                    return rssFeedList;
                }

                String url = baseUrl8 + "bbs/board.php?bo_table=" + targetBoard + "&" + pageHtml8 + "=" + page;
                Document doc = getDoc(url);

                Elements els = null;
                
                //<div class="wr-subject">
                els = doc.select("div.wr-subject");

                log.debug(els.toString());

                for(int i = els.size() -1; i >= 0; i--) {
                    try {
                        Element item = els.get(i).select("a").get(0);
                        String title = item.text();

                        log.debug(item.absUrl("href"));

                        String magnet = getTorrentLink8(item.absUrl("href"));

                        log.debug("rss8: {}, {}", new Object[]{title, magnet});

                        rssFeedList.add(makeFeed(title, magnet, rss));

                        Thread.sleep(SLEEP_SECOND * 1000);
                    } catch ( Exception e) {
                        log.error(baseUrl8+ " / " + e.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }

        return rssFeedList;
    }

    private String getTorrentLink8(String urlString) throws Exception {
        Document doc = getDoc(urlString);

        // <li class="list-group-item en font-14 red break-word">
        Element el = doc.select("li.list-group-item a[href]").first();

        log.debug("getTorrentLink8 {} {}", urlString, el.toString());

        //magnet:?xt=urn:
        return el.attr("href");
    }
    
}
