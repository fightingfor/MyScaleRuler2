package com.example.jinguang.myscaleruler2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinguang.myscaleruler2.utils.DrawUtil;
import com.example.jinguang.myscaleruler2.view.DecimalScaleRulerView;

public class MainActivity extends AppCompatActivity {


    private float mHeight = 170;
    private float mMaxHeight = 220;
    private float mMinHeight = 100;


    private float mWeight = 0f;//默认起始点
    private float mMaxWeight = 200;
    private float mMinWeight = 25;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        DecimalScaleRulerView mWeightRulerView = findViewById(R.id.ruler_weight);
        final TextView mWeightValueTwo = findViewById(R.id.tv_user_weight_value_two);
        mWeightRulerView.initViewParam(mWeight, 0, 60 * 24, 60 * 20, 10);
        mWeightRulerView.setValueChangeListener(new DecimalScaleRulerView.OnValueChangeListener() {
            @Override
            public void onValueChange(String value) {
                mWeightValueTwo.setText(value);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
