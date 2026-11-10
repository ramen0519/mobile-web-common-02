package com.example.imageviewdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;

// 앱이 꺼져 있어도 탐지 알림을 받아서 알림창에 띄움
public class AlertService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        // 토큰이 바뀌면 서버에 다시 등록
        try {
            AlertApi.registerDevice(this, token);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");
        String imageUrl = AlertApi.siteUrl(this) + data.get("image");
        int id = Integer.parseInt(data.get("post_id"));

        // 감지 사진 내려받기
        Bitmap image = null;
        try {
            image = AlertApi.loadImage(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 알림을 누르면 감지 사진 화면으로
        Intent intent = new Intent(this, AlertDetailActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.putExtra("image", imageUrl);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 잠금 화면에서는 사진 없이 보이는 알림
        Notification publicVersion = new NotificationCompat.Builder(this, AlertApi.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("탐지 알림")
                .setContentText("새 감지가 있습니다. 잠금을 풀고 확인하세요.")
                .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AlertApi.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(publicVersion)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        if (image != null) {
            builder.setLargeIcon(image)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(image)
                            .bigLargeIcon((Bitmap) null));
        }

        AlertApi.createChannel(this);
        getSystemService(NotificationManager.class).notify(id, builder.build());
    }
}
