package com.tarpha.torrssen2.domain;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class WatchList {

    @Id
    private String title;

    private Boolean useRegex = false;

    private String startSeason = "01";

    private String startEpisode = "01";

    private String endSeason = "999";

    private String endEpisode = "999";

    private String quality = "720p+";

    private String releaseGroup;

    private String downloadPath;

    private String rename;
    
    private Boolean use = true;

    private Boolean subtitle = false;

    private Boolean series = true;

    private ArrayList<String> rssList = new ArrayList<>();
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();

}