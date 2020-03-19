package com.realmax.cameras;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.realmax.cameras.tcputil.TCPConnected;
import com.realmax.cameras.utils.SpUtil;

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

        // 判断tcp连接是否建立，如果建立则不为空
        if (TCPConnected.getSocket() == null) {
            status = "通讯：未连接";
        } else {
            status = "通讯：已连接";
        }

        // 将当前的连接状态回显到控件中
        tv_link_status.setText(status);
        // 获取上一次连接成功记录的ip，将其显示到输入框中（此处通过SharedPreferences实现对数据简单存储）
        host = SpUtil.getString("host", "127.0.0.1");
        et_ip.setText(host);
    }

    /**
     * 开启连接
     */
    private void submit() {
        String ip = et_ip.getText().toString().trim();
        // 判断ip是否为空
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "请输入IP", Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断与之前连接的ip是否相同，如果不相同则断开连
        if (!host.equals(ip) && TCPConnected.getSocket() != null) {
            // host不相同断开连接
            TCPConnected.stop();
            tv_link_status.setText("通讯：未连接");
        } else if (TCPConnected.getSocket() != null) {
            // 相同的IP显示"已连接"
            Toast.makeText(SettingActivity.this, "已连接", Toast.LENGTH_SHORT).show();
            return;
        }

        // 开启连接
        TCPConnected.start(ip, 8527, new TCPConnected.ResultData() {
            @Override
            public void isConnected(boolean isConnected) {
                String msg = "";

                if (isConnected) {
                    msg = "连接成功";
                    // 将数据存入sp中
                    SpUtil.putString("host", ip);
                    // 记录但前连接的地址
                    host = ip;
                } else {
                    msg = "连接失败";
                }

                String finalMsg = msg;
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        // 连接状态
                        tv_link_status.setText("通讯：" + (finalMsg.equals("连接成功") ? "已连接" : "未连接"));
                        // 弹出Toast提示用户是否连接成功
                        Toast.makeText(SettingActivity.this, finalMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
