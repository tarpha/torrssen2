package com.tarpha.torrssen2.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class DownloadPath {

    @Id
    private String name;

    private String path;

    private Boolean useTitle = false;
    
    private Boolean useSeason = false;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();

}