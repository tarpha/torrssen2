package com.tarpha.torrssen2.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Service
public class TransmissionService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private SettingService settingService;

    @Autowired
    private CryptoService cryptoService;

    // @Value("${transmission.username}")
    private String username;

    // @Value("${transmission.password}")
    private String password;

    // @Value("http://${transmission.host}:${transmission.port}/transmission/rpc")
    private String baseUrl;

    private CloseableHttpClient httpClient = null;

    private String xTransmissionSessionId = null;

    @Getter
    @Setter
    private class TransmissionVO {

        private String result;
        private JSONObject arguments;

    }

    public boolean initialize() {
        username = settingService.getSettingValue("TRANSMISSION_USERNAME");
        try {
            password = cryptoService.decrypt(settingService.getSettingValue("TRANSMISSION_PASSWORD"));
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            logger.error(e.getMessage());
        }
        baseUrl = "http://" +
            settingService.getSettingValue("TRANSMISSION_HOST") +
            ":" + 
            settingService.getSettingValue("TRANSMISSION_PORT") +
            "/transmission/rpc";

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        
        if (StringUtils.isEmpty(xTransmissionSessionId)) {
            CloseableHttpResponse response = null;

            try {
                httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

                response = httpClient.execute(new HttpGet(baseUrl));
                xTransmissionSessionId = response.getFirstHeader("X-Transmission-Session-Id").getValue();
            } catch (IOException e) {
                logger.error(e.getMessage());
            } finally {
                HttpClientUtils.closeQuietly(response);
                HttpClientUtils.closeQuietly(httpClient);
            }
        }

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        headers.add(new BasicHeader("X-Transmission-Session-Id", xTransmissionSessionId));

        httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).setDefaultHeaders(headers)
                .build();
        
        if(StringUtils.isEmpty(xTransmissionSessionId)) {
            return false;
        } 

        return true;
    }

    public boolean test(String host, String port, String id, String pwd) {
        boolean ret = false;

        String url = "http://" + host + ":" + port + "/transmission/rpc";

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(id, pwd);
        provider.setCredentials(AuthScope.ANY, credentials);

        CloseableHttpResponse response = null;

        try {
            httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

            response = httpClient.execute(new HttpGet(url));
            if(response.getFirstHeader("X-Transmission-Session-Id") != null) {
                if(!StringUtils.isEmpty(response.getFirstHeader("X-Transmission-Session-Id").getValue())) {
                    ret = true;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
        
        return ret;
    }

    private TransmissionVO execute(JSONObject params) {
        TransmissionVO ret = null;

        if(httpClient == null) {
            initialize();
        }

        HttpPost httpPost = new HttpPost(baseUrl);
        CloseableHttpResponse response = null;

        logger.debug(params.toString());

        try {
            httpPost.setEntity(new StringEntity(params.toString(), "UTF-8"));
            response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 409) {
                xTransmissionSessionId = response.getFirstHeader("X-Transmission-Session-Id").getValue();
                response.close();
                httpClient.close();
                initialize();

                response = httpClient.execute(httpPost);
            }

            logger.debug("transmission-execute-response-code: " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject resJson = new JSONObject(EntityUtils.toString(response.getEntity()));

                logger.debug(resJson.toString());

                ret = new TransmissionVO();

                if (resJson.has("result")) {
                    ret.setResult(resJson.getString("result"));
                }
                if (resJson.has("arguments")) {
                    ret.setArguments(resJson.getJSONObject("arguments"));
                }
            }

        } catch (IOException | ParseException | JSONException e) {
            logger.error(e.getMessage());
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
            httpClient = null;
        } 
        HttpClientUtils.closeQuietly(response);

        return ret;
    }

    public int torrentAdd(String filename, String downloadDir) {
        int ret = -1;

        JSONObject params = new JSONObject();

        try {
            params.put("method", "torrent-add");

            JSONObject args = new JSONObject();
            args.put("filename", filename);
            if (!StringUtils.isEmpty(downloadDir)) {
                args.put("download-dir", downloadDir);
            }
            logger.debug(args.toString());

            params.put("arguments", args);

            TransmissionVO vo = execute(params);
            if (vo != null) {
                if(vo.getArguments().has("torrent-added")) {
                    ret = vo.getArguments().getJSONObject("torrent-added").getInt("id");
                } else if(vo.getArguments().has("torrent-duplicate")) {
                    ret = -2;
                }
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        return ret;
    }

    public List<DownloadList> torrentGet(List<Long> ids) {
        List<DownloadList> ret = new ArrayList<DownloadList>();

        JSONObject params = new JSONObject();

        try {
            params.put("method", "torrent-get");

            JSONObject args = new JSONObject();
            // transmission.status =
            //   STOPPED       : 0  # Torrent is stopped
            //   CHECK_WAIT    : 1  # Queued to check files
            //   CHECK         : 2  # Checking files
            //   DOWNLOAD_WAIT : 3  # Queued to download
            //   DOWNLOAD      : 4  # Downloading
            //   SEED_WAIT     : 5  # Queued to seed
            //   SEED          : 6  # Seeding
            //   ISOLATED      : 7  # Torrent can't find peers
            args.put("fields"
                , new JSONArray(new String[]{"id", "name", "totalSize", "percentDone", "status", "downloadDir", "isFinished", "magnetLink"}));
            if(ids != null && ids.size() > 0) {
                args.put("ids", new JSONArray(ids));
            }
            logger.debug(args.toString());

            params.put("arguments", args);

            TransmissionVO vo = execute(params);
            if (vo != null) {
                if (vo.getArguments().has("torrents")) {
                    for(int i = 0; i < vo.getArguments().getJSONArray("torrents").length(); i++) {
                        JSONObject json = vo.getArguments().getJSONArray("torrents").getJSONObject(i);
                        DownloadList down = new DownloadList();
                        down.setId(json.getLong("id"));
                        down.setUri(json.getString("magnetLink"));
                        down.setName(json.getString("name"));
                        down.setDownloadPath(json.getString("downloadDir"));
                        down.setPercentDone((int)(json.getDouble("percentDone") * 100));
                        down.setStatus(json.getInt("status"));
                        down.setDone((boolean)json.get("isFinished") || json.getInt("status") == 6);

                        Optional<DownloadList> info = downloadListRepository.findById(json.getLong("id"));
                        if(info.isPresent()) {
                            down.setVueItemIndex(info.get().getVueItemIndex());
                        }

                        ret.add(down);
                    }
                }
            }

        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        return ret;
    }

    public boolean torrentRemove(List<Long> ids) {
        boolean ret = true;

        JSONObject params = new JSONObject();

        try {
            params.put("method", "torrent-remove");

            JSONObject args = new JSONObject();
            args.put("ids", new JSONArray(ids));
            args.put("delete-local-data", false);
            logger.debug(args.toString());

            params.put("arguments", args);

            TransmissionVO vo = execute(params);
            if (vo != null) {
                if (!StringUtils.equals(vo.getResult(), "success")) {
                    ret = false;
                }
            }

        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        return ret;
    }

}