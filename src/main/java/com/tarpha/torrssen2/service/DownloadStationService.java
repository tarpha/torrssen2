package com.tarpha.torrssen2.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.util.SynologyApiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DownloadStationService extends SynologyApiUtils {

    @Autowired
    private DownloadListRepository downloadListRepository;

    private DownloadList setInfo(JSONObject json) throws JSONException {
        DownloadList down = new DownloadList();

        if(json.has("id")) {
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
        }

        return down;
    }

    public List<DownloadList> list() {
        logger.info("Download Station list");
        List<DownloadList> ret = new ArrayList<DownloadList>();

        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "list").setParameter("additional", "detail,transfer")
                    .setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);

            if(resJson != null) {
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        if(resJson.has("data")) {
                            if(resJson.getJSONObject("data").has("tasks")) {
                                JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("tasks");

                                for(int i = 0; i < jsonArray.length(); i ++) {
                                    DownloadList down = setInfo(jsonArray.getJSONObject(i));
                                    ret.add(down);
                                }
                            }
                        }
                    }
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
            URIBuilder builder = new URIBuilder(baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "getinfo").setParameter("additional", "detail,transfer")
                    .setParameter("id", id).setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);

            if(resJson != null) {
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("tasks");

                        ret = setInfo(jsonArray.getJSONObject(0));
                    }
                }
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
            URIBuilder builder = new URIBuilder(baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "getinfo").setParameter("additional", "detail,transfer")
                    .setParameter("id", StringUtils.join(ids, ",")).setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);

            if(resJson != null) {
                if(Boolean.parseBoolean(resJson.get("success").toString())) {
                    JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("tasks");

                    for(int i = 0; i < jsonArray.length(); i++) {
                        ret.add(setInfo(jsonArray.getJSONObject(i)));
                    }
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

        JSONObject resJson = executePost(form);

        if(resJson != null) {
            try {
                if(Boolean.parseBoolean(resJson.get("success").toString())) {
                    ret = true;
                } 
            } catch (JSONException e) {
                logger.error(e.getMessage());
            } 
        }

        return ret;
    }

    public boolean delete(List<String> ids) {
        logger.info("Download Station delete");
        boolean ret = true;
        
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/DownloadStation/task.cgi");
            builder.setParameter("api", "SYNO.DownloadStation.Task").setParameter("version", "3")
                    .setParameter("method", "delete").setParameter("force_complete", "false")
                    .setParameter("id", StringUtils.join(ids, ",")).setParameter("_sid", this.sid);

            JSONObject resJson = executeGet(builder);
            
            if(resJson != null) {
                if(resJson.has("data")) {
                    JSONArray jarray = resJson.getJSONArray("data");
                    for(int i = 0; i <jarray.length(); i++) {
                        if(jarray.getJSONObject(i).has("error")) {
                            if(jarray.getJSONObject(i).getInt("error") > 0) {
                                ret = false;
                                break;
                            }
                        }
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