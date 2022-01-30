package com.junling.registry.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.junling.registry.common.result.R;
import com.junling.registry.common.utils.FileUtil;
import com.junling.registry.server.dao.RegistryDao;
import com.junling.registry.server.dao.ServiceDao;
import com.junling.registry.server.entity.RegistryEntity;
import com.junling.registry.server.entity.ServiceEntity;
import com.junling.registry.server.service.RegistryService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class RegistryServiceImpl implements RegistryService, InitializingBean {

    private volatile LinkedBlockingDeque<ServiceEntity> registryQueue = new LinkedBlockingDeque<>();
    private volatile LinkedBlockingDeque<ServiceEntity> removeQueue = new LinkedBlockingDeque<>();
    private volatile LinkedBlockingDeque<RegistryEntity> messageQueue = new LinkedBlockingDeque<>();

    private volatile Map<String, List<DeferredResult<R>>> map = new ConcurrentHashMap<>();
    private final Long TIME_OUT = 30000L;
    private final Integer BEAT_TIME = 10; //time unit second


    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(5));
    private volatile Boolean executorStop = false;

    @Autowired
    private ServiceDao serviceDao;
    @Autowired
    private RegistryDao registryDao;

    @Value("${registry.file.dir}")
    private String registryFileDir;

    @Override
    public R register(String data) {

        if (!StringUtils.hasLength(data)) {
            return new R(R.FAIL_CODE, "registry data is empty");
        }
        List<ServiceEntity> serviceEntities = JSON.parseArray(data, ServiceEntity.class);
        registryQueue.addAll(serviceEntities);

        return R.SUCCESS();
    }

    @Override
    public R remove(String data) {

        if (!StringUtils.hasLength(data)) {
            return new R(R.FAIL_CODE, "removal data is empty");
        }
        List<ServiceEntity> serviceEntities = JSON.parseArray(data, ServiceEntity.class);
        removeQueue.addAll(serviceEntities);
        return R.SUCCESS();
    }

    @Override
    public R discover(String data) {

        if (!StringUtils.hasLength(data)) {
            return new R(R.FAIL_CODE, "discovery key set is empty");
        }

        List<String> registryKeys = JSON.parseArray(data, String.class);
        Map<String, TreeSet<String>> map = new HashMap<>();

        for (String registryKey: registryKeys) {
            String filePath = registryFileDir + "/" + registryKey;
            List<String> list = FileUtil.loadData(filePath);
            if (list != null && list.size() > 0) {
                map.put(registryKey, new TreeSet<>(list));
            }
        }

        return new R(JSON.toJSONString(map));
    }

    @Override
    public DeferredResult<R> monitor(String data) {
        List<String> registryKeys = JSON.parseArray(data, String.class);
        DeferredResult<R> deferredResult = new DeferredResult<>(TIME_OUT, new R(R.FAIL_CODE, "time out"));
        if (!StringUtils.hasLength(data)) {
            deferredResult.setResult(new R(R.FAIL_CODE, "monitor key set is empty"));
            return deferredResult;
        }

        for (String registryKey: registryKeys) {
            if (!map.containsKey(registryKey)) {
                map.put(registryKey, new ArrayList<>());
            }
            map.get(registryKey).add(deferredResult);
        }

        return deferredResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //register service
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(!executorStop) {

                    try {
                        ServiceEntity service = registryQueue.take();
                        if (service != null) {
                            int updateRes = serviceDao.update(service);

                            if (updateRes == 0) {
                                serviceDao.save(service);
                                refreshAndNotify(service.getName());
                            }


                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        //remove service
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(!executorStop) {

                    try {
                        ServiceEntity serviceEntity = removeQueue.take();
                        if (serviceEntity != null) {
                            serviceDao.delete(serviceEntity);
                            refreshAndNotify(serviceEntity.getName());
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //message handler
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(!executorStop) {

                    try {
                        RegistryEntity registryEntity = messageQueue.take();
                        if (registryEntity != null) {
                            refreshFile(registryEntity);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        //clean expired db and file data
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while(!executorStop) {
                    List<String> filePaths = new ArrayList<>();

                    serviceDao.cleanExpiredData(BEAT_TIME * 3); //remove expire service entities
                    List<RegistryEntity> registryEntities = registryDao.findAll(); //find all registry entities and iterate each one
                    for (RegistryEntity registryEntity: registryEntities) {
                        String name = registryEntity.getName();
                        List<ServiceEntity> serviceEntities = serviceDao.findAll(name);


                        List<String> serviceAddressList = new ArrayList<>();
                        serviceEntities.forEach(entity->{
                            serviceAddressList.add(entity.getAddress());
                        });
                        String updateData = JSON.toJSONString(serviceAddressList); //check the current active service entities, can be zero, which is empty list
                        String registryData = registryEntity.getData();

                        if (!updateData.equals(registryData)) {
                            registryDao.update(name, updateData);
                        }

                        String filePath = refreshFile(registryEntity);
                        filePaths.add(filePath);
                    }

                    cleanFileData(filePaths);

                    try {
                        TimeUnit.SECONDS.sleep(BEAT_TIME);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });



    }


    private void refreshAndNotify(String name) {
        List<ServiceEntity> serviceEntities = serviceDao.findAll(name);
        List<String> addressList = new ArrayList<>();
        if (serviceEntities != null && serviceEntities.size()>0) {
            for (ServiceEntity entity: serviceEntities) {
                addressList.add(entity.getAddress());
            }
        }
        String updateData = JSON.toJSONString(addressList);

        //update t_registry: data property can be null or empty.
        RegistryEntity registryEntity = registryDao.findByName(name);
        boolean sendMessage = false;
        if (registryEntity == null) {
            registryEntity = new RegistryEntity();
            registryEntity.setName(name);
            registryEntity.setData(updateData);
            registryDao.save(registryEntity);
            sendMessage = true;
        }else {
            if (!registryEntity.getData().equals(updateData)) {
                registryDao.update(name, updateData);
                sendMessage = true;
            }
        }

        if (sendMessage) {
            messageQueue.add(registryEntity);
        }
    }

    private String refreshFile(RegistryEntity registryEntity) {
        String filePath = registryFileDir + "/" + registryEntity.getName();
        FileUtil.write(filePath, registryEntity.getData());


        List<DeferredResult<R>> deferredResults = map.get(registryEntity.getName());
        map.remove(registryEntity.getName());
        if (deferredResults != null && deferredResults.size() > 0) {
            for (DeferredResult<R> result: deferredResults) {
                result.setResult(new R(R.SUCCESS_CODE, "monitor updated"));
            }
        }

        return new File(filePath).getPath();
    }

    private void cleanFileData(List<String> filePaths){
        String parentDir = registryFileDir;
        File parent = new File(parentDir);
        File[] files = parent.listFiles();
        if (files != null && files.length > 0) {
            for (File file: files) {
                if (filePaths.contains(file.getPath())) {
                    file.delete();
                }
            }
        }

    }

}
