package com.tarpha.torrssen2.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class DownloadList {

    @Id
    private Long id;

    private String dbid;

    private String name;

    @Column(length = 1024)
    private String fileName;

    private String downloadPath;

    private String rename;

    @Column(length = 2048)
    private String uri;

    private String rssTitle;

    private String rssReleaseGroup;

    private Integer percentDone = 0;

    private Integer status = 3;

    private Integer vueItemIndex;

    private Boolean auto = false;

    private Boolean done = false;

    private Boolean cancel = false;

    private Boolean isFake = false;

    private Boolean isSentAlert = false;

    private Boolean task = false;

    private String taskId;

    private String deletePath;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();

}