package com.example.imageviewdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

// 탐지 알림 기능에서 같이 쓰는 서버 요청
public class AlertApi {
    static final String CHANNEL_ID = "detect";

    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("alert", Context.MODE_PRIVATE);
    }

    static String siteUrl(Context context) {
        return prefs(context).getString("site_url", "http://10.0.2.2:8000");
    }

    static String fcmToken(Context context) {
        return prefs(context).getString("fcm_token", "");
    }

    static String encode(String value) throws IOException {
        return URLEncoder.encode(value, "UTF-8");
    }

    // 서버에 요청을 보내고 응답(JSON 문자열)을 돌려줌
    static String request(Context context, String method, String path, String body) throws IOException {
        URL url = new URL(siteUrl(context) + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Token " + prefs(context).getString("token", ""));
        conn.setRequestMethod(method);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        if (body != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes("UTF-8"));
            os.close();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("응답 코드 " + responseCode);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }

    static Bitmap loadImage(String imageUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        InputStream is = conn.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();
        return bitmap;
    }

    // 이 기기를 서버에 등록 (이미 있으면 설정은 그대로)
    static void registerDevice(Context context, String fcmToken) throws IOException, JSONException {
        JSONObject json = new JSONObject();
        json.put("token", fcmToken);
        json.put("kind", "app");
        request(context, "POST", "/api_root/Device/", json.toString());
        prefs(context).edit().putString("fcm_token", fcmToken).apply();
    }

    static void createChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "탐지 알림", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("YOLO가 고른 물체를 감지하면 알려줍니다");
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
