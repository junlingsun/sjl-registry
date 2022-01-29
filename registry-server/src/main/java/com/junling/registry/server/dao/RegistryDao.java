package com.junling.registry.server.dao;


import com.junling.registry.server.entity.RegistryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistryDao {
    RegistryEntity findByName(@Param("name") String name);

    void save(@Param("registryEntity") RegistryEntity registryEntity);

    void update(@Param("name") String name, @Param("data") String updateData);

    List<RegistryEntity> findAll();

    void deletebyName(@Param("name") String name);
}
