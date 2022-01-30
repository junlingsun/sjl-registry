package com.junling.registry.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Service {
    private String name;
    private String address;
}
