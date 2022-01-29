package com.junling.registry.server.dao;


import com.junling.registry.server.entity.ServiceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ServiceDao {
    void save(@Param("serviceEntity") ServiceEntity serviceEntity);
    int update(@Param("serviceEntity") ServiceEntity serviceEntity);

    List<ServiceEntity> findAll(@Param("name") String name);

    void delete(@Param("serviceEntity") ServiceEntity serviceEntity);

    void cleanExpiredData(@Param("expirationTime") int expirationTime);
}
