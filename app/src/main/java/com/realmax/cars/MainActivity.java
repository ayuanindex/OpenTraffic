package com.realmax.cars;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.realmax.cars.tcputil.TCPConnected;

import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private TextView tv_link_status;
    private TextView tv_device;
    private TextView tv_device_id;
    private TextView tv_camera_number;
    private ImageView iv_image;
    private Button btn_camera_one;
    private Button btn_camera_two;
    private Button btn_setting;
    private boolean flag = false;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        tv_link_status = (TextView) findViewById(R.id.tv_link_status);
        tv_device = (TextView) findViewById(R.id.tv_device);
        tv_device_id = (TextView) findViewById(R.id.tv_device_id);
        tv_camera_number = (TextView) findViewById(R.id.tv_camera_number);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        btn_camera_one = (Button) findViewById(R.id.btn_camera_one);
        btn_camera_two = (Button) findViewById(R.id.btn_camera_two);
        btn_setting = (Button) findViewById(R.id.btn_setting);
    }

    private void initEvent() {
        btn_camera_one.setOnClickListener(this);
        btn_camera_two.setOnClickListener(this);
        btn_setting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera_one:
                TCPConnected.start_camera("小车", 1, 1);
                getData();
                break;
            case R.id.btn_camera_two:
                TCPConnected.start_camera("小车", 1, 2);
                break;
            case R.id.btn_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        // 显示状态
        tv_link_status.setText("通讯：未连接");
        tv_device.setText("设备：小车");
        tv_device_id.setText("ID：" + 1);
        tv_camera_number.setText("摄像头：" + 1);

        getData();
    }

    /**
     * 获取返回的数据
     */
    private void getData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        sleep(500);
                        // 接受服务端返回的数据
                        String s = TCPConnected.fetch_camera();
                        Log.i(TAG, "run: " + s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (flag) {
            super.onBackPressed();
        } else {
            flag = true;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(2000);
                        flag = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TCPConnected.getSocket() != null && TCPConnected.getSocket().isConnected()) {
            tv_link_status.setText("通讯：已连接");
        }
    }
}