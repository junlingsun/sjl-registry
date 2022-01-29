package com.junling.registry.client;

import com.alibaba.fastjson.JSON;
import com.junling.registry.common.entity.Service;
import com.junling.registry.common.result.R;
import com.junling.registry.common.utils.HttpUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegistryClient {

    private volatile String host;
    private volatile Integer port;

    private Integer registerSleepTime = 10; //time unit is second
    private Integer discoverSleepTime = 1; //time unit is second
    private volatile Boolean executorStop = false;

    private static ThreadPoolExecutor registerThread = new ThreadPoolExecutor(5,10,60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(10));
    private static ThreadPoolExecutor discoverThread = new ThreadPoolExecutor(5,10,60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(10));;

    private volatile Set<Service> services = Collections.newSetFromMap(new ConcurrentHashMap<Service, Boolean>());
    private volatile Map<String, TreeSet<String>> discoveryMap = new ConcurrentHashMap<>();

    /**
     * initialize RegistryClient: (1) keep registering services every 3s. (2) keep testing discover services every 3s
     * @param host
     * @param port
     */
    public RegistryClient(String host, Integer port) {
        this.host = host;
        this.port = port;

        //register thread pool to constantly register services.
        registerThread.execute(new Runnable() {
            @Override
            public void run() {

                while (!executorStop) {
                    if (services != null && services.size() > 0) {
                        register(new ArrayList<>(services));
                    }

                    try {
                        TimeUnit.SECONDS.sleep(registerSleepTime);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        discoverThread.execute(new Runnable() {
            @Override
            public void run() {
                while(!executorStop) {

                    if (discoveryMap!=null && discoveryMap.size()>0) {
                        monitor(discoveryMap.keySet());
                        discover(discoveryMap.keySet());
                    }

                    try {
                        TimeUnit.SECONDS.sleep(discoverSleepTime);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    /**
     * register: method to register services
     * @param serviceList
     * @return
     */
    public boolean register(List<Service> serviceList) {
        if (serviceList == null || serviceList.size() == 0) return true;
        services.addAll(serviceList);
        String jsonData = JSON.toJSONString(serviceList);
        String url = host + ":" + port +"/registry/register";
        R ret = postRequest(url, jsonData, 5);
        return ret.getCode().equals(R.SUCCESS_CODE);
    }

    /**
     * discover: method to return all addresses for each requested service
     * @param keySet
     * @return
     */
    public Map<String, TreeSet<String>> discover(Set<String> keySet) {

        String jsonData = JSON.toJSONString(keySet);
        String url = host + ":" + port + "/registry/discover";
        R ret = postRequest(url, jsonData, 5);
        Map<String, TreeSet<String>> map = JSON.parseObject(ret.getData().toString(), Map.class);
        if (map == null || map.size() == 0) return map;

        //add the discovered services to map
        for (String key: map.keySet()) {
           discoveryMap.put(key, map.get(key));
        }
        return map;
    }

    /**
     * monitor: method to monitor the status of services.
     * @param keySet
     * @return
     */
    public boolean monitor(Set<String> keySet) {
        String jsonData = JSON.toJSONString(keySet);
        String url = host+":"+port+"/registry/monitor";
        R ret = postRequest(url, jsonData, 5);
        return ret.getCode().equals(R.SUCCESS_CODE);
    }

    /**
     * remove: method to remove services
     * @param serviceList
     * @return
     */
    public boolean remove(Set<Service> serviceList) {
        if (services == null || services.size()==0) return true;
        services.removeAll(serviceList);

        String jsonData = JSON.toJSONString(serviceList);
        String url = host+":" + port + "/registry/remove";
        R r = postRequest(url, jsonData, 5);
        return r.getCode().equals(R.SUCCESS_CODE);
    }

    /**
     * post request: method to send post request to server to register services.
     * @param url
     * @param jsonData
     * @return
     */
    private R postRequest(String url, String jsonData, Integer timeOut) {
        String ret = HttpUtil.post(url, jsonData, timeOut);
        return JSON.parseObject(ret, R.class);
    }

}
