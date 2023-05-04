// Copyright (c) 2023 Created By Dayona All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.


package id.agen.zoloz_sdk;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LocalRequest implements IRequest {
    public String request(String requestUrl, String requestData) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Charset", "UTF-8");
            httpConn.connect();
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
            dos.writeBytes(requestData);
            dos.flush();
            dos.close();
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode) {
                StringBuilder sb = new StringBuilder();
                String readLine;
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), StandardCharsets.UTF_8));
                while ((readLine = responseReader.readLine()) != null) {
                    sb.append(readLine).append("\n");
                }
                responseReader.close();
                return sb.toString();
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
}
