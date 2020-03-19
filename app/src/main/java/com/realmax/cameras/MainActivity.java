package com.realmax.cameras;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.realmax.cameras.bean.CameraBean;
import com.realmax.cameras.tcputil.TCPConnected;
import com.realmax.cameras.utils.EncodeAndDecode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView tv_link_status;
    private TextView tv_device;
    private TextView tv_device_id;
    private TextView tv_camera_number;
    private ImageView iv_image;
    private Button btn_setting;
    private boolean flag = false;
    private Spinner sp_select;
    private ImageView iv_add;
    private ArrayList<CameraBean> cameraBeans;
    private CustomerAdapter customerAdapter;

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
        btn_setting = (Button) findViewById(R.id.btn_setting);
        sp_select = (Spinner) findViewById(R.id.sp_select);
        iv_add = (ImageView) findViewById(R.id.iv_add);
    }

    private void initEvent() {
        sp_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 下拉框的监听，通过点击拿到对应的设备来进行连接
                CameraBean cameraBean = cameraBeans.get(position);
                // 将选择的设备显示到屏幕中对应的控件上
                tv_device.setText("设备：" + cameraBean.getName());
                tv_device_id.setText("ID：" + cameraBean.getId());
                tv_camera_number.setText("摄像头：" + cameraBean.getCameraId());
                // 开始使用对应的设备进行拍照
                TCPConnected.start_camera(cameraBean.getName(), cameraBean.getId(), cameraBean.getCameraId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 点击按钮弹出对话框添加设备
        iv_add.setOnClickListener(new View.OnClickListener() {
            private Button btnOk;
            private Button btnCancal;
            private EditText etCamera;
            private EditText etId;
            private EditText etName;

            @Override
            public void onClick(View v) {
                // 初始化对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.create();
                // 自定义对话框布局
                View view = View.inflate(MainActivity.this, R.layout.dialog_add_camera, null);
                // 设置对话框布局
                alertDialog.setView(view);
                // 初始化空间
                initView(view);
                // 设置对话框中控件的点击事件
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 对各个输入框进行判空处理
                        String name = etName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(MainActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String id = etId.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(MainActivity.this, "请输入ID", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String camera_id = etCamera.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(MainActivity.this, "请输入摄像头编号", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 弹出吐司提示用户是否添加成功
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        // 将new一个新的设备添加到设备集合中，下来框显示的就是设备集合
                        cameraBeans.add(new CameraBean(name, Integer.parseInt(id), Integer.parseInt(camera_id)));
                        // 刷新Spinner的数据适配器
                        customerAdapter.notifyDataSetChanged();
                        // 关闭对话框
                        alertDialog.dismiss();
                    }
                });

                // 取消按钮的点击事件
                btnCancal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                // 显示对话框
                alertDialog.show();
            }

            private void initView(View view) {
                etName = (EditText) view.findViewById(R.id.et_name);
                etId = (EditText) view.findViewById(R.id.et_id);
                etCamera = (EditText) view.findViewById(R.id.et_camera);
                btnCancal = (Button) view.findViewById(R.id.btn_cancal);
                btnOk = (Button) view.findViewById(R.id.btn_ok);
            }
        });

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到设置界面
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        // 开启线程实时获取数据
        getData();

        // 创建一个设备集合
        cameraBeans = new ArrayList<>();
        // 初始化Spinner的数据适配器
        customerAdapter = new CustomerAdapter();
        // 设置Spinner的数据适配器
        sp_select.setAdapter(customerAdapter);
    }

    /**
     * 获取返回的数据
     */
    private void getData() {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        // 接受服务端返回的数据
                        String imageData = TCPConnected.fetch_camera();
                        // 对数据进行判空处理
                        if (!TextUtils.isEmpty(imageData)) {
                            // 将拿到的base64的图片上护具通过decodeBase64ToImage方法将其转换成bitmap图片
                            Bitmap bitmap = EncodeAndDecode.decodeBase64ToImage(imageData);
                            if (bitmap != null) {
                                // 在主线中将图片数据显示到控件中
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        iv_image.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        // 点击两次返回按钮退出程序
        // flag默认为false
        if (flag) {
            super.onBackPressed();
        } else {
            // 将flag设置为true，第二次点击的时候退出程序
            flag = true;
            // 开启线程等待第二次点击
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        // 等待2s后如果不点击则将flag设置为false，则需要重新开始这段操作
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

    /**
     * Spinner的数据适配器
     */
    class CustomerAdapter extends BaseAdapter {

        private TextView tvText;

        @Override
        public int getCount() {
            return cameraBeans.size();
        }

        @Override
        public CameraBean getItem(int position) {
            return cameraBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = View.inflate(MainActivity.this, R.layout.item_sp, null);
            } else {
                view = convertView;
            }
            initView(view);
            tvText.setText(getItem(position).getName() + "-" + getItem(position).getId() + "-" + getItem(position).getCameraId());
            return view;
        }

        private void initView(View view) {
            tvText = (TextView) view.findViewById(R.id.tv_text);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TCPConnected.getSocket() != null && TCPConnected.getSocket().isConnected()) {
            tv_link_status.setText("通讯：已连接");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止拍照
        TCPConnected.stop_camera();
        // 界面销毁时停止服务
        TCPConnected.stop();
    }
}