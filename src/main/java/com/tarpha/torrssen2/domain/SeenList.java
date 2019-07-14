package com.tarpha.torrssen2.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class SeenList {

    @Id
    private String link;

    private String title;

    private String season;

    private String episode;

    private String fileName;

    private String downloadPath;
    
    private String rename;

    private Boolean subtitle = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();
    
}