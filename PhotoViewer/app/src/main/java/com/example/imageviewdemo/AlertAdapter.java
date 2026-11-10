package com.example.imageviewdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

// 알림 기록 목록 (사진, 물체, 시각)
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {
    private List<JSONObject> alertList;
    private List<Bitmap> imageList;

    public AlertAdapter(List<JSONObject> alertList, List<Bitmap> imageList) {
        this.alertList = alertList;
        this.imageList = imageList;
    }

    @Override
    public AlertViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlertViewHolder holder, int position) {
        JSONObject alert = alertList.get(position);
        String labels = alert.optString("labels_ko");
        String date = alert.optString("sent_date").replace("T", " ").substring(0, 19);  // 2026-11-10 15:20:31
        String image = alert.optString("image");

        holder.imageView.setImageBitmap(imageList.get(position));
        holder.textLabels.setText(labels);
        holder.textDate.setText(date);
        holder.textNew.setVisibility(alert.optBoolean("is_read") ? View.GONE : View.VISIBLE);

        // 누르면 감지 사진 크게 보기
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AlertDetailActivity.class);
            intent.putExtra("title", "[침입 감지] " + labels);
            intent.putExtra("body", date);
            intent.putExtra("image", image);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textLabels;
        TextView textDate;
        TextView textNew;

        public AlertViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageItem);
            textLabels = itemView.findViewById(R.id.textLabels);
            textDate = itemView.findViewById(R.id.textDate);
            textNew = itemView.findViewById(R.id.textNew);
        }
    }
}
