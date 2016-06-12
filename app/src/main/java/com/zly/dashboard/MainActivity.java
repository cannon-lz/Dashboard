package com.zly.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.zly.dashboard.lib.Dashboard;

/**
 * Created by zhangly on 16/6/12.
 */
public class MainActivity extends AppCompatActivity {

    private Dashboard mDashboard;
    private EditText mScoreEt;
    private EditText mTextEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDashboard = (Dashboard) findViewById(R.id.id_dashboard);
        mScoreEt = (EditText) findViewById(R.id.id_et_score);
        mTextEt = (EditText) findViewById(R.id.id_et_text);
    }

    public void setScore(View view) {
        String score = mScoreEt.getText().toString();
        if (!TextUtils.isEmpty(score)) {
            mDashboard.setScore(Float.valueOf(score));
        }
    }

    public void setMarkColors(View view) {
        mDashboard.setMarkColors(new int[]{Color.DKGRAY, Color.BLACK, Color.BLUE});
    }

    public void reset(View view) {
        mDashboard.reset();
        mDashboard.setMarkColors(mDashboard.getDefMarkColors());
    }

    public void setText(View view) {
        String text = mTextEt.getText().toString();
        if(!TextUtils.isEmpty(text)) {
            mDashboard.setCenterText(text);
        }
    }
}
