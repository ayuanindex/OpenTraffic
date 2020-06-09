package com.realmax.cameras;

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

import com.realmax.cameras.tcputil.CustomerHandlerBase;
import com.realmax.cameras.tcputil.NettyLinkUtil;
import com.realmax.cameras.tcputil.ValueUtil;
import com.realmax.cameras.utils.SpUtil;

import io.netty.channel.EventLoopGroup;

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
            default:
        }
    }

    private void initData() {
        String status = "";

        // 判断tcp连接是否建立，如果建立则不为空
        if (ValueUtil.isCameraConnected()) {
            status = "通讯：已连接";
        } else {
            status = "通讯：未连接";
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
        String ip = null;
        ip = et_ip.getText().toString().trim();
        // 判断ip是否为空
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "请输入IP", Toast.LENGTH_SHORT).show();
            return;
        }

        SpUtil.putString("host", ip);

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    CustomerHandlerBase nettyHandler = new CustomerHandlerBase();
                    ValueUtil.getCustomerHandlerBases().put("camera", nettyHandler);

                    NettyLinkUtil nettyLinkUtil = new NettyLinkUtil(host, 8527);
                    nettyLinkUtil.start(new NettyLinkUtil.Callback() {
                        @Override
                        public void success(EventLoopGroup eventLoopGroup) {
                            Log.d(TAG, "success: 连接成功");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_link_status.setText("通讯：已连接");
                                    ValueUtil.setCameraConnected(true);
                                }
                            });
                        }

                        @Override
                        public void error() {
                            Log.d(TAG, "error: 连接失败");
                            if (nettyHandler.getCustomerCallback() != null) {
                                nettyHandler.getCustomerCallback().disConnected();
                            }
                        }
                    }, nettyHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
