package com.tarpha.torrssen2.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class RssList {

    @Id
    private String name;

    private String url;

    private Boolean useDb = true;

    private Boolean show = true;

    private Boolean tvSeries = true;

    private String linkKey = "link";

    private Boolean downloadAll = false;

    private String downloadPath;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();

}