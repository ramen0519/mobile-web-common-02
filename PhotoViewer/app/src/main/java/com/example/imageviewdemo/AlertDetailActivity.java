package com.example.imageviewdemo;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

// 감지 사진 크게 보기 (알림이나 알림 기록에서 열림)
public class AlertDetailActivity extends AppCompatActivity {
    ImageView imageAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_detail);
        TextView textTitle = findViewById(R.id.textTitle);
        TextView textBody = findViewById(R.id.textBody);
        imageAlert = findViewById(R.id.imageAlert);

        textTitle.setText(getIntent().getStringExtra("title"));
        textBody.setText(getIntent().getStringExtra("body"));
        new LoadImage().execute(getIntent().getStringExtra("image"));
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                return AlertApi.loadImage(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if (image == null) {
                Toast.makeText(getApplicationContext(), "사진을 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
            } else {
                imageAlert.setImageBitmap(image);
            }
        }
    }
}
