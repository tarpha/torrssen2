package com.tarpha.torrssen2.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.tarpha.torrssen2.domain.DownloadList;
import com.tarpha.torrssen2.repository.DownloadListRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
public class HttpDownloadService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private Map<Long, HttpVo> jobs = new HashMap<>();

    @Data
    private class HttpVo {
        private Long id;
        private String name;
        private String filename;
        private String path;
        private int percentDone = 0;
        private Boolean done = false;
    }

    @Async
    public void createTransmission(DownloadList download) {     
        String link = download.getUri();
        String path = download.getDownloadPath();

        long currentId = download.getId();

        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder(link);
            HttpGet httpGet = new HttpGet(builder.build());
            CloseableHttpResponse response = httpClient.execute(httpGet);

            Header[] header = response.getHeaders("Content-Disposition");
            String content = header[0].getValue();
            for(String str: StringUtils.split(content, ";")) {
                if(StringUtils.containsIgnoreCase(str, "filename=")) {
                    logger.debug(str);
                    String[] attachment = StringUtils.split(str, "=");
                    File directory = new File(path);
    
                    if(!directory.isDirectory()) {
                        FileUtils.forceMkdir(directory);
                    }

                    String filename = StringUtils.remove(attachment[1], "\"");

                    HttpVo vo = new HttpVo();
                    vo.setId(currentId);
                    vo.setName(download.getName());
                    vo.setFilename(filename);
                    vo.setPath(download.getDownloadPath());

                    jobs.put(currentId, vo);

                    download.setFileName(filename);
                    download.setDbid("http_" + vo.getId());
                    downloadListRepository.save(download);

                    BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(path, filename)));

                    int inByte;
                    while((inByte = bis.read()) != -1) bos.write(inByte);
                    bis.close();
                    bos.close();

                    vo.setDone(true);
                    vo.setPercentDone(100);

                    jobs.put(currentId, vo);

                    downloadListRepository.save(download);

                    if(StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(attachment[1]), "torrent")) {
                        long ret = transmissionService.torrentAdd(path + File.separator + filename, path);
                        if(ret > 0L) {
                            download.setId(ret);
                            downloadListRepository.save(download);
                            simpMessagingTemplate.convertAndSend("/topic/feed/download", download);
                        }
                    }
                }      
            }
            response.close();
            httpClient.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public DownloadList getInfo(Long id) {
        if(jobs.containsKey(id)) {
            DownloadList download = new DownloadList();
            HttpVo vo = jobs.get(id);
            download.setId(vo.getId());
            download.setDbid("http_" + vo.getId());
            download.setPercentDone(vo.getPercentDone());
            download.setDone(vo.getDone());
            download.setDownloadPath(vo.getPath());
            download.setFileName(vo.getFilename());
            download.setName(vo.getName());

            if(vo.getDone()) {
                jobs.remove(vo.getId());
            }

            return download;
        }

        return null;
    }
}