package com.tarpha.torrssen2.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

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

@Service
public class HttpDownloadService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransmissionService transmissionService;

    @Autowired
    private DownloadListRepository downloadListRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Async
    public void createTransmission(DownloadList download) {     
        String link = download.getUri();
        String path = download.getDownloadPath();
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder(link);
            HttpGet httpGet = new HttpGet(builder.build());
            CloseableHttpResponse response = httpClient.execute(httpGet);

            Header[] header = response.getHeaders("Content-Disposition");

            if(StringUtils.containsIgnoreCase(header[0].getValue(), "filename=")) {
                String[] attachment = StringUtils.split(header[0].getValue(), "=");
                
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

                if(StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(attachment[1]), "torrent")) {
                    long ret = transmissionService.torrentAdd(path + File.separator + attachment[1], path);
                    if(ret > 0L) {
                        download.setId(ret);
                        downloadListRepository.save(download);
                        simpMessagingTemplate.convertAndSend("/topic/feed/download", download);
                    }
                }
            }

            response.close();
            httpClient.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}