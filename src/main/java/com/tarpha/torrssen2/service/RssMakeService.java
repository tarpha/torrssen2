package com.tarpha.torrssen2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

// import java.io.BufferedReader;
// import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
// import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// import com.gargoylesoftware.htmlunit.BrowserVersion;
// import com.gargoylesoftware.htmlunit.ProxyConfig;
// import com.gargoylesoftware.htmlunit.WebClient;
// import com.gargoylesoftware.htmlunit.html.HtmlPage;

// import javax.net.ssl.HttpsURLConnection;
// import javax.net.ssl.SSLContext;
// import javax.net.ssl.TrustManager;
// import javax.net.ssl.X509TrustManager;

import com.tarpha.torrssen2.domain.RssFeed;
import com.tarpha.torrssen2.domain.RssList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.RssListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.lang3.StringUtils;
// import org.json.JSONArray;
// import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
// import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Service
@Slf4j
public class RssMakeService {

    // @Value("${internal-rss1.base-url}")
    // private String baseUrl1;

    // @Value("${internal-rss1.page-query}")
    // private String pageQuery1;

    // @Value("${internal-rss1.max-page}")
    // private int maxPage1;

    // @Value("${internal-rss1.board-query}")
    // private String boardQuery1;

    // @Value("${internal-rss2.base-url}")
    // private String baseUrl2;

    // @Value("${internal-rss2.page-html}")
    // private String pageHtml2;

    // @Value("${internal-rss2.max-page}")
    // private int maxPage2;

    @Value("${internal-rss1.tv-boards}")
    private String[] tvBoards1;

    // @Value("${internal-rss2.tv-boards}")
    // private String[] tvBoards2;

    // @Value("${internal-rss.other-boards}")
    // private String[] otherBoards;

    // @Value("${internal-rss3.base-url}")
    // private String baseUrl3;

    // @Value("${internal-rss3.page-query}")
    // private String pageQuery3;

    // @Value("${internal-rss3.max-page}")
    // private int maxPage3;

    // @Value("${internal-rss3.board-query}")
    // private String boardQuery3;

    // @Value("${internal-rss4.base-url}")
    // private String baseUrl4;

    // @Value("${internal-rss4.page-html}")
    // private String pageHtml4;

    // @Value("${internal-rss4.max-page}")
    // private int maxPage4;

    // @Value("${internal-rss4.tv-boards}")
    // private String[] tvBoards4;

    // @Value("${internal-rss5.base-url}")
    // private String baseUrl5;

    // @Value("${internal-rss5.page-html}")
    // private String pageHtml5;

    // @Value("${internal-rss5.max-page}")
    // private int maxPage5;

    // @Value("${internal-rss5.tv-boards}")
    // private String[] tvBoards5;

    @Value("${internal-rss6.base-url}")
    private String baseUrl6;

    @Value("${internal-rss6.page-html}")
    private String pageHtml6;

    @Value("${internal-rss6.max-page}")
    private int maxPage6;

    @Value("${internal-rss6.tv-boards}")
    private String[] tvBoards6;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private RssListRepository rssListRepository;

    @Autowired
    private DaumMovieTvService daumMovieTvService;

    private String sessionId;

    // private WebClient webClient;

    // private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";

    // private final int TIMEOUT_VALUE = 30000;

    private final String SESSION_KEY = "PHPSESSID";

    private final int SLEEP_SECOND = 10;

    public List<RssFeed> makeRss() {
        List<RssFeed> rssFeedList = new ArrayList<>();

        for (RssList rss : rssListRepository.findByUseDbAndInternal(true, true)) {
            // rssFeedList.addAll(makeRss1(rss));
            // rssFeedList.addAll(makeRss2(rss));
            // rssFeedList.addAll(makeRss3(rss));
            // rssFeedList.addAll(makeRss4(rss));
            rssFeedList.addAll(makeRss6(rss));
        }

        return rssFeedList;
    }

    // private List<RssFeed> makeRss1(RssList rss) {
    //     log.info("Load RSS Site1 : " + rss.getName());

    //     sessionId = null;
        
    //     List<RssFeed> rssFeedList = new ArrayList<>();

    //     for(int page = 1; page <= maxPage1; page++ ) {
    //         String url = baseUrl1 + "?" + boardQuery1 + "=" + rss.getUrl() + "&" + pageQuery1 + "=" + page;
    //         Document doc = getDoc(url);
    //         Elements els = null;
            
    //         try {
    //             els = doc.select(".board-list-body table tr td .td-subject");
                
    //             for(int i = els.size() -1; i >= 0; i--) {
    //                 try {
    //                     Element item = els.get(i).select("a").last();
    //                     String title = item.text();
    //                     String magnet = getMagnetString1(item.absUrl("href"));
    //                     log.debug(title + "|" + magnet);

    //                     rssFeedList.add(makeFeed(title, magnet, rss));
    //                 } catch (Exception e) {
    //                     log.error(els.get(i).select("a").last().text() + "/" + e.toString());
    //                 }
    //             }

    //         } catch (NullPointerException e) {
    //             log.error(baseUrl1 + " / " + e.toString());
    //         }
    //     }

    //     return rssFeedList;
    // }

    // private List<RssFeed> makeRss2(RssList rss) {
    //     log.info("Load RSS Site2 : " + rss.getName());

    //     sessionId = null;

    //     List<RssFeed> rssFeedList = new ArrayList<>();

    //     for(int page = 1; page <= maxPage2; page++ ) {
    //         String targetBoard = null;

    //         for(int i = 0; i < tvBoards2.length; i++) {
    //             if(StringUtils.equals(tvBoards2[i], rss.getUrl())) {                    
    //                 targetBoard = tvBoards2[i];
    //             }
    //         }

    //         if(StringUtils.isBlank(targetBoard)) {
    //             return rssFeedList;
    //         }

    //         String url = baseUrl2 + "/" + targetBoard + "/list?p&" + pageHtml2 + "=" + page;
    //         Document doc = getDoc(url);

    //         Elements els = null;
            
    //         try {
    //             els = doc.select("script");

    //             log.debug(els.toString());

    //             for(int i = els.size() -1; i >= 0; i--) {
    //                 Pattern pattern = Pattern.compile(".*pageItems\\s*=\\s*(\\[.*\\]).*", Pattern.CASE_INSENSITIVE);
    //                 Matcher matcher = pattern.matcher(els.get(i).toString().replace("\n", "").replace("\r", ""));

    //                 if (matcher.matches()) {
    //                     JSONArray jsonArray = new JSONArray(matcher.group(1));

    //                    for(int j = 0; j < jsonArray.length(); j++) {
    //                        JSONObject jsonObj = jsonArray.getJSONObject(j);
    //                        String title = jsonObj.getString("fn");
    //                        String magnet = "magnet:?xt=urn:btih:" + jsonObj.getString("hs");

    //                        rssFeedList.add(makeFeed(title, magnet, rss));
    //                    }

    //                 }
    //             }

    //         } catch ( NullPointerException e) {
    //             log.error(baseUrl2 + " / " + e.toString());
    //         }
    //     }

    //     return rssFeedList;
    // }

    // private List<RssFeed> makeRss3(RssList rss) {
    //     log.info("Load RSS Site3 : {}", rss.getName());

    //     sessionId = null;
        
    //     List<RssFeed> rssFeedList = new ArrayList<>();

    //     for(int page = 1; page <= maxPage3; page++ ) {
    //         String url = baseUrl3 + "?" + boardQuery3 + "=" + rss.getUrl() + "&" + pageQuery3 + "=" + page;

    //         //log.info("Load RSS Site3 : {}", url);

    //         Document doc = getDoc(url);


    //         Elements els = null;
            
    //         try {
    //             els = doc.select("tbody tr td.list-subject");
                
    //             for(int i = els.size() -1; i >= 0; i--) {
    //                 try {
    //                     if(els.get(i).hasClass("pr_subject")) {
    //                         continue;
    //                     }

    //                     Element item = els.get(i).select("a").last();
    //                     String title = item.text() ;
    //                     String magnet = getTorrentLink3(item.absUrl("href"));
    //                     log.debug(title + "|" + magnet);

    //                     rssFeedList.add(makeFeed(title, magnet, rss));
    //                 } catch (Exception e) {
    //                     log.error(els.get(i).select("a").last().text() + "/" + e.toString());
    //                 }
    //             }

    //         } catch (NullPointerException e) {
    //             log.error(baseUrl1 + " / " + e.toString());
    //         }
    //     }

    //     return rssFeedList;
    // }

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
        // URL url;
        // HttpsURLConnection uc = null;

        try {
            // url = new URL(urlString);

            Optional<Setting> optionalHost = settingRepository.findByKey("PROXY_HOST");
            Optional<Setting> optionalPort = settingRepository.findByKey("PROXY_PORT");

            // TrustManager[] trustAllCerts = new TrustManager[]{
            //     new X509TrustManager() {
            //         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            //             return null;
            //         }
            //         public void checkClientTrusted(
            //             java.security.cert.X509Certificate[] certs, String authType) {
            //         }
            //         public void checkServerTrusted(
            //             java.security.cert.X509Certificate[] certs, String authType) {
            //         }
            //     }
            // };
            
            // SSLContext sc = SSLContext.getInstance("SSL");
            // sc.init(null, trustAllCerts, new java.security.SecureRandom());
            // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // if(webClient == null) {
            //     webClient = new WebClient(BrowserVersion.CHROME);
            // }

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
                    res = Jsoup.connect(urlString).cookie(SESSION_KEY, sessionId).proxy(proxy).execute();

                    log.debug("set sessionId: {}", sessionId);
                } else {
                    res = Jsoup.connect(urlString).proxy(proxy).execute();
                    sessionId = res.cookie(SESSION_KEY);

                    log.debug("get sessionId: {}", sessionId);
                }

                log.debug("PHPSESSID: {}", sessionId);

                return res.parse();
                // webClient.getOptions().setProxyConfig(new ProxyConfig(proxyHost, proxyPort, null));

                // HtmlPage page = webClient.getPage(urlString);
                
                // return Jsoup.parse(page.asXml());
            } else {
                log.debug("No Proxy {}", urlString);

                Response res;

                if(StringUtils.isNotEmpty(sessionId)) {
                    res = Jsoup.connect(urlString).cookie(SESSION_KEY, sessionId).execute();

                    log.debug("res: {}", res);
                    log.debug("set sessionId: {}", sessionId);
                } else {
                    res = Jsoup.connect(urlString).execute();
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
        } finally {
            // if(uc != null) uc.disconnect();
        }

    }
   

    // private String getMagnetString1(String urlString) throws Exception {
    //     Document doc = getDoc(urlString);

    //     Element el = doc.selectFirst(".btn.btn-success.btn-xs");

    //     Pattern pattern = Pattern.compile("magnet_link\\(\\'(.{1,})\\'\\);", Pattern.CASE_INSENSITIVE);
    //     Matcher matcher = pattern.matcher(el.attr("onclick"));

    //     if (matcher.matches()) {
    //         return "magnet:?xt=urn:btih:" + matcher.group(1);
    //     } else {
    //         return null;
    //     }
    // }

    // private String getTorrentLink3(String urlString) throws Exception {
    //     Document doc = getDoc(urlString);

    //     Element el = doc.select(".btn.btn-color.btn-xs.view_file_download").get(1);

    //     String uri = el.attr("href");

    //     Optional<Setting> optionalHost = settingRepository.findByKey("PROXY_HOST");
    //     Optional<Setting> optionalPort = settingRepository.findByKey("PROXY_PORT");

    //     Response res;

    //     if (optionalHost.isPresent() && optionalPort.isPresent()) {
    //         String proxyHost = optionalHost.get().getValue();
    //         int proxyPort = Integer.parseInt(optionalPort.get().getValue());

    //         Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

    //         res = Jsoup.connect(uri).cookie("PHPSESSID", sessionId).proxy(proxy).followRedirects(false).execute();
    //     } else {
    //         res = Jsoup.connect(uri).cookie("PHPSESSID", sessionId).followRedirects(false).execute();
    //     }

    //     return res.header("location");
    // }

    // private String getMagnetString2(String urlString) throws Exception {
    //     Document doc = getDoc(urlString);
    //     Element el = doc.selectFirst("div + b + a");

    //     return el.attr("href");
    // }

    // private List<RssFeed> makeRss4(RssList rss) {
    //     log.info("Load RSS Site4 : " + rss.getName());

    //     sessionId = null;

    //     List<RssFeed> rssFeedList = new ArrayList<>();

    //     for(int page = 1; page <= maxPage4; page++ ) {
    //         String targetBoard = null;

    //         for(int i = 0; i < tvBoards2.length; i++) {
    //             if(StringUtils.equals(tvBoards2[i], rss.getUrl())) {                    
    //                 targetBoard = tvBoards4[i];
    //             }
    //         }

    //         if(StringUtils.isBlank(targetBoard)) {
    //             return rssFeedList;
    //         }

    //         String url = baseUrl4 + "/" + targetBoard + "?&" + pageHtml4 + "=" + page;
    //         Document doc = getDoc(url);

    //         Elements els = null;

    //         try {
    //             els = doc.select("div.list-board li.list-item div.wr-subject");

    //             log.debug(els.toString());

    //             for(int i = els.size() -1; i >= 0; i--) {
    //                 Element item = els.get(i).select("a").get(1);
    //                 String title = item.text().replaceFirst("N", "");
    //                 String magnet = getTorrentLink4(item.absUrl("href"));

    //                 log.debug("rss4: {}, {}", new Object[]{title, magnet});

    //                 rssFeedList.add(makeFeed(title, magnet, rss));
    //             }

    //         } catch ( Exception e) {
    //             log.error(baseUrl4+ " / " + e.toString());
    //         }
    //     }

    //     return rssFeedList;
    // }

    // private String getTorrentLink4(String urlString) throws Exception {
    //     Document doc = getDoc(urlString);

    //     Element el = doc.select("tbody tr td ul li").first();

    //     return "magnet:?xt=urn:btih:" + el.text().replace("Info Hash:", "").trim();
    // }

    // private String getMagnetString2(String urlString) throws Exception {
    //     Document doc = getDoc(urlString);
    //     Element el = doc.selectFirst("div + b + a");

    //     return el.attr("href");
    // }

    // private List<RssFeed> makeRss5(RssList rss) {
    //     log.info("Load RSS Site5 : {}, {} ", rss.getName(), rss.getUrl());

    //     sessionId = null;

    //     List<RssFeed> rssFeedList = new ArrayList<>();

    //     for(int page = 1; page <= maxPage5; page++ ) {
    //         String targetBoard = null;

    //         for(int i = 0; i < tvBoards1.length; i++) {
    //             if(StringUtils.equals(tvBoards1[i], rss.getUrl())) {                    
    //                 targetBoard = tvBoards5[i];
    //             }
    //         }

    //         if(StringUtils.isBlank(targetBoard)) {
    //             return rssFeedList;
    //         }

    //         String url = baseUrl5 + "/" + targetBoard + "?&" + pageHtml5 + "=" + page;
    //         Document doc = getDoc(url);

    //         Elements els = null;

    //         try {
    //             //<div class="flex-auto px-2 truncate">
    //             els = doc.select("div.flex-auto.px-2.truncate");

    //             log.debug(els.toString());

    //             for(int i = els.size() -1; i >= 0; i--) {
    //                 Element item = els.get(i).select("a").get(0);
    //                 String title = item.attr("title");
    //                 String magnet = getTorrentLink5(item.absUrl("href"));

    //                 log.debug("rss5: {}, {}", new Object[]{title, magnet});

    //                 rssFeedList.add(makeFeed(title, magnet, rss));
    //             }

    //         } catch ( Exception e) {
    //             log.error(baseUrl5+ " / " + e.toString());
    //         }
    //     }

    //     return rssFeedList;
    // }

    // private String getTorrentLink5(String urlString) throws Exception {
    //     Document doc = getDoc(urlString);

    //     // <div class="border-b">
    //     // <div class="p-2 border-b border-dashed"><span class="font-semibold">Info Hash:</span> ab9f75470bd1a51e7e89ffe8b163b81975fd3d7d</div>
    //     Element el = doc.select("div.border-b div.p-2.border-b.border-dashed").first();

    //     log.debug(el.toString());

    //     return "magnet:?xt=urn:btih:" + el.text().replace("Info Hash:", "").trim();
    // }

    private List<RssFeed> makeRss6(RssList rss) {
        log.info("Load RSS Site6 : {}, {} ", rss.getName(), rss.getUrl());

        sessionId = null;

        List<RssFeed> rssFeedList = new ArrayList<>();

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

            String url = baseUrl6 + "/" + targetBoard + "?" + pageHtml6 + "=" + page;
            Document doc = getDoc(url);

            Elements els = null;

            try {
                //<dd class="list_cut" style="padding-top:18px;">
                els = doc.select("dd.list_cut");

                log.debug(els.toString());

                for(int i = els.size() -1; i >= 0; i--) {
                    Element item = els.get(i).select("a").get(0);
                    String title = item.text();
                    String magnet = getTorrentLink6(item.absUrl("href"));

                    log.debug("rss6: {}, {}", new Object[]{title, magnet});

                    rssFeedList.add(makeFeed(title, magnet, rss));

                    Thread.sleep(SLEEP_SECOND * 1000);
                }

            } catch ( Exception e) {
                log.error(baseUrl6+ " / " + e.toString());
            }
        }

        return rssFeedList;
    }

    private String getTorrentLink6(String urlString) throws Exception {
        Document doc = getDoc(urlString);

        Element el = doc.select("section table a[href]").first();

        log.debug("getTorrentLink6 {} {}", urlString, el.toString());

        return el.attr("href");
    }
    
}