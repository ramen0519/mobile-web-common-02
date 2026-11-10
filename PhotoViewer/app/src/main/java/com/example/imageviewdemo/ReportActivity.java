package com.example.imageviewdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 감시 리포트: 최근 7일 감지 통계를 그래프로 보여줌
public class ReportActivity extends AppCompatActivity {
    TextView textPeriod, textOffline, textToday, textTotal, textTop;
    BarChartView chartHourly, chartDaily, chartCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        textPeriod = findViewById(R.id.textPeriod);
        textOffline = findViewById(R.id.textOffline);
        textToday = findViewById(R.id.textToday);
        textTotal = findViewById(R.id.textTotal);
        textTop = findViewById(R.id.textTop);
        chartHourly = findViewById(R.id.chartHourly);
        chartDaily = findViewById(R.id.chartDaily);
        chartCategory = findViewById(R.id.chartCategory);

        new LoadReport().execute();
    }

    public void onClickReportNow(View v) {
        ReportWorker.runNow(this);
        Toast.makeText(getApplicationContext(), "잠시 뒤 리포트 알림이 옵니다", Toast.LENGTH_SHORT).show();
    }

    private class LoadReport extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String result = AlertApi.request(ReportActivity.this, "GET", "/api_root/Report/", null);
                // 인터넷이 끊겨도 볼 수 있게 마지막 리포트 저장
                String time = new SimpleDateFormat("MM/dd HH:mm", Locale.KOREA).format(new Date());
                AlertApi.prefs(ReportActivity.this).edit()
                        .putString("report_json", result)
                        .putString("report_time", time)
                        .apply();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                result = AlertApi.prefs(ReportActivity.this).getString("report_json", null);
                if (result == null) {
                    textPeriod.setText("리포트를 불러오지 못했습니다. 서버를 확인하세요.");
                    return;
                }
                String time = AlertApi.prefs(ReportActivity.this).getString("report_time", "");
                textOffline.setText("서버에 연결할 수 없어 마지막으로 받은 리포트(" + time + ")를 보여줍니다.");
                textOffline.setVisibility(View.VISIBLE);
            }
            try {
                showReport(new JSONObject(result));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showReport(JSONObject stats) throws JSONException {
        textPeriod.setText(stats.getString("start") + " ~ " + stats.getString("end") + " (최근 7일)");
        JSONObject today = stats.getJSONObject("today");
        textToday.setText(today.getString("text"));
        String peak = stats.isNull("peak_hour") ? "-" : stats.getInt("peak_hour") + "시";
        textTotal.setText("최근 7일 감지 " + stats.getInt("total") + "번 · 가장 많았던 시간 " + peak);

        // 시간대별 (0~23시, 이름은 3시간마다)
        JSONArray hourly = stats.getJSONArray("hourly");
        int[] hourValues = new int[24];
        String[] hourLabels = new String[24];
        for (int i = 0; i < 24; i++) {
            hourValues[i] = hourly.getInt(i);
            hourLabels[i] = i + "시";
        }
        chartHourly.setData(hourValues, hourLabels, 3);

        // 날짜별
        JSONArray daily = stats.getJSONArray("daily");
        int[] dayValues = new int[daily.length()];
        String[] dayLabels = new String[daily.length()];
        for (int i = 0; i < daily.length(); i++) {
            dayValues[i] = daily.getJSONObject(i).getInt("count");
            dayLabels[i] = daily.getJSONObject(i).getString("date");
        }
        chartDaily.setData(dayValues, dayLabels, 1);

        // 종류별
        JSONArray categories = stats.getJSONArray("categories");
        int[] catValues = new int[categories.length()];
        String[] catLabels = new String[categories.length()];
        for (int i = 0; i < categories.length(); i++) {
            catValues[i] = categories.getJSONObject(i).getInt("count");
            catLabels[i] = categories.getJSONObject(i).getString("ko");
        }
        chartCategory.setData(catValues, catLabels, 1);

        // 많이 감지된 물체
        JSONArray top = stats.getJSONArray("top_objects");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < top.length(); i++) {
            JSONObject item = top.getJSONObject(i);
            text.append(i + 1).append(". ").append(item.getString("ko"))
                    .append(" · ").append(item.getInt("count")).append("번\n");
        }
        textTop.setText(top.length() == 0 ? "아직 감지된 물체가 없습니다." : text.toString().trim());
    }
}
