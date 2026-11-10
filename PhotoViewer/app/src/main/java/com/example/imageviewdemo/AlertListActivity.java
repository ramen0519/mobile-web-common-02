package com.example.imageviewdemo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 이 기기가 받은 탐지 알림 기록
public class AlertListActivity extends AppCompatActivity {
    TextView textCount;
    RecyclerView recyclerAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        textCount = findViewById(R.id.textCount);
        recyclerAlert = findViewById(R.id.recyclerAlert);

        if (AlertApi.fcmToken(this).isEmpty()) {
            Toast.makeText(getApplicationContext(), "알림 등록이 아직 안 됐습니다. 잠시 후 다시 열어 주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        new LoadAlerts().execute();
    }

    private class LoadAlerts extends AsyncTask<Void, Void, Boolean> {
        List<JSONObject> alerts = new ArrayList<>();
        List<Bitmap> images = new ArrayList<>();
        int unread = 0;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String query = "?token=" + AlertApi.encode(AlertApi.fcmToken(AlertListActivity.this));
                JSONArray aryJson = new JSONArray(AlertApi.request(AlertListActivity.this, "GET", "/api_root/Alert/" + query, null));
                for (int i = 0; i < aryJson.length() && i < 50; i++) {
                    JSONObject alert = aryJson.getJSONObject(i);
                    if (!alert.getBoolean("is_read")) {
                        unread++;
                    }
                    Bitmap image = null;
                    try {
                        image = AlertApi.loadImage(alert.getString("image"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    alerts.add(alert);
                    images.add(image);
                }
                // 목록을 열었으니 모두 읽음으로
                AlertApi.request(AlertListActivity.this, "POST", "/api_root/Alert/read_all/" + query, "{}");
                return true;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (!ok) {
                textCount.setText("알림 기록을 불러오지 못했습니다. 서버를 확인하세요.");
                return;
            }
            if (alerts.isEmpty()) {
                textCount.setText("아직 받은 알림이 없습니다.");
                return;
            }
            textCount.setText("받은 알림 " + alerts.size() + "개 · 새 알림 " + unread + "개");
            recyclerAlert.setLayoutManager(new LinearLayoutManager(AlertListActivity.this));
            recyclerAlert.setAdapter(new AlertAdapter(alerts, images));
        }
    }
}
