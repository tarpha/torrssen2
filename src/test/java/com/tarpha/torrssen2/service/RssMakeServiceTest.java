package com.tarpha.torrssen2.service;

import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RssMakeServiceTest {

    @Test
    public void rssMake() {
        RssMakeService svc = new RssMakeService();

        try {
            svc.makeRss();
        } catch (Exception e) {
            log.error(e.toString());
            Assert.fail();
        }
        Assert.assertTrue(true);
    }
    
}