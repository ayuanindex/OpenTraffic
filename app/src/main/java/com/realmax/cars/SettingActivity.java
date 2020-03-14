package com.realmax.cars;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars
 * @ClassName: SettingActivity
 * @CreateDate: 2020/3/14 11:06
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_ip;
    private TextView tv_port;
    private TextView tv_link_status;
    private Button btn_link;
    private Button btn_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        et_ip = (EditText) findViewById(R.id.et_ip);
        tv_port = (TextView) findViewById(R.id.tv_port);
        tv_link_status = (TextView) findViewById(R.id.tv_link_status);
        btn_link = (Button) findViewById(R.id.btn_link);
        btn_back = (Button) findViewById(R.id.btn_back);
    }

    private void initEvent() {
        btn_link.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_link:

                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    private void initData() {

    }

    private void submit() {
        String ip = et_ip.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "请输入IP", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO validate success, do something
    }
}
