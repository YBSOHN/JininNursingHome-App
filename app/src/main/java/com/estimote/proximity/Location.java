package com.estimote.proximity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Location {


    Location() {

    }

    public JSONObject getData(String id) {
        String j = download(MainActivity.HOST + "/select.php?id=" + id);
        try {
            return new JSONObject(j);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<LocationMeta> getNameList() {
        ArrayList<LocationMeta> data = new ArrayList<>();
        try {
            String j = download(MainActivity.HOST+ "/name.php");
            JSONArray arr = new JSONArray(j);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                data.add(new LocationMeta(item.getString("beacon_id"), item.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String download(String _url) {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        StringBuilder data = new StringBuilder();

        try {
            url = new URL(_url);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                data.append(line);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
        return data.toString();
    }
}
