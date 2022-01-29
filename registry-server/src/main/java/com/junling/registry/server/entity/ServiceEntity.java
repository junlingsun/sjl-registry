package com.junling.registry.server.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ServiceEntity {
    private Integer id;
    private String name;
    private String address;
    private Date updateTime;
}
