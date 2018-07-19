package com.cn.tempdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cn.tempdemo.pieview.PieChartView3;

public class ThreeActivity extends AppCompatActivity {
    private PieChartView3 mPieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three);
        initView();
    }

    private void initView() {
        float[] data = new float[]{40.2f, 20.2f, 30.2f,40.2f, 20.2f, 30.2f, 60.4f};
        String[] name = new String[]{"兄弟", "姐妹", "情侣","兄弟", "姐妹", "情侣", "基友"};
        mPieChartView = (PieChartView3) findViewById(R.id.percentPieView);
        mPieChartView.setData(data, name);
        mPieChartView.startAnimation(2000);
    }
}
