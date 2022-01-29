package com.junling.registry.common.utils;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class FileUtil {

    public static List<String> loadData(String filePath){
        InputStream inputStream = null;

        File file = new File(filePath);
        if (!file.exists()) return null;
        try {
            inputStream = new FileInputStream(file);
            if (inputStream == null) return null;
            Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, "utf-8"));

            String data = properties.get("data").toString();
            return JSON.parseArray(data, String.class);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    public static void write(String filePath, String data) {

        Properties properties = new Properties();
        properties.setProperty("data", data);


        FileOutputStream fileOutputStream = null;

        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdir();
        }
        try {
            fileOutputStream = new FileOutputStream(file, false);
            properties.store(new OutputStreamWriter(fileOutputStream, "utf-8"),null);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }
}
