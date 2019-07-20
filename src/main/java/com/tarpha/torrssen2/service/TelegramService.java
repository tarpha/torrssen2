package com.tarpha.torrssen2.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class TelegramService {
    @Autowired
    private SettingService settingService;

    // @Value("${telegram.token}")
    private String token;

    // @Value("${telegram.chat-id}")
    private String[] chatIds;

    private String baseUrl;

    private CloseableHttpClient httpClient = null;

    private void initialize() {
        token = settingService.getSettingValue("TELEGRAM_TOKEN");
        chatIds = StringUtils.split(settingService.getSettingValue("TELEGRAM_CHAT_ID"), ",");
        baseUrl = "https://api.telegram.org/bot" + token + "/sendMessage";
        
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        httpClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    }

    public boolean sendMessage(String message) {
        boolean ret = false;

        if(httpClient == null) {
            initialize();
        }

        HttpPost httpPost = new HttpPost(baseUrl);
        CloseableHttpResponse response = null;

        try {
            for(String chatId: chatIds) {
                JSONObject params = new JSONObject();
                params.put("chat_id", chatId);
                params.put("text", message);
                params.put("parse_mode", "HTML");
                httpPost.setEntity(new StringEntity(params.toString(), StandardCharsets.UTF_8));

                response = httpClient.execute(httpPost);

                log.debug("telegram-send-message-response-code: " + response.getStatusLine().getStatusCode());
                if (response.getStatusLine().getStatusCode() == 200) {
                    ret = true;
                } 
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return ret;
    }

    public boolean sendMessage(String inToken, String chatId, String message) {
        boolean ret = false;

        baseUrl = "https://api.telegram.org/bot" + inToken + "/sendMessage";
        
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        httpClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();

        HttpPost httpPost = new HttpPost(baseUrl);
        CloseableHttpResponse response = null;

        log.debug("token:" + inToken);
        log.debug("chatId: " + chatId);

        try {
            JSONObject params = new JSONObject();
            params.put("chat_id", chatId);
            params.put("text", message);
            params.put("parse_mode", "HTML");
            httpPost.setEntity(new StringEntity(params.toString(), StandardCharsets.UTF_8));

            response = httpClient.execute(httpPost);

            log.debug("telegram-send-message-response-code: " + response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                ret = true;
            } 
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return ret;
    }

}