package com.tarpha.torrssen2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tarpha.torrssen2.domain.RssFeed;
import com.tarpha.torrssen2.domain.RssList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.RssListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
@Slf4j
public class RssMakeService {

    @Value("${internal-rss.base-url}")
    private String baseUrl;

    @Value("${internal-rss.page-query}")
    private String pageQuery;

    @Value("${internal-rss.max-page}")
    private int maxPage;

    @Value("${internal-rss.board-query}")
    private String boardQuery;

    // @Value("${internal-rss.tv-boards}")
    // private String[] tvBoards;

    // @Value("${internal-rss.other-boards}")
    // private String[] otherBoards;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private DaumMovieTvService daumMovieTvService;

    public List<RssFeed> makeRss() {
        List<RssFeed> rssFeedList = new ArrayList<>();

        // Document doc = getDoc("https://torrenthaja12.com/bbs/board.php?bo_table=torrent_drama");
        // for(BoardVO board: boardList) {
        for (RssList rss : rssListRepository.findByUseDbAndInternal(true, true)) {
            for(int page = 1; page <= maxPage; page++ ) {

                try {
                    // String url = baseUrl + "?" + boardQuery + "=" + board.getName() + "&" + pageQuery + "=" + page;
                    String url = baseUrl + "?" + boardQuery + "=" + rss.getUrl() + "&" + pageQuery + "=" + page;
                    log.debug(url);
                    Document doc = getDoc(url);
                    Elements els = doc.select(".board-list-body table tr td .td-subject");
                    
                    for(int i = els.size() -1; i >= 0; i--) {
                        Element item = els.get(i).select("a").last();
                        String title = item.text();
                        String magnet = getMagnetString(item.absUrl("href"));
                        log.debug(title + "|" + magnet);

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

                        rssFeedList.add(rssFeed);
                    }

                } catch(Exception e) {
                    log.error(e.toString());
                }
                
            }
        }

        return rssFeedList;
    }

    private Document getDoc(String urlString) {
        URL url;
        HttpURLConnection uc = null;

        try {
            url = new URL(urlString);

            Optional<Setting> optionalHost = settingRepository.findByKey("PROXY_HOST");
            Optional<Setting> optionalPort = settingRepository.findByKey("PROXY_PORT");
            if (optionalHost.isPresent() && optionalPort.isPresent()) {
                log.debug("Use Proxy");
                String proxyHost = optionalHost.get().getValue();
                int proxyPort = Integer.parseInt(optionalPort.get().getValue());

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                uc = (HttpURLConnection)url.openConnection(proxy);
                uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.13) Gecko/20150702 Firefox/3.6.13 (.NET CLR 3.5.30729)");

                uc.connect();

                String line = null;
                StringBuffer tmp = new StringBuffer();
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                
                while ((line = in.readLine()) != null) {
                    tmp.append(line);
                }

                in.close();

                return Jsoup.parse(String.valueOf(tmp));
            } else {
                log.debug("No Proxy");
                return Jsoup.connect(urlString).get();
            }
        } catch(Exception e) {
            log.error(e.toString());
            return null;
        } finally {
            if(uc != null) uc.disconnect();
        }
        
    }

    private String getMagnetString(String urlString) throws Exception {
        Document doc = getDoc(urlString);

        Element el = doc.selectFirst(".btn.btn-success.btn-xs");

        Pattern pattern = Pattern.compile("magnet_link\\(\\'(.{1,})\\'\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(el.attr("onclick"));

        if (matcher.matches()) {
            return "magnet:?xt=urn:btih:" + matcher.group(1);
        } else {
            return null;
        }
    }
    
}