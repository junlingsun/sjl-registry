package com.junling.registry.common.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

    public static String post(String url, String jsonData, Integer timeout) {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try{
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 10000);
            connection.setConnectTimeout(3 * 10000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
            connection.connect();

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(jsonData);
            dataOutputStream.flush();
            dataOutputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("http request StatusCode("+ responseCode +") invalid. for url : " + url);
            }

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;

    }
}
