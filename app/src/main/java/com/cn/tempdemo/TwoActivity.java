package com.cn.tempdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cn.tempdemo.pieview.PieChartView2;

public class TwoActivity extends AppCompatActivity {
    private PieChartView2 mPieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
        initView();
    }

    private void initView() {
        float[] data = new float[]{40.2f, 20.2f, 30.2f,40.2f, 20.2f, 30.2f, 60.4f};
        String[] name = new String[]{"兄弟", "姐妹", "情侣","兄弟", "姐妹", "情侣", "基友"};
        mPieChartView = (PieChartView2) findViewById(R.id.percentPieView);
        mPieChartView.setData(data, name);
        mPieChartView.startAnimation(2000);
    }
}
