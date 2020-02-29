package com.estimote.proximity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Article {


    Article() {

    }

    public String getName(String id) {
        String j = download("http://13.209.74.71/select.php?id=" + id);
        try {
            JSONObject json = new JSONObject(j);
            return json.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getSubject(String id) {
        String j = download("http://13.209.74.71/select.php?id=" + id);
        try {
            JSONObject json = new JSONObject(j);
            return json.getString("subject");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getInfo(String id) {
        String j = download("http://13.209.74.71/select.php?id=" + id);
        try {
            JSONObject json = new JSONObject(j);
            return json.getString("info");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getNotificationHead(String id) {
        String j = download("http://13.209.74.71/select.php?id=" + id);
        try {
            JSONObject json = new JSONObject(j);
            return json.getString("notification_head");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getNotificationBody(String id) {
        String j = download("http://13.209.74.71/select.php?id=" + id);
        try {
            JSONObject json = new JSONObject(j);
            return json.getString("notification_body");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
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
