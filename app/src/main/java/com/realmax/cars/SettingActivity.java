package com.realmax.cars;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.realmax.cars.tcputil.TCPConnected;
import com.realmax.cars.utils.SpUtil;

/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars
 * @ClassName: SettingActivity
 * @CreateDate: 2020/3/14 11:06
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private EditText et_ip;
    private TextView tv_port;
    private TextView tv_link_status;
    private Button btn_link;
    private Button btn_back;
    private String host;

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
                submit();
                break;
            case R.id.btn_back:
                finish();
                break;
        }
    }

    private void initData() {
        String status = "";
        if (TCPConnected.getSocket() == null) {
            status = "通讯：未连接";
        } else {
            status = "通讯：已连接";
        }
        tv_link_status.setText(status);
        host = SpUtil.getString("host", "127.0.0.1");
        et_ip.setText(host);
    }

    private void submit() {
        String ip = et_ip.getText().toString().trim();
        // 判断ip是否为空
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "请输入IP", Toast.LENGTH_SHORT).show();
            return;
        }
        // 判断与之前连接的ip是否相同，如果不相同则断开连
        if (!host.equals(ip) && TCPConnected.getSocket() != null) {
            TCPConnected.stop();
            tv_link_status.setText("通讯：未连接");
        } else if (TCPConnected.getSocket() != null) {
            Log.i(TAG, "onClick: 已连接");
            Toast.makeText(SettingActivity.this, "已连接", Toast.LENGTH_SHORT).show();
            return;
        }
        String host = et_ip.getText().toString().trim();
        TCPConnected.start(host, 8527, new TCPConnected.ResultData() {
            @Override
            public void isConnected(boolean isConnected) {
                String msg = "";
                if (isConnected) {
                    msg = "连接成功";
                    SpUtil.putString("host", host);
                    SettingActivity.this.host = host;
                    et_ip.setText(host);
                } else {
                    msg = "连接失败";
                }
                String finalMsg = msg;
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        tv_link_status.setText("通讯：" + (finalMsg.equals("连接成功") ? "已连接" : "未连接"));
                        Toast.makeText(SettingActivity.this, finalMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void getResultData(String data) {

            }
        });
    }
}
