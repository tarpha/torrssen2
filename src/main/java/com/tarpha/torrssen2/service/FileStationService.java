package com.tarpha.torrssen2.service;

import java.io.File;
import java.net.URISyntaxException;

import com.tarpha.torrssen2.util.SynologyApiUtils;

import org.apache.http.ParseException;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
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

    public JSONArray list(String path, String name) {
        try {
            baseUrl = "http://" +
                settingService.getSettingValue("DS_HOST") +
                ":" + 
                settingService.getSettingValue("DS_PORT") +
                "/webapi";

            String folderPath = path + File.separator + name;

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.List").setParameter("version", "2")
                    .setParameter("method", "list").setParameter("folder_path", folderPath);

            JSONObject resJson = executeGet(builder);

            logger.debug(builder.toString());

            if(resJson != null) {
                logger.debug(resJson.toString());
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        if(resJson.has("data")) {
                            if(resJson.getJSONObject("data").has("files")) {
                                return resJson.getJSONObject("data").getJSONArray("files");
                            }
                        }
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return null;
    }

    public boolean move(String path, String dest) {
        logger.info("File Station CopyMove");
        boolean ret = false;

        try {
            baseUrl = "http://" +
                settingService.getSettingValue("DS_HOST") +
                ":" + 
                settingService.getSettingValue("DS_PORT") +
                "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.CopyMove").setParameter("version", "3")
                    .setParameter("method", "start").setParameter("path", path)
                    .setParameter("dest_folder_path", dest).setParameter("overwrite", "true")
                    .setParameter("remove_src", "true");

            JSONObject resJson = executeGet(builder);

            logger.debug(builder.toString());

            if(resJson != null) {
                logger.debug(resJson.toString());
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        ret = true;
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

    public boolean delete(String path) {
        logger.info("File Station Delete");
        boolean ret = false;

        try {
            baseUrl = "http://" +
                settingService.getSettingValue("DS_HOST") +
                ":" + 
                settingService.getSettingValue("DS_PORT") +
                "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.Delete").setParameter("version", "2")
                    .setParameter("method", "start").setParameter("path", path)
                    .setParameter("recursive", "true");

            JSONObject resJson = executeGet(builder);

            logger.debug(builder.toString());

            if(resJson != null) {
                logger.debug(resJson.toString());
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
                        ret = true;
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            logger.error(e.getMessage());
        } 

        return ret;
    }

    public boolean rename(String path, String name) {
        logger.info("File Station Rename");
        boolean ret = false;

        try {
            baseUrl = "http://" +
                settingService.getSettingValue("DS_HOST") +
                ":" + 
                settingService.getSettingValue("DS_PORT") +
                "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.Rename").setParameter("version", "2")
                    .setParameter("method", "rename").setParameter("path", path)
                    .setParameter("name", name);

            JSONObject resJson = executeGet(builder);

            logger.debug(builder.toString());

            if(resJson != null) {
                logger.debug(resJson.toString());
                if(resJson.has("success")) {
                    if(Boolean.parseBoolean(resJson.get("success").toString())) {
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