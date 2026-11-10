package com.example.imageviewdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

// 매일 밤 9시쯤 오늘의 감시 리포트를 받아 알림으로 띄움 (앱이 꺼져 있어도 동작)
public class ReportWorker extends Worker {
    static final String CHANNEL_ID = "report";
    static final String WORK_NAME = "daily_report";

    public ReportWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        try {
            String result = AlertApi.request(getApplicationContext(), "GET", "/api_root/Report/today/", null);
            JSONObject today = new JSONObject(result);
            show(getApplicationContext(), "오늘의 감시 리포트", today.getString("text"));
            return Result.success();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return Result.retry();  // 인터넷이 안 되면 잠시 뒤 다시
        }
    }

    // 매일 밤 9시 리포트 예약 (끄면 예약 취소)
    static void schedule(Context context, boolean enabled) {
        WorkManager workManager = WorkManager.getInstance(context);
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME);
            return;
        }
        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        next.set(Calendar.HOUR_OF_DAY, 21);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }
        long delay = next.getTimeInMillis() - now.getTimeInMillis();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ReportWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    // 지금 한 번 받아 보기 (리포트 화면의 미리 받기 버튼)
    static void runNow(Context context) {
        WorkManager.getInstance(context).enqueue(new OneTimeWorkRequest.Builder(ReportWorker.class).build());
    }

    static void show(Context context, String title, String body) {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "감시 리포트", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("하루 동안의 감지 요약을 알려줍니다");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        // 누르면 감시 리포트 화면
        Intent intent = new Intent(context, ReportActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        manager.notify(0, builder.build());
    }
}
