package com.tarpha.torrssen2.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.util.CommonUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private DownloadStationService downloadStationService;

    @Autowired
    private FileStationService fileStationService;

    @Autowired
    private BtService btService;

    @Autowired
    private TelegramService telegramService;

    public void runTask() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("DOWNLOAD_APP");
        if (optionalSetting.isPresent()) {
            if (StringUtils.equals(optionalSetting.get().getValue(), "TRANSMISSION")) {
                transmissionJob();
            } else if (StringUtils.equals(optionalSetting.get().getValue(), "DOWNLOAD_STATION")) {
                downloadStationJob();
            } else if (StringUtils.equals(optionalSetting.get().getValue(), "EMBEDDED")) {
                btService.check();
            }
        }
    }

    public void killTask() {
        logger.debug("killTask");
        Optional<Setting> optionalSetting = settingRepository.findByKey("USE_CRON");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                try {
                    while (btService.list().size() > 0) {
                        Thread.sleep(60000);
                    }
                    File script = new File(File.separator, "kill.sh");
                    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
                    if (script.isFile()) {
                        if (!isWindows) {
                            logger.debug("run kill.sh");
                            Runtime.getRuntime().exec(String.format("sh -c %s", File.separator + "kill.sh"));
                        }
                    } else {
                        logger.debug("run ps -ef kill");
                        Runtime.getRuntime().exec("ps - ef | grep torrssen2.jar | awk '{print $1}' | xargs kill");
                    }
                } catch (InterruptedException | IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public void transmissionJob() {
        boolean doneDelete = false;
        boolean sendTelegram = false;
        boolean transmissionCallback = false;

        // 완료 시 삭제 여부
        Optional<Setting> optionalSetting = settingRepository.findByKey("DONE_DELETE");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                doneDelete = true;
            }
        }

        // Telegram 발송 여부
        optionalSetting = settingRepository.findByKey("SEND_TELEGRAM");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                sendTelegram = true;
            }
        }

        // TRANSMISSION_CALLBACK
        optionalSetting = settingRepository.findByKey("TRANSMISSION_CALLBACK");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                transmissionCallback = true;
            }
        }

        logger.info("=== Transmission Stop Seeding ===");
        List<DownloadList> list = transmissionService.torrentGet(null);
        List<Long> ids = new ArrayList<Long>();
        for (DownloadList down : list) {
            // SEED: 6 # Seeding
            if (down.getStatus() == 6) {
                Optional<DownloadList> rdown = downloadListRepository.findById(down.getId());
                if (rdown.isPresent()) {
                    down.setIsSentAlert(rdown.get().getIsSentAlert());
                    downloadListRepository.save(down);

                    ids.add(down.getId());
                    if (sendTelegram && !transmissionCallback) {
                        sendTelegram(down);
                    }

                    logger.debug("removeDirectory");
                    List<String> inners = CommonUtils.removeDirectory(down.getDownloadPath(), down.getName(),
                            settingRepository);

                    String rename = rdown.get().getRename();
                    if (!StringUtils.isBlank(rename)) {
                        logger.debug("getRename: " + rename);
                        if (inners == null) {
                            CommonUtils.renameFile(down.getDownloadPath(), down.getName(), rename);
                        } else {
                            for (String name : inners) {
                                if (StringUtils.contains(down.getName(), name)) {
                                    CommonUtils.renameFile(down.getDownloadPath(), name, rename);
                                }
                            }
                        }
                    }
                }   
            }
        }

        if (ids.size() > 0 && doneDelete) {
            transmissionService.torrentRemove(ids);
        }
    }

    public void downloadStationJob() {
        // 다운로드 스테이션 완료 체크
        logger.info("=== Download Station Check Done ===");
        List<DownloadList> list = downloadStationService.list();

        boolean doneDelete = false;
        boolean sendTelegram = false;

        // 완료 시 삭제 여부
        Optional<Setting> optionalSetting = settingRepository.findByKey("DONE_DELETE");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                doneDelete = true;
            }
        }

        // Telegram 발송 여부
        optionalSetting = settingRepository.findByKey("SEND_TELEGRAM");
        if (optionalSetting.isPresent()) {
            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                sendTelegram = true;
            }
        }

        for (DownloadList down : list) {
            for (DownloadList tdown : downloadListRepository.findAllById(0L)) {
                if (StringUtils.equals(down.getUri(), tdown.getUri())) {
                    down.setRename(tdown.getRename());
                    downloadListRepository.save(down);
                }
            }

            if (down.getId() > 0) {
                Optional<DownloadList> rdown = downloadListRepository.findById(down.getId());
                if (rdown.isPresent()) {
                    down.setRename(rdown.get().getRename());
                    down.setIsSentAlert(rdown.get().getIsSentAlert());
                }
                downloadListRepository.save(down);

                if (down.getDone()) {
                    rdown = downloadListRepository.findFirstByUriAndDoneOrderByCreateDtDesc(down.getUri(), true);
                    if (rdown.isPresent()) {
                        DownloadList tdown = rdown.get();
                        // Telegram Message를 발송한다.
                        if (sendTelegram) {
                            sendTelegram(down);
                        }
                        // 완료 시 삭제 여부
                        if (doneDelete) {
                            logger.info("Remove Torrent: " + down.getFileName());

                            List<String> ids = new ArrayList<String>();
                            ids.add(tdown.getDbid());
                            downloadStationService.delete(ids);
                        }

                        Optional<Setting> delDirSetting = settingRepository.findByKey("DEL_DIR");

                        if (delDirSetting.isPresent()) {
                            if (Boolean.parseBoolean(delDirSetting.get().getValue())
                                    && !StringUtils.isEmpty(down.getName())) {

                                logger.debug("removeDirectory");
                                String path = down.getDownloadPath();
                                if (!StringUtils.startsWith(path, File.separator)) {
                                    path = File.separator + path;
                                }
                                JSONArray jsonArray = fileStationService.list(path, down.getName());
                                List<String> srcs = new ArrayList<>();
                                if (jsonArray != null) {
                                    String[] exts = {};
                                    Optional<Setting> exceptExtSetting = settingRepository.findByKey("EXCEPT_EXT");
                                    if (exceptExtSetting.isPresent()) {
                                        exts = StringUtils
                                                .split(StringUtils.lowerCase(exceptExtSetting.get().getValue()), ",");
                                    }

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);

                                        if (object.has("path")) {
                                            String temp = object.getString("path");
                                            if (!StringUtils.containsAny(temp, exts)) {
                                                srcs.add(temp);
                                            }
                                        }
                                    }

                                    if (srcs.size() > 0) {
                                        String taskId = fileStationService.move(srcs, path);
                                        tdown.setTask(true);
                                        tdown.setTaskId(taskId);
                                        tdown.setDeletePath(path + File.separator + down.getName());
                                        downloadListRepository.save(tdown);
                                        // fileStationService.delete(path + File.separator + down.getName());
                                    }
                                }

                                if (!StringUtils.isBlank(down.getRename())) {
                                    logger.debug("getRename: " + down.getRename());
                                    if (jsonArray == null) {
                                        fileStationService.rename(path + File.separator + down.getName(),
                                                path + File.separator + down.getRename());
                                    } else {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            if (object.has("name")) {
                                                String name = object.getString("name");
                                                if (StringUtils.contains(name, down.getName())) {
                                                    fileStationService.rename(path + File.separator + name,
                                                            down.getRename() + "." + FilenameUtils.getExtension(name));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<DownloadList> tasks = downloadListRepository.findByTask(true);
        for (DownloadList task : tasks) {
            logger.debug("moveDone: " + task.getTaskId());
            int resCode = fileStationService.moveTask(task.getTaskId());
            if (resCode != 0) {
                logger.debug("finished");
                if (resCode == 1) {
                    fileStationService.delete(task.getDeletePath());
                }
                task.setTask(false);
                downloadListRepository.save(task);
            }
        }
    }

    private void sendTelegram(DownloadList down) {
        Optional<DownloadList> rdown = downloadListRepository.findById(down.getId());
        logger.debug(rdown.toString());
        if (rdown.isPresent()) {
            if (rdown.get().getIsSentAlert() == false) {
                String target = StringUtils.isEmpty(down.getFileName()) ? rdown.get().getName() : down.getFileName();
                logger.info("Send Telegram: " + target);
                boolean ret = telegramService.sendMessage("<b>" + target + "</b>의 다운로드가 완료되었습니다.");
                logger.debug("send telegram result: " + ret);
                if (ret) {
                    DownloadList sdown = rdown.get();
                    sdown.setIsSentAlert(true);
                    logger.debug(sdown.toString());

                    downloadListRepository.save(sdown);
                }
            }
        }
    }

}