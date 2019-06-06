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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DaumMovieTvService {
    
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    // https://suggest-bar.daum.net/suggest?id=movie&cate=tv&multiple=0&mod=json&code=utf_in_out&q=${query}&limit=10
    @Value("${daum-movie-tv.search-url}")
    private String baseUrl;

    @Value("${daum-movie-tv.limit}")
    private int limit;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public String getPoster(String query) {
        logger.info("Get Poster: " + query);
        CloseableHttpResponse response = null;

        try {
            URIBuilder builder = new URIBuilder(this.baseUrl);
            builder.setParameter("id", "movie").setParameter("multiple", "0").setParameter("mod", "json")
                    .setParameter("code", "utf_in_out").setParameter("limit", String.valueOf(limit))
                    .setParameter("q", query);

            HttpGet httpGet = new HttpGet(builder.build());
            response = httpClient.execute(httpGet);

            JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
            logger.debug(json.toString());

            if(json.has("items")) {
                if(json.getJSONArray("items").length() > 0) {
                    String[] arr = StringUtils.split(json.getJSONArray("items").getString(0), "|");

                    if(arr.length > 2) {
                        return arr[2];
                    }
                }   
            }
        } catch (URISyntaxException | IOException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        
        return null;
    }

}