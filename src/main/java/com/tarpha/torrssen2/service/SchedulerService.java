package com.tarpha.torrssen2.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.SeenList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.SeenListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.util.CommonUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SchedulerService {
    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private SeenListRepository seenListRepository;

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
        log.debug("killTask");
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
                            log.debug("run kill.sh");
                            Runtime.getRuntime().exec(String.format("sh -c %s", File.separator + "kill.sh"));
                        }
                    } else {
                        log.debug("run ps -ef kill");
                        Runtime.getRuntime().exec("ps - ef | grep torrssen2.jar | awk '{print $1}' | xargs kill");
                    }
                } catch (InterruptedException | IOException e) {
                    log.error(e.getMessage());
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

        log.info("=== Transmission Stop Seeding ===");
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

                    log.debug("removeDirectory");
                    List<String> inners = CommonUtils.removeDirectory(down.getDownloadPath(), down.getName(),
                            settingRepository);

                    String rename = rdown.get().getRename();
                    if (!StringUtils.isBlank(rename)) {
                        log.debug("getRename: " + rename);
                        if (inners == null) {
                            boolean renameStatus = CommonUtils.renameFile(down.getDownloadPath(), down.getName(), rename);
                            setSeenList(down.getUri(), String.valueOf(renameStatus));
                        } else {
                            for (String name : inners) {
                                if (StringUtils.contains(down.getName(), name)) {
                                    boolean renameStatus = CommonUtils.renameFile(down.getDownloadPath(), name, rename);
                                    setSeenList(down.getUri(), String.valueOf(renameStatus));
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
        log.info("=== Download Station Check Done ===");
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
                    downloadListRepository.save(down);
                }
                
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
                            log.info("Remove Torrent: " + down.getFileName());

                            List<String> ids = new ArrayList<String>();
                            ids.add(tdown.getDbid());
                            downloadStationService.delete(ids);
                        }

                        Optional<Setting> delDirSetting = settingRepository.findByKey("DEL_DIR");

                        if (delDirSetting.isPresent()) {
                            if (Boolean.parseBoolean(delDirSetting.get().getValue())
                                    && !StringUtils.isEmpty(down.getName())) {

                                log.debug("removeDirectory");
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
                                    log.debug("getRename: " + down.getRename());
                                    if (jsonArray == null) {
                                        boolean renameStatus = 
                                            fileStationService.rename(path + File.separator + down.getName(),
                                                path + File.separator + down.getRename());
                                        setSeenList(down.getUri(), String.valueOf(renameStatus));
                                    } else {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject object = jsonArray.getJSONObject(i);
                                            if (object.has("name")) {
                                                String name = object.getString("name");
                                                if (StringUtils.contains(name, down.getName())) {
                                                    boolean renameStatus = 
                                                        fileStationService.rename(path + File.separator + name,
                                                            down.getRename() + "." + FilenameUtils.getExtension(name));
                                                    setSeenList(down.getUri(), String.valueOf(renameStatus));
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
            log.debug("moveDone: " + task.getTaskId());
            int resCode = fileStationService.moveTask(task.getTaskId());
            if (resCode != 0) {
                log.debug("finished");
                if (resCode == 1) {
                    fileStationService.delete(task.getDeletePath());
                }
                task.setTask(false);
                downloadListRepository.save(task);
            }
        }
    }

    private void setSeenList(String link, String renameStatus) {
        Optional<SeenList> optionalSeen = seenListRepository.findFirstByLink(link);
        if(optionalSeen.isPresent()) {
            SeenList seen = optionalSeen.get();
            seen.setRenameStatus(renameStatus);
            seenListRepository.save(seen);
        }
        
    }

    private void sendTelegram(DownloadList down) {
        Optional<DownloadList> rdown = downloadListRepository.findById(down.getId());
        log.debug(rdown.toString());
        if (rdown.isPresent()) {
            if (rdown.get().getIsSentAlert() == false) {
                String target = StringUtils.isEmpty(down.getFileName()) ? rdown.get().getName() : down.getFileName();
                log.info("Send Telegram: " + target);
                boolean ret = telegramService.sendMessage("<b>" + target + "</b>의 다운로드가 완료되었습니다.");
                log.debug("send telegram result: " + ret);
                if (ret) {
                    DownloadList sdown = rdown.get();
                    sdown.setIsSentAlert(true);
                    log.debug(sdown.toString());

                    downloadListRepository.save(sdown);
                }
            }
        }
    }

}