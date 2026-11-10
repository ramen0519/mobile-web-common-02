package com.example.imageviewdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 알림 받을 종류(사람, 동물, 탈것, 그 외 물건)와 알림 간격(분) 설정
public class AlertSettingActivity extends AppCompatActivity {
    SwitchCompat switchEnabled;
    EditText editMinutes;
    ListView listCategories;
    List<String> names = new ArrayList<>();  // 서버에 저장하는 종류 이름 (person, animal, vehicle, object)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_setting);
        switchEnabled = findViewById(R.id.switchEnabled);
        editMinutes = findViewById(R.id.editMinutes);
        listCategories = findViewById(R.id.listCategories);

        if (AlertApi.fcmToken(this).isEmpty()) {
            Toast.makeText(getApplicationContext(), "알림 등록이 아직 안 됐습니다. 잠시 후 다시 열어 주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        new LoadSetting().execute();
    }

    public void onClickSave(View v) {
        String minutes = editMinutes.getText().toString();
        if (minutes.isEmpty()) {
            Toast.makeText(getApplicationContext(), "몇 분에 한 번 알릴지 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> checked = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            if (listCategories.isItemChecked(i)) {
                checked.add(names.get(i));
            }
        }
        new SaveSetting(String.join(",", checked), Integer.parseInt(minutes), switchEnabled.isChecked()).execute();
    }

    // 종류 목록과 이 기기의 설정 불러오기
    private class LoadSetting extends AsyncTask<Void, Void, Boolean> {
        List<String> items = new ArrayList<>();
        List<String> chosen = new ArrayList<>();
        int minutes;
        boolean enabled;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONArray categories = new JSONArray(AlertApi.request(AlertSettingActivity.this, "GET", "/api_root/Device/categories/", null));
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    names.add(category.getString("name"));
                    if (category.getString("name").equals("person")) {
                        items.add(category.getString("ko"));
                    } else {
                        items.add(category.getString("ko") + " (" + category.getString("desc") + ")");
                    }
                }

                String path = "/api_root/Device/" + AlertApi.encode(AlertApi.fcmToken(AlertSettingActivity.this)) + "/";
                JSONObject device = new JSONObject(AlertApi.request(AlertSettingActivity.this, "GET", path, null));
                chosen = Arrays.asList(device.getString("categories").split(","));
                minutes = device.getInt("minutes");
                enabled = device.getBoolean("enabled");
                return true;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (!ok) {
                Toast.makeText(getApplicationContext(), "설정을 불러오지 못했습니다. 서버를 확인하세요.", Toast.LENGTH_LONG).show();
                return;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(AlertSettingActivity.this,
                    android.R.layout.simple_list_item_multiple_choice, items);
            listCategories.setAdapter(adapter);
            for (int i = 0; i < names.size(); i++) {
                listCategories.setItemChecked(i, chosen.contains(names.get(i)));
            }
            editMinutes.setText(String.valueOf(minutes));
            switchEnabled.setChecked(enabled);
        }
    }

    private class SaveSetting extends AsyncTask<Void, Void, Boolean> {
        String categories;
        int minutes;
        boolean enabled;

        SaveSetting(String categories, int minutes, boolean enabled) {
            this.categories = categories;
            this.minutes = minutes;
            this.enabled = enabled;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONObject json = new JSONObject();
                json.put("token", AlertApi.fcmToken(AlertSettingActivity.this));
                json.put("kind", "app");
                json.put("categories", categories);
                json.put("minutes", minutes);
                json.put("enabled", enabled);
                AlertApi.request(AlertSettingActivity.this, "POST", "/api_root/Device/", json.toString());
                return true;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(getApplicationContext(), "저장했습니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "저장하지 못했습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
