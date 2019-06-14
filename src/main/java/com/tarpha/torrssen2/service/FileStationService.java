package com.tarpha.torrssen2.service;

import java.net.URISyntaxException;

import com.tarpha.torrssen2.util.SynologyApiUtils;

import org.apache.http.ParseException;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class FileStationService extends SynologyApiUtils {

    public FileStationService() {
        session = "FileStation";
    }

    public boolean createFolder(String path, String name) {
        logger.info("File Station createFolder");
        logger.debug(path + ":" + name);
        boolean ret = false;

        try {
            baseUrl = "http://" +
                settingService.getSettingValue("DS_HOST") +
                ":" + 
                settingService.getSettingValue("DS_PORT") +
                "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.CreateFolder").setParameter("version", "2")
                    .setParameter("method", "create").setParameter("folder_path", "[\"" + path + "\"]")
                    .setParameter("name", "[\"" + name + "\"]").setParameter("force_parent", "true");

            JSONObject resJson = executeGet(builder);

            logger.debug(builder.toString());

            if(resJson != null) {
                logger.debug(resJson.toString());
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        // JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("folders");
                        ret = true;
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

}