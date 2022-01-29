package com.junling.registry.common.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Service {

    private Integer id;
    private String name;
    private String address;
    private Date updateTime;
}
