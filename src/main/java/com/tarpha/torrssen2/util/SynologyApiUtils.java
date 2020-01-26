package com.tarpha.torrssen2.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.annotation.PostConstruct;

import com.tarpha.torrssen2.service.CryptoService;
import com.tarpha.torrssen2.service.SettingService;

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
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SynologyApiUtils {
    @Autowired
    protected SettingService settingService;

    @Autowired
    protected CryptoService cryptoService;

    private String username;

    private String password;

    protected String baseUrl;

    protected CloseableHttpClient httpClient = null;

    @Getter
    protected String sid = null;

    protected String session = "DownloadStation";

    @PostConstruct
    protected void setBaseUrl() {
        this.baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                + settingService.getSettingValue("DS_PORT") + "/webapi";
    }

    protected boolean initialize() {
        log.debug("Initialize File Station Http Client");
        HttpResponse response = null;

        this.username = settingService.getSettingValue("DS_USERNAME");
        try {
            password = cryptoService.decrypt(settingService.getSettingValue("DS_PASSWORD"));
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            log.error(e.getMessage());
        }
        this.baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                + settingService.getSettingValue("DS_PORT") + "/webapi";

        if (StringUtils.isEmpty(this.sid)) {
            try {
                URIBuilder builder = new URIBuilder(this.baseUrl + "/auth.cgi");
                builder.setParameter("api", "SYNO.API.Auth").setParameter("version", "3")
                        .setParameter("method", "login").setParameter("session", session).setParameter("format", "sid")
                        .setParameter("account", this.username).setParameter("passwd", this.password);

                log.debug(builder.toString());

                HttpGet httpGet = new HttpGet(builder.build());

                try {
                    httpClient = HttpClientBuilder.create().build();
                    response = httpClient.execute(httpGet);

                    log.debug("init-response-code: " + response.getStatusLine().getStatusCode());
                    JSONObject resJson = new JSONObject(EntityUtils.toString(response.getEntity()));

                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        if (resJson.getJSONObject("data").has("sid")) {
                            this.sid = resJson.getJSONObject("data").getString("sid");
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }

            } catch (URISyntaxException | ParseException | JSONException e) {
                log.error(e.getMessage());
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        }

        if (StringUtils.isEmpty(this.sid)) {
            return false;
        }

        return true;
    }

    public boolean test(String host, String port, String id, String pwd) {
        boolean ret = false;
        HttpResponse response = null;
        String url = "http://" + host + ":" + port + "/webapi";

        if(StringUtils.equals(settingService.getSettingValue("DS_PASSWORD"), pwd)) {
            try {
                pwd = cryptoService.decrypt(pwd);
            } catch (UnsupportedEncodingException | GeneralSecurityException e) {
                log.error(e.getMessage());
            }
        }

        try {
            URIBuilder builder = new URIBuilder(url+ "/auth.cgi");
            builder.setParameter("api", "SYNO.API.Auth").setParameter("version", "3")
                    .setParameter("method", "login").setParameter("session", session).setParameter("format", "sid")
                    .setParameter("account", id).setParameter("passwd", pwd);

            log.debug(builder.toString());

            HttpGet httpGet = new HttpGet(builder.build());

            try {
                httpClient = HttpClientBuilder.create().build();
                response = httpClient.execute(httpGet);

                log.debug("init-response-code: " + response.getStatusLine().getStatusCode());
                JSONObject resJson = new JSONObject(EntityUtils.toString(response.getEntity()));

                if (Boolean.parseBoolean(resJson.get("success").toString())) {
                    if (resJson.getJSONObject("data").has("sid")) {
                        if(!StringUtils.isEmpty(resJson.getJSONObject("data").getString("sid"))) {
                            ret = true;
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
        
        httpClient = null;
        return ret;
    }

    protected JSONObject executeGet(URIBuilder builder) {
        JSONObject ret = null;
        CloseableHttpResponse response = null;

        log.debug(builder.toString());

        if (httpClient == null) {
            this.sid = null;
            initialize();
        }

        try {
            HttpGet httpGet = new HttpGet(builder.build());
            response = httpClient.execute(httpGet);
            log.debug("get-response-code: " + response.getStatusLine().getStatusCode());

            ret = new JSONObject(EntityUtils.toString(response.getEntity()));
            log.debug(ret.toString());

            if (ret.has("success") && ret.has("error")) {
                if (Boolean.parseBoolean(ret.get("success").toString()) == false) {
                    if (ret.getJSONObject("error").has("code")) {
                        int resCode = ret.getJSONObject("error").getInt("code");
                        if (resCode == 105 || resCode == 401) {
                            this.sid = null;
                            httpClient.close();
                            initialize();

                            if (httpClient != null) {
                                response.close();
                                builder.setParameter("_sid", this.sid);
                                httpGet = new HttpGet(builder.build());
                                response = httpClient.execute(httpGet);
                                ret = new JSONObject(EntityUtils.toString(response.getEntity()));
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
            httpClient = null;
        }
        HttpClientUtils.closeQuietly(response);

        return ret;
    }

    protected JSONObject executePost(List<NameValuePair> form) {
        JSONObject ret = null;
        CloseableHttpResponse response = null;

        if (httpClient == null) {
            this.sid = null;
            initialize();
            ;
        }

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("_sid", this.sid);

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, StandardCharsets.UTF_8);

            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setEntity(entity);

            response = httpClient.execute(httpPost);
            log.debug("post-response-code: " + response.getStatusLine().getStatusCode());

            ret = new JSONObject(EntityUtils.toString(response.getEntity()));

            if (ret.has("success") && ret.has("error")) {
                if (Boolean.parseBoolean(ret.get("success").toString()) == false) {
                    if (ret.getJSONObject("error").has("code")) {
                        if (ret.getJSONObject("error").getInt("code") == 105) {
                            this.sid = null;
                            httpClient.close();
                            initialize();

                            response.close();
                            builder.setParameter("_sid", this.sid);
                            httpPost = new HttpPost(builder.build());
                            response = httpClient.execute(httpPost);
                            ret = new JSONObject(EntityUtils.toString(response.getEntity()));
                        }
                    }
                }
            }

        } catch (IOException | URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
            httpClient = null;
        }
        HttpClientUtils.closeQuietly(response);

        return ret;
    }

}