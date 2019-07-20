package com.tarpha.torrssen2.service;

import java.net.URISyntaxException;
import java.util.List;

import com.tarpha.torrssen2.util.SynologyApiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileStationService extends SynologyApiUtils {

    public FileStationService() {
        session = "FileStation";
    }

    public boolean createFolder(String path, String name) {
        log.info("File Station createFolder");
        boolean ret = false;

        try {
            baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                    + settingService.getSettingValue("DS_PORT") + "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.CreateFolder").setParameter("version", "2")
                    .setParameter("method", "create").setParameter("folder_path", "[\"" + path + "\"]")
                    .setParameter("name", "[\"" + name + "\"]").setParameter("force_parent", "true")
                    .setParameter("_sid", sid);

            JSONObject resJson = executeGet(builder);

            log.debug(builder.toString());

            if (resJson != null) {
                log.debug(resJson.toString());
                if (resJson.has("success")) {
                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        // JSONArray jsonArray = resJson.getJSONObject("data").getJSONArray("folders");
                        ret = true;
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        }

        return ret;
    }

    public JSONArray list(String path, String name) {
        try {
            baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                    + settingService.getSettingValue("DS_PORT") + "/webapi";

            String folderPath = path + "/" + name;

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.List").setParameter("version", "2")
                    .setParameter("method", "list").setParameter("folder_path", folderPath).setParameter("_sid", sid);

            JSONObject resJson = executeGet(builder);

            log.debug(builder.toString());

            if (resJson != null) {
                log.debug(resJson.toString());
                if (resJson.has("success")) {
                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        if (resJson.has("data")) {
                            if (resJson.getJSONObject("data").has("files")) {
                                return resJson.getJSONObject("data").getJSONArray("files");
                            }
                        }
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    public String move(List<String> paths, String dest) {
        log.info("File Station CopyMove");
        String taskId = null;

        try {
            baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                    + settingService.getSettingValue("DS_PORT") + "/webapi";

            String path = "[\"" + StringUtils.join(paths, "\",\"") + "\"]";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.CopyMove").setParameter("version", "3")
                    .setParameter("method", "start").setParameter("path", path).setParameter("dest_folder_path", dest)
                    .setParameter("overwrite", "true").setParameter("remove_src", "true").setParameter("_sid", sid);

            JSONObject resJson = executeGet(builder);

            log.debug(builder.toString());

            if (resJson != null) {
                log.debug(resJson.toString());
                if (resJson.has("success")) {
                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        if (resJson.has("data")) {
                            if (resJson.getJSONObject("data").has("taskid")) {
                                taskId = resJson.getJSONObject("data").getString("taskid");
                            }
                        }
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        }

        return taskId;
    }

    public int moveTask(String taskId) {
        int ret = 0;

        try {
            baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                    + settingService.getSettingValue("DS_PORT") + "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.CopyMove").setParameter("version", "3")
                    .setParameter("method", "status").setParameter("taskid", taskId).setParameter("_sid", sid);

            JSONObject resJson = executeGet(builder);

            log.debug(builder.toString());

            if (resJson != null) {
                log.debug(resJson.toString());
                if (resJson.has("success")) {
                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        if (resJson.has("data")) {
                            JSONObject jsonObj = resJson.getJSONObject("data");
                            if (jsonObj.has("finished")) {
                                if (jsonObj.getBoolean("finished")) {
                                    ret = 1;
                                    if (jsonObj.has("status")) {
                                        if (StringUtils.equals(jsonObj.getString("status"), "FAIL")) {
                                            ret = -1;
                                        }
                                    }
                                }
                                ;
                            }
                        }
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        }

        return ret;
    }

    public boolean delete(String path) {
        log.info("File Station Delete");
        boolean ret = false;

        try {
            baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                    + settingService.getSettingValue("DS_PORT") + "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.Delete").setParameter("version", "2")
                    .setParameter("method", "start").setParameter("path", path).setParameter("recursive", "true")
                    .setParameter("_sid", sid);

            JSONObject resJson = executeGet(builder);

            log.debug(builder.toString());

            if (resJson != null) {
                log.debug(resJson.toString());
                if (resJson.has("success")) {
                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        ret = true;
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        }

        return ret;
    }

    public boolean rename(String path, String name) {
        log.info("File Station Rename");
        boolean ret = false;

        try {
            baseUrl = "http://" + settingService.getSettingValue("DS_HOST") + ":"
                    + settingService.getSettingValue("DS_PORT") + "/webapi";

            URIBuilder builder = new URIBuilder(baseUrl + "/entry.cgi");
            builder.setParameter("api", "SYNO.FileStation.Rename").setParameter("version", "2")
                    .setParameter("method", "rename").setParameter("path", path).setParameter("name", name)
                    .setParameter("_sid", sid);

            JSONObject resJson = executeGet(builder);

            log.debug(builder.toString());

            if (resJson != null) {
                log.debug(resJson.toString());
                if (resJson.has("success")) {
                    if (Boolean.parseBoolean(resJson.get("success").toString())) {
                        ret = true;
                    }
                }
            }

        } catch (URISyntaxException | ParseException | JSONException e) {
            log.error(e.getMessage());
        }

        return ret;
    }

}