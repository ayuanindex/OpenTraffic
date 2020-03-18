package com.realmax.cars;

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

import com.realmax.cars.bean.CameraBean;
import com.realmax.cars.tcputil.TCPConnected;
import com.realmax.cars.utils.EncodeAndDecode;

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
                CameraBean cameraBean = cameraBeans.get(position);
                tv_device.setText("设备：" + cameraBean.getName());
                tv_device_id.setText("ID：" + cameraBean.getId());
                tv_camera_number.setText("摄像头：" + cameraBean.getCameraId());
                TCPConnected.start_camera(cameraBean.getName(), cameraBean.getId(), cameraBean.getCameraId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        iv_add.setOnClickListener(new View.OnClickListener() {
            private Button btnOk;
            private Button btnCancal;
            private EditText etCamera;
            private EditText etId;
            private EditText etName;

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.create();
                View view = View.inflate(MainActivity.this, R.layout.dialog_add_camera, null);
                alertDialog.setView(view);
                initView(view);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        cameraBeans.add(new CameraBean(name, Integer.parseInt(id), Integer.parseInt(camera_id)));
                        customerAdapter.notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                });

                btnCancal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
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
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        // 开启线程实时获取数据
        getData();
        cameraBeans = new ArrayList<>();
        customerAdapter = new CustomerAdapter();
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
                        if (!TextUtils.isEmpty(imageData)) {
                            Bitmap bitmap = EncodeAndDecode.decodeBase64ToImage(imageData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    iv_image.setImageBitmap(bitmap);
                                }
                            });
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

}