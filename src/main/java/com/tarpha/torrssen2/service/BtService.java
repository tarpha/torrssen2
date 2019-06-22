package com.tarpha.torrssen2.service;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.runtime.BtClient;
import bt.runtime.BtRuntime;
import bt.runtime.Config;
import lombok.Data;

@Service
public class BtService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DownloadListRepository downloadListRepository;

    @Autowired
    SettingRepository settingRepository;

    @Autowired
    TelegramService telegramService;

    private static Map<Long, BtVo> jobs = new HashMap<>();
    private static long id = 1;

    private BtRuntime sharedRuntime;

    @Data
    private class BtVo {
        private int percentDone;
        private String path;
        private String link;
        private String filename;
        private CompletableFuture<?> future;
    }

    // public BtService() {
    //     // enable multithreaded verification of torrent data
    //     Config config = new Config() {
    //         @Override
    //         public int getNumOfHashingThreads() {
    //             logger.debug("availableProcessors: " + Runtime.getRuntime().availableProcessors());
    //             int size = Runtime.getRuntime().availableProcessors();
    //             if (size == 0 ) {
    //                 size = 2;
    //             }
    //             return size * 2;
    //         }
    //     };

    //     // enable bootstrapping from public routers
    //     DHTModule dhtModule = new DHTModule(new DHTConfig() {
    //         @Override
    //         public boolean shouldUseRouterBootstrap() {
    //             return true;
    //         }
    //     });

    //     // sharedRuntime = BtRuntime.defaultRuntime();
        
    // }

    @PostConstruct
    private void setId() {
        Optional<DownloadList> optionalSeq = downloadListRepository.findTopByOrderByIdDesc();
        if(optionalSeq.isPresent()) {
            id = optionalSeq.get().getId() + 1L;  
            logger.debug("id: " + id);
        }
    }

    private DownloadList setInfo(BtVo vo) {
        DownloadList download = new DownloadList();
        if(vo != null) {
            download.setId(id);
            download.setPercentDone(vo.getPercentDone());
            download.setDone(vo.getFuture().isDone());
            download.setUri(vo.getLink());
            download.setName(vo.getFilename());
            download.setDownloadPath(vo.getPath());
        }

        return download;
    }

    public Long create(String link, String path, String filename) {
        // get download directory
        Path targetDirectory = new File(path).toPath();

        // create file system based backend for torrent data
        Storage storage = new FileSystemStorage(targetDirectory);

        long currentId = id++;

        try {
            BtClient client;
            Config config = new Config() {
                @Override
                public int getNumOfHashingThreads() {
                    logger.debug("availableProcessors: " + Runtime.getRuntime().availableProcessors());
                    int size = Runtime.getRuntime().availableProcessors();
                    if (size == 0 ) {
                        size = 2;
                    }
                    return size * 2;
                }
            };
    
            // enable bootstrapping from public routers
            DHTModule dhtModule = new DHTModule(new DHTConfig() {
                @Override
                public boolean shouldUseRouterBootstrap() {
                    return true;
                }
            });
            sharedRuntime = BtRuntime.builder(config).module(dhtModule).build();
            if(StringUtils.startsWith(link, "magnet")) {
                client = Bt.client(sharedRuntime).storage(storage).magnet(link).build();
            } else {
                client = Bt.client(sharedRuntime).storage(storage).torrent(new URL(link)).build();
            }
            CompletableFuture<?> future = client.startAsync(state -> {
                if(jobs.containsKey(currentId)) {
                    BtVo vo = jobs.get(currentId);
                    logger.debug("getDownloaded: " + state.getDownloaded());
                    logger.debug("getPiecesTotal: " + state.getPiecesTotal());
                    logger.debug("getPiecesComplete: " + state.getPiecesComplete());
                    logger.debug("percentDone: " + ((float)state.getPiecesComplete() / (float)state.getPiecesTotal()) * 100);
                    vo.setPercentDone((int)(((float)state.getPiecesComplete() / (float)state.getPiecesTotal()) * 100));
                    jobs.put(currentId, vo);
                }
                logger.debug(jobs.get(currentId).toString());
                if(state.getPiecesRemaining() == 0) {
                    Optional<DownloadList> optionalDownload = downloadListRepository.findFirstByUriAndDoneOrderByCreateDtDesc(link, false);
                    if(optionalDownload.isPresent()) {
                        DownloadList download = optionalDownload.get();
                        download.setPercentDone(100);
                        download.setDone(true);
                        downloadListRepository.save(download);

                        Optional<Setting> optionalSetting = settingRepository.findByKey("SEND_TELEGRAM");
                        if (optionalSetting.isPresent()) {
                            if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                                if(download.getIsSentAlert() == false) {
                                    String target = StringUtils.isEmpty(download.getFileName()) ? download.getName() : download.getFileName();
                                    logger.info("Send Telegram: " + target);
                                    if(telegramService.sendMessage("<b>" + target + "</b>의 다운로드가 완료되었습니다.")) {
                                        download.setIsSentAlert(true);
                    
                                        downloadListRepository.save(download);
                                    }
                                }
                            }
                        }
                    }
                    client.stop();
                }
            }, 1000);

            BtVo bt = new BtVo();
            bt.setPercentDone(0);
            bt.setFuture(future);
            bt.setPath(path);
            bt.setLink(link);
            bt.setFilename(filename);
            jobs.put(currentId, bt); 

        } catch (Exception e) {
            logger.error(e.getMessage());
            return -1L;
        }

        return currentId;
    }

    public boolean remove(long id) {
        jobs.get(id).getFuture().cancel(true);
        return true;
    }

    public DownloadList getInfo(long id) {
        if(jobs.containsKey(id)) {
            return setInfo(jobs.get(id));
        }
        return null;
    }

    public List<DownloadList> list() {
        List<DownloadList> ret = new ArrayList<>();

        for( Long id : jobs.keySet() ) {
            if(!jobs.get(id).getFuture().isDone()) {
                ret.add(setInfo(jobs.get(id)));
            }
        }

        logger.debug(ret.toString());

        return ret;
    }

}