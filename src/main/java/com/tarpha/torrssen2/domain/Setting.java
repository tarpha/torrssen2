package com.tarpha.torrssen2.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Data
public class Setting {

    @Id
    private String key;

    private String value;

    private String type;

    private String label;

    private String groupLabel;

    private Boolean required;

    private Byte orderId;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDt = new Date();

}