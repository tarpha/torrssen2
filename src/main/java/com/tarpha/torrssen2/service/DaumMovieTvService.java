package com.tarpha.torrssen2.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DaumMovieTvService {
    // https://suggest-bar.daum.net/suggest?id=movie&cate=tv&multiple=0&mod=json&code=utf_in_out&q=${query}&limit=10
    @Value("${daum-movie-tv.search-url}")
    private String baseUrl;

    @Value("${daum-movie-tv.limit}")
    private int limit;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public String getPoster(String query) {
        log.info("Get Poster: " + query);
        CloseableHttpResponse response = null;

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl);
            builder.setParameter("id", "movie").setParameter("multiple", "0").setParameter("mod", "json")
                    .setParameter("code", "utf_in_out").setParameter("limit", String.valueOf(limit))
                    .setParameter("q", query);

            HttpGet httpGet = new HttpGet(builder.build());
            response = httpClient.execute(httpGet);

            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
            log.debug(json.toString());

            if(json.has("items")) {
                JSONArray jarr = json.getJSONArray("items");
                for(int i = 0; i < jarr.length(); i++) {
                    String[] arr = StringUtils.split(jarr.getString(i), "|");

                    if(arr.length > 2) {
                        if(StringUtils.containsIgnoreCase(arr[0], query)) {
                            return arr[2];
                        }
                    }
                }   
            }
        } catch (URISyntaxException | IOException | ParseException | JSONException e) {
            log.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        
        return null;
    }

}