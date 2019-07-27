package com.tarpha.torrssen2.vo;

import java.util.List;

import com.tarpha.torrssen2.domain.WatchList;

import lombok.Data;

@Data
public class WatchListVO {
    private WatchList watchList;
    private List<TextValueVO> selectList;
}