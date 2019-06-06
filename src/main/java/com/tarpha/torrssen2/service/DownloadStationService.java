package com.tarpha.torrssen2.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadStationService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private CryptoService cryptoService;

    // @Value("${download-station.username}")
    private String username;

    // @Value("${download-station.password}")
    private String password;

    // @Value("http://${download-station.host}:${download-station.port}/webapi")
    private String baseUrl;

    private CloseableHttpClient httpClient = null;

    private String sid = null;

    public void initialize() {
        logger.info("Initialize Download Station Http Client");
        HttpResponse response = null;

        this.username = settingService.getSettingValue("DS_USERNAME");
        // this.password = settingService.getSettingValue("DS_PASSWORD");
        try {
            password = cryptoService.decrypt(settingService.getSettingValue("DS_PASSWORD"));
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            logger.error(e.getMessage());
        }
        this.baseUrl = "http://" +
            settingService.getSettingValue("DS_HOST") +
            ":" + 
            settingService.getSettingValue("DS_PORT") +
            "/webapi";

        if (StringUtils.isEmpty(sid)) {
            try {
                URIBuilder builder = new URIBuilder(this.baseUrl + "/auth.cgi");
                builder.setParameter("api", "SYNO.API.Auth").setParameter("version", "2")
                        .setParameter("method", "login").setParameter("session", "DownloadStation")
                        .setParameter("format", "sid").setParameter("account", this.username)
                        .setParameter("passwd", this.password);

                HttpGet httpGet = new HttpGet(builder.build());

                try {
                    httpClient = HttpClientBuilder.create().build();
                    response = httpClient.execute(httpGet);

                    logger.debug("init-response-code: " + response.getStatusLine().getStatusCode());
                    JSONObject resJson = new JSONObject(EntityUtils.toString(response.getEntity()));

                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        if(resJson.getJSONObject("data").has("sid")) {
                            this.sid = resJson.getJSONObject("data").getString("sid");
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }

            } catch (URISyntaxException | ParseException | JSONException e) {
                logger.error(e.getMessage());
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        }
    }

    private JSONObject executeGet(URIBuilder builder) {
        JSONObject ret = null;
        CloseableHttpResponse response = null;

        if(httpClient == null) {
            initialize();;
        }

        try {
            HttpGet httpGet = new HttpGet(builder.build());
            response = httpClient.execute(httpGet);
            logger.debug("get-response-code: " + response.getStatusLine().getStatusCode());

            ret = new JSONObject(EntityUtils.toString(response.getEntity()));
            logger.debug(ret.toString());

            if(ret.has("success") && ret.has("error")) {
                if(Boolean.parseBoolean(ret.get("success").toString()) == false ) {
                    if(ret.getJSONObject("error").has("code")) {
                        if (ret.getJSONObject("error").getInt("code") == 105) {
                            this.sid = null;
                            httpClient.close();
                            initialize();
        
                            response.close();
                            builder.setParameter("_sid", this.sid);
                            httpGet = new HttpGet(builder.build());
                            response = httpClient.execute(httpGet);
                            ret = new JSONObject(EntityUtils.toString(response.getEntity()));
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return ret;
    }

    private JSONObject executePost(List<NameValuePair> form) {
        JSONObject ret = null;
        CloseableHttpResponse response = null;

        if(httpClient == null) {
            initialize();;
        }

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("_sid", this.sid);

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, StandardCharsets.UTF_8);
            
            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setEntity(entity);
            
            response = httpClient.execute(httpPost);
            logger.debug("post-response-code: " + response.getStatusLine().getStatusCode());

            ret = new JSONObject(EntityUtils.toString(response.getEntity()));

            if(ret.has("success") && ret.has("error")) {
                if(Boolean.parseBoolean(ret.get("success").toString()) == false ) {
                    if(ret.getJSONObject("error").has("code")) {
                        if (ret.getJSONObject("error").getInt("code") == 105) {
                            this.sid = null;
                            httpClient.close();
                            initialize();

                            response.close();
                            builder.setParameter("_sid", this.sid);
                            httpPost = new HttpPost(builder.build());
                            response = httpClient.execute(httpPost);
                            ret= new JSONObject(EntityUtils.toString(response.getEntity()));
                        }
                    }
                }
            }

        } catch (IOException | URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return ret;
    }

    private DownloadList setInfo(JSONObject json) throws JSONException {
        DownloadList down = new DownloadList();

        boolean done = StringUtils.equals(json.getString("status"), "finished");

        Long id = Long.parseLong(StringUtils.remove(json.getString("id"), "dbid_"));

        down.setId(id);
        down.setDbid(json.getString("id"));
        down.setDone(done);
        down.setName(json.getString("title"));
        down.setFileName(json.getString("title"));
        down.setUri(json.getJSONObject("additional").getJSONObject("detail").getString("uri"));
        down.setDownloadPath(json.getJSONObject("additional").getJSONObject("detail").getString("destination"));

        Optional<DownloadList> info = downloadListRepository.findById(id);
        if(info.isPresent()) {
            down.setVueItemIndex(info.get().getVueItemIndex());
        }

        try {
            Long sizeDownloaded = json.getJSONObject("additional").getJSONObject("transfer").getLong("size_downloaded");
            Long fileSize = json.getLong("size");
            logger.debug("percent-done: " + sizeDownloaded.toString() + " / " + fileSize.toString() + " = " + String.valueOf(((double)sizeDownloaded / (double)fileSize) * 100));
            if(fileSize > 0L) {
                down.setPercentDone((int)(((double)sizeDownloaded / (double)fileSize) * 100));
            }
        } catch (ArithmeticException ae) {
            logger.error(ae.getMessage());
        }

        return down;
    }

    public List<DownloadList> list() {
        logger.info("Download Station list");
        List<DownloadList> ret = new ArrayList<DownloadList>();

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "list").setParameter("additional", "detail,transfer")
                    .setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);
            logger.debug("list: " + resJson.toString());

            if(Boolean.parseBoolean(resJson.get("success").toString())) {
                JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("tasks");

                for(int i = 0; i < jsonArray.length(); i ++) {
                    DownloadList down = setInfo(jsonArray.getJSONObject(i));
                    ret.add(down);
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 
        
        return ret;
    }

    public DownloadList getInfo(String id) {
        logger.info("Download Station getInfo");
        DownloadList ret = null;

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "getinfo").setParameter("additional", "detail,transfer")
                    .setParameter("id", id).setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);
            logger.debug("getInfo: " + resJson.toString());

            if(Boolean.parseBoolean(resJson.get("success").toString())) {
                JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("tasks");

                ret = setInfo(jsonArray.getJSONObject(0));
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

    public List<DownloadList> getInfo(List<String> ids) {
        logger.info("Download Station getInfo");
        List<DownloadList> ret = new ArrayList<DownloadList>();

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "getinfo").setParameter("additional", "detail,transfer")
                    .setParameter("id", StringUtils.join(ids, ",")).setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);
            logger.debug("getInfo: " + resJson.toString());

            if(Boolean.parseBoolean(resJson.get("success").toString())) {
                JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("tasks");

                for(int i = 0; i < jsonArray.length(); i++) {
                    ret.add(setInfo(jsonArray.getJSONObject(i)));
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

    public boolean create(String uri, String downloadDir) {
        logger.info("Download Station create");
        boolean ret = false;

        List<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("api", "SYNO.DownloadStation.Task"));
        form.add(new BasicNameValuePair("version", "3"));
        form.add(new BasicNameValuePair("method", "create"));
        form.add(new BasicNameValuePair("uri", uri));
        if(!StringUtils.isBlank(downloadDir)) {
            if(StringUtils.startsWith(downloadDir, "/")) {
                form.add(new BasicNameValuePair("destination", StringUtils.substring(downloadDir, 1)));
            } else {
                form.add(new BasicNameValuePair("destination", downloadDir));
            }
        }

        logger.debug("form: " + form.toString());

        JSONObject resJson = executePost(form);

        logger.debug("responase: " + resJson.toString());

        try {
            if(Boolean.parseBoolean(resJson.get("success").toString())) {
                ret = true;
            } 
        } catch (JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

    public boolean delete(List<String> ids) {
        logger.info("Download Station delete");
        boolean ret = true;
        
        try {
            URIBuilder builder = new URIBuilder(this.baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "delete").setParameter("force_complete", "false")
                    .setParameter("id", StringUtils.join(ids, ",")).setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);
            
            if(resJson.has("data")) {
                if(resJson.getJSONObject("data").has("error")) {
                    if(resJson.getJSONObject("data").getInt("error") > 0) {
                        ret = false;
                    }
                }
            } 

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

    public String getDbId(Long id) {
        String ret = null;

        Optional<DownloadList> info = downloadListRepository.findById(id);
        if(info.isPresent()) {
            ret = info.get().getDbid();
        }

        return ret;
    }
    
}