package com.example.imageviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

// 감시 리포트 막대그래프 (값과 이름을 받아 직접 그림)
public class BarChartView extends View {
    private int[] values = new int[0];
    private String[] labels = new String[0];
    private int labelStep = 1;  // 이름을 몇 칸마다 쓸지 (24시간은 3칸마다)
    private Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linePaint = new Paint();

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        barPaint.setColor(Color.parseColor("#FF9400"));
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics()));
        linePaint.setColor(Color.LTGRAY);
    }

    public void setData(int[] values, String[] labels, int labelStep) {
        this.values = values;
        this.labels = labels;
        this.labelStep = labelStep;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length == 0) {
            return;
        }
        int max = 1;
        for (int value : values) {
            max = Math.max(max, value);
        }

        float dp = getResources().getDisplayMetrics().density;
        float top = 18 * dp;  // 막대 위 숫자 자리
        float bottom = getHeight() - 18 * dp;  // 아래 이름 자리
        float slot = (float) getWidth() / values.length;
        float barWidth = slot * 0.6f;
        canvas.drawLine(0, bottom, getWidth(), bottom, linePaint);

        for (int i = 0; i < values.length; i++) {
            float centerX = slot * i + slot / 2;
            float barTop = bottom - (bottom - top) * values[i] / max;
            if (values[i] > 0) {
                canvas.drawRect(centerX - barWidth / 2, barTop, centerX + barWidth / 2, bottom, barPaint);
                canvas.drawText(String.valueOf(values[i]), centerX, barTop - 4 * dp, textPaint);
            }
            if (i % labelStep == 0) {
                canvas.drawText(labels[i], centerX, getHeight() - 4 * dp, textPaint);
            }
        }
    }
}
