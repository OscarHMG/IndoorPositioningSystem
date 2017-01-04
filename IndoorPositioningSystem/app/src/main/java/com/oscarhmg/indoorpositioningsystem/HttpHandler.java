package com.oscarhmg.indoorpositioningsystem;

import android.os.StrictMode;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 14/12/2016.
 */
public class HttpHandler {

    public String request(String _urlString,String _Data){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StringBuffer chaine = new StringBuffer("");
        HttpURLConnection connection = null;

        try{
            URL url = new URL(_urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "");
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(_Data.getBytes());
            out.close();

            InputStream inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = rd.readLine())!= null){
                chaine.append(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            connection.disconnect();
        }
        return chaine.toString();
    }
}
