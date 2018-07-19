package com.cn.tempdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cn.tempdemo.pieview.PieChartView4;

public class FourActivity extends AppCompatActivity {
    private PieChartView4 mPieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four);
        initButton();
        initView();
    }

    private void initButton() {
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPieChartView.updateView(false);
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPieChartView.updateView(true);
            }
        });
    }

    private void initView() {
        float[] data = new float[]{10, 10, 10, 40};
        String[] name = new String[]{"兄弟", "姐妹", "情侣", "基友"};
        mPieChartView = (PieChartView4) findViewById(R.id.percentPieView);
        mPieChartView.setData(data, name);
    }
}
