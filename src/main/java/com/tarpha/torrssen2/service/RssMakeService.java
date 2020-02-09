package com.tarpha.torrssen2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

    @Value("${internal-rss1.base-url}")
    private String baseUrl1;

    @Value("${internal-rss1.page-query}")
    private String pageQuery1;

    @Value("${internal-rss1.max-page}")
    private int maxPage1;

    @Value("${internal-rss1.board-query}")
    private String boardQuery1;

    @Value("${internal-rss2.base-url}")
    private String baseUrl2;

    @Value("${internal-rss2.page-html}")
    private String pageHtml2;

    @Value("${internal-rss2.max-page}")
    private int maxPage2;

    @Value("${internal-rss1.tv-boards}")
    private String[] tvBoards1;

    @Value("${internal-rss2.tv-boards}")
    private String[] tvBoards2;

    // @Value("${internal-rss.other-boards}")
    // private String[] otherBoards;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private DaumMovieTvService daumMovieTvService;

    private final int TIMEOUT_VALUE = 30000;

    public List<RssFeed> makeRss() {
        List<RssFeed> rssFeedList = new ArrayList<>();

        // Document doc = getDoc("https://torrenthaja12.com/bbs/board.php?bo_table=torrent_drama");
        // for(BoardVO board: boardList) {
        for (RssList rss : rssListRepository.findByUseDbAndInternal(true, true)) {
            rssFeedList.addAll(makeRss1(rss));
            rssFeedList.addAll(makeRss2(rss));
        }

        return rssFeedList;
    }

    private List<RssFeed> makeRss1(RssList rss) {
        log.info("Load RSS Site1 : " + rss.getName());
        
        List<RssFeed> rssFeedList = new ArrayList<>();

        for(int page = 1; page <= maxPage1; page++ ) {

            // try {
                // String url = baseUrl + "?" + boardQuery + "=" + board.getName() + "&" + pageQuery + "=" + page;
                String url = baseUrl1 + "?" + boardQuery1 + "=" + rss.getUrl() + "&" + pageQuery1 + "=" + page;
                // log.debug(url);
                Document doc = getDoc(url);
                Elements els = null;
                
                try {
                    els = doc.select(".board-list-body table tr td .td-subject");
                    
                    for(int i = els.size() -1; i >= 0; i--) {
                        try {
                            Element item = els.get(i).select("a").last();
                            String title = item.text();
                            String magnet = getMagnetString1(item.absUrl("href"));
                            log.debug(title + "|" + magnet);

                            rssFeedList.add(makeFeed(title, magnet, rss));
                        } catch (Exception e) {
                            log.error(els.get(i).select("a").last().text() + "/" + e.toString());
                        }
                    }

                } catch (NullPointerException e) {
                    log.error(baseUrl1 + " / " + e.toString());
                }

            // } catch(Exception e) {
            //     log.error(e.toString());
            // }
        }

        return rssFeedList;
    }

    private List<RssFeed> makeRss2(RssList rss) {
        log.info("Load RSS Site2 : " + rss.getName());

        List<RssFeed> rssFeedList = new ArrayList<>();

        for(int page = 1; page <= maxPage2; page++ ) {

            // try {
                String targetBoard = null;

                for(int i = 0; i < tvBoards1.length; i++) {
                    if(StringUtils.equals(tvBoards1[i], rss.getUrl())) {
                        targetBoard = tvBoards2[i];
                    }
                }

                if(StringUtils.isBlank(targetBoard)) {
                    return rssFeedList;
                }

                String url = baseUrl2 + "/" + targetBoard + "/" + pageHtml2 + page + ".htm";
                // log.debug(url);
                Document doc = getDoc(url);
                Elements els = null;
                
                try {
                    els = doc.select("#main_body tr td.subject");

                    for(int i = els.size() -1; i >= 0; i--) {
                        try{
                            Element item = els.get(i).select("a").last();
                            log.debug(item.text() + ":" + item.attr("href"));
                            String title = item.text();
                            log.debug(baseUrl2 + item.attr("href").substring(2));
                            String magnet = getMagnetString2(baseUrl2 + item.attr("href").substring(2));
                            log.debug(title + "|" + magnet);

                            rssFeedList.add(makeFeed(title, magnet, rss));
                        } catch(Exception e) {
                            log.error(els.get(i).select("a").last().text() + "/" + e.toString());
                        }
                    }

                } catch ( NullPointerException e) {
                    log.error(baseUrl2 + " / " + e.toString());
                }

            // } catch(Exception e) {
            //     log.error(e.toString());
            // }
        }

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
        URL url;
        HttpsURLConnection uc = null;

        try {
            url = new URL(urlString);

            Optional<Setting> optionalHost = settingRepository.findByKey("PROXY_HOST");
            Optional<Setting> optionalPort = settingRepository.findByKey("PROXY_PORT");

            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            if (optionalHost.isPresent() && optionalPort.isPresent()) {
                log.debug("Use Proxy");
                String proxyHost = optionalHost.get().getValue();
                int proxyPort = Integer.parseInt(optionalPort.get().getValue());

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                uc = (HttpsURLConnection)url.openConnection(proxy);
                uc.setConnectTimeout(TIMEOUT_VALUE);
                uc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.13) Gecko/20150702 Firefox/3.6.13 (.NET CLR 3.5.30729)");

                uc.connect();

                String line = null;
                StringBuffer tmp = new StringBuffer();
                BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                
                while ((line = in.readLine()) != null) {
                    tmp.append(line);
                }

                in.close();
                uc.disconnect();

                return Jsoup.parse(String.valueOf(tmp));
            } else {
                log.debug("No Proxy");
                return Jsoup.connect(urlString).get();
            }
        } catch(Exception e) {
            log.error(urlString + " / " + e.toString());
            return null;
        } finally {
            if(uc != null) uc.disconnect();
        }
        
    }

    private String getMagnetString1(String urlString) throws Exception {
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

    private String getMagnetString2(String urlString) throws Exception {
        Document doc = getDoc(urlString);
        Element el = doc.selectFirst("div + b + a");

        return el.attr("href");
    }
    
}