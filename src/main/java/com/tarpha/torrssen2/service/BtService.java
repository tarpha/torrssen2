package com.tarpha.torrssen2.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import javax.annotation.PostConstruct;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.domain.Setting;
import com.tarpha.torrssen2.repository.DownloadListRepository;
import com.tarpha.torrssen2.repository.SettingRepository;
import com.tarpha.torrssen2.util.CommonUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import bt.runtime.BtClient;
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
    private static Queue<BtVo> queue = new LinkedList<>();
    private static long id = 1;

    private int concurrentSize = Runtime.getRuntime().availableProcessors() * 2;

    @Data
    private class BtVo {
        private Long id;
        private int percentDone;
        private String path;
        private String link;
        private String filename;
        private String innerFile;
        private List<String> innerList;
        private Boolean error = false;
        private Boolean done = false;
        private Boolean cancel = false;
        // private CompletableFuture<?> future;
    }

    @PostConstruct
    private void setId() {
        Optional<DownloadList> optionalSeq = downloadListRepository.findTopByOrderByIdDesc();
        if (optionalSeq.isPresent()) {
            id = optionalSeq.get().getId() + 1L;
            logger.debug("id: " + id);
        }
        setConcurrentSize();
    }

    private void setConcurrentSize() {
        Optional<Setting> optionalSetting = settingRepository.findByKey("EMBEDDED_LIMIT");
        if (optionalSetting.isPresent()) {
            concurrentSize = Integer.parseInt(optionalSetting.get().getValue());
        }
    }

    private DownloadList setInfo(BtVo vo, Long id) {
        DownloadList download = new DownloadList();
        if (vo != null) {
            download.setId(id);
            download.setPercentDone(vo.getPercentDone());
            download.setUri(vo.getLink());
            download.setName(vo.getFilename());
            download.setDownloadPath(vo.getPath());
            download.setStatus(vo.getError() ? -1 : 3);

            // if(vo.getFuture() != null) {
            //     download.setDone(vo.getFuture().isDone());
            // }
        }

        return download;
    }

    private String getInnerFile(List<TorrentFile> list, String name) {
        for (int i = 0; i < list.size(); i++) {
            TorrentFile torrent = list.get(i);
            String pathElement = StringUtils.join(torrent.getPathElements(), File.pathSeparator);
            logger.debug("pathElement : " + pathElement);
            if (StringUtils.contains(pathElement, name)) {
                return pathElement;
            }
        }
        return null;
    }

    private List<String> getInnerFileList(List<TorrentFile> list) {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            TorrentFile torrent = list.get(i);
            String pathElement = StringUtils.join(torrent.getPathElements(), File.separator);
            logger.debug("pathElement : " + pathElement);
            ret.add(pathElement);
        }
        return ret;
    }

    public long create(String link, String path, String filename) {
        long currentId = id++;

        BtVo vo = new BtVo();
        vo.setId(currentId);
        vo.setLink(link);
        vo.setPath(path);
        vo.setFilename(filename);

        queue.offer(vo);

        check();

        return currentId;
    }

    public void check() {
        setConcurrentSize();

        logger.debug("jobs size: " + jobs.size());
        logger.debug("concurrentSize: " + concurrentSize);

        for(Long key: jobs.keySet()) {
            if(jobs.get(key).getDone()) {
                jobs.remove(key);
                Optional<DownloadList> optionalDownload = downloadListRepository.findById(key);
                if (optionalDownload.isPresent()) {
                    DownloadList download = optionalDownload.get();
                    download.setPercentDone(100);
                    download.setDone(true);
                    downloadListRepository.save(download);
                }
            }
        }

        if (jobs.size() < concurrentSize) {
            if (queue.size() > 0) {
                BtVo vo = queue.poll();
                if (vo != null) {
                    execute(vo.getId(), vo.getLink(), vo.getPath(), vo.getFilename());
                }
            }
        }
    }

    private void setInfo(Long currentId, Torrent torrent) {
        BtVo vo = jobs.get(currentId);
        vo.setFilename(torrent.getName());
        vo.setInnerFile(getInnerFile(torrent.getFiles(), torrent.getName()));
        vo.setInnerList(getInnerFileList(torrent.getFiles()));
    }

    @Async
    private void httpDownload(Long currentId, String link, String path) {
        BtVo bt = new BtVo();
        bt.setId(currentId);
                
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder(link);
            HttpGet httpGet = new HttpGet(builder.build());
            CloseableHttpResponse response = httpClient.execute(httpGet);

            Header[] header = response.getHeaders("Content-Disposition");
            // logger.debug(header.length + "");
            // logger.debug(header[0].getValue());

            if(StringUtils.containsIgnoreCase(header[0].getValue(), "filename=")) {
                String[] attachment = StringUtils.split(header[0].getValue(), "=");
                // logger.debug(attachment[1]);

                File directory = new File(path);

                if(!directory.isDirectory()) {
                    FileUtils.forceMkdir(directory);
                }

                BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(path, attachment[1])));

                int inByte;
                while((inByte = bis.read()) != -1) bos.write(inByte);
                bis.close();
                bos.close();

                bt.setPercentDone(100);
                bt.setPath(path);
                bt.setLink(link);
                bt.setDone(true);
                bt.setFilename(attachment[1]);
            }

            response.close();
            httpClient.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            bt.setError(true);
        }

        jobs.put(currentId, bt);
    }

    public void execute(Long currentId, String link, String path, String filename) {
        // get download directory
        Path targetDirectory = new File(path).toPath();

        // create file system based backend for torrent data
        Storage storage = new FileSystemStorage(targetDirectory);

        // long currentId = id++;

        try {
            // BtClient client = null;
            DHTModule dhtModule = new DHTModule(new DHTConfig() {
                @Override
                public boolean shouldUseRouterBootstrap() {
                    return true;
                }
            });
            if (StringUtils.startsWith(link, "magnet")) {
                BtClient client = Bt.client().autoLoadModules().module(dhtModule).storage(storage).magnet(link)
                        .afterTorrentFetched(torrent -> {
                            logger.debug("getName: " + torrent.getName());
                            logger.debug("getFiles: " + torrent.getFiles().toString());
                            if (jobs.containsKey(currentId)) {
                                setInfo(currentId, torrent);
                            }
                        }).build();

                // CompletableFuture<?> future = 
                client.startAsync(state -> {
                    if (jobs.containsKey(currentId)) {
                        BtVo vo = jobs.get(currentId);

                        logger.debug("getDownloaded: " + state.getDownloaded());
                        logger.debug("getPiecesTotal: " + state.getPiecesTotal());
                        logger.debug("getPiecesComplete: " + state.getPiecesComplete());
                        logger.debug("percentDone: "
                                + ((float) state.getPiecesComplete() / (float) state.getPiecesTotal()) * 100);
                        vo.setPercentDone(
                                (int) (((float) state.getPiecesComplete() / (float) state.getPiecesTotal()) * 100));
                        jobs.put(currentId, vo);

                        if(vo.getCancel()) {
                            logger.debug("=== cancel ====");
                            jobs.remove(currentId);
                            client.stop();
                        }
                    }
                    logger.debug(jobs.get(currentId).toString());
                    if (state.getPiecesRemaining() == 0) {
                        Optional<DownloadList> optionalDownload = downloadListRepository.findById(currentId);
                        if (optionalDownload.isPresent()) {
                            DownloadList download = optionalDownload.get();
                            download.setPercentDone(100);
                            download.setDone(true);
                            downloadListRepository.save(download);
                            Optional<Setting> optionalSetting = settingRepository.findByKey("SEND_TELEGRAM");
                            if (optionalSetting.isPresent()) {
                                if (Boolean.parseBoolean(optionalSetting.get().getValue())) {
                                    if (download.getIsSentAlert() == false) {
                                        String target = StringUtils.isEmpty(download.getFileName()) ? download.getName()
                                                : download.getFileName();
                                        logger.info("Send Telegram: " + target);
                                        if (telegramService.sendMessage("<b>" + target + "</b>의 다운로드가 완료되었습니다.")) {
                                            download.setIsSentAlert(true);

                                            downloadListRepository.save(download);
                                        }
                                    }
                                }
                            }

                            if (jobs.containsKey(currentId)) {
                                BtVo vo = jobs.get(currentId);
                                logger.debug("removeDirectory");
                                if (CommonUtils.removeDirectory(vo.getPath(), vo.getFilename(), vo.getInnerList(), settingRepository)) {
                                    vo.setFilename(vo.getInnerFile());
                                }
                                if (!StringUtils.isBlank(download.getRename())) {
                                    logger.debug("getRename: " + download.getRename());
                                    CommonUtils.renameFile(vo.getPath(), vo.getFilename(), download.getRename());
                                }
                                jobs.remove(currentId);
                            }
                        }
                        // state = null;
                        client.stop();
                    }
                    // state = null;
                }, 1000);

                BtVo bt = new BtVo();
                bt.setId(currentId);
                bt.setPercentDone(0);
                // bt.setFuture(future);
                bt.setPath(path);
                bt.setLink(link);
                bt.setFilename(filename);
                jobs.put(currentId, bt);
            } else {
                httpDownload(currentId, link, path);
                // client = Bt.client().storage(storage).torrent(new URL(link))
                //         .afterTorrentFetched(torrent -> {
                //             logger.debug("getName: " + torrent.getName());
                //             if (jobs.containsKey(currentId)) {
                //                 setInfo(currentId, torrent);
                //             }
                //         }).build();
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            // return -1L;
            if (jobs.containsKey(currentId)) {
                BtVo vo = jobs.get(currentId);
                vo.setError(true);
            }
        }
    }

    public boolean remove(long id) {
        if (jobs.containsKey(id)) {
            BtVo vo = jobs.get(id);
            vo.setCancel(true);
            // vo.getFuture().cancel(true);
            try {
                File file = new File(vo.getPath(), vo.getFilename());
                if (file.isFile() || file.isDirectory()) {
                    FileUtils.forceDelete(file);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return false;
            }
            // jobs.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public DownloadList getInfo(long id) {

        if (jobs.containsKey(id)) {
            return setInfo(jobs.get(id), id);
        } else {
            Optional<DownloadList> optionalDownload = downloadListRepository.findById(id);
            if (optionalDownload.isPresent()) {
                if (optionalDownload.get().getDone()) {
                    return optionalDownload.get();
                }
            }
        }
        return null;
    }

    public List<DownloadList> list() {
        List<DownloadList> ret = new ArrayList<>();

        for (Long id : jobs.keySet()) {
            if (!jobs.get(id).getDone()) {
                ret.add(setInfo(jobs.get(id), id));
            }
        }

        logger.debug(ret.toString());

        return ret;
    }

}