package com.dataexpo.autogate.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.DBUtils;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.listener.OnServeiceCallback;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.MainService;
import com.dataexpo.autogate.service.UserService;

import java.util.Vector;

public class GateActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = GateActivity.class.getSimpleName();
    private Context mContext;
    private ImageView iv_head;
    private TextView tv_name;

    private ServiceConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);
        mContext = this;
        initView();
        DBUtils.getInstance().create(mContext);
        /**
         * test code add a user
         */
//        DBUtils.getInstance().insertData("测试", "数展科技", "研发",
//                "123456", 1, "999999");

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MainService.MsgBinder msgBinder = (MainService.MsgBinder) service;
                MainApplication.getInstance().setService(msgBinder.getService());
                msgBinder.getService().setCallback(onServeiceCallback);

                if (MainApplication.getInstance().getService() != null) {
                    MainApplication.getInstance().getService().removeGateObserver(GateActivity.this);
                    MainApplication.getInstance().getService().addGateObserver(GateActivity.this);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        onServeiceCallback = new OnServeiceCallback() {
            @Override
            public void onCallback(int action) {
//                if (action == ACTION_TIMEOUT) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i(TAG, "onServeiceCallback " + MainWindow.this.hasWindowFocus());
//
//                            startActivity(new Intent(mContext, ScreensaverActivity.class));
//
//                            Toast.makeText(mContext, "this is on long ting", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
            }
        };

        bindService(new Intent(getApplicationContext(), MainService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        iv_head = findViewById(R.id.iv_gate);
        tv_name = findViewById(R.id.tv_gage_name);
        findViewById(R.id.btn_gosetting).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gosetting:
                startActivity(new Intent(mContext, GateSetActivity.class));
                break;
                default:
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().addGateObserver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MainApplication.getInstance().getService() != null) {
            MainApplication.getInstance().getService().removeGateObserver(this);
        }
    }

    @Override
    public void response(int status, final Vector<ReportData> mReports) {
        Log.i(TAG, "-------------- ");
        if (mReports != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ReportData rd : mReports) {
                        tv_name.setText(rd.getStrData());
                    }
                    User user = new User();
                    user.code = "999999";
                    if (UserService.getInstance().checkUser(user)) {
                        //有此用户
                        String absolutePath = FileUtils.getBatchImportDirectory()
                                + "/" + user.code + ".jpg";
                        final Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                        Log.i(TAG, " response image path: " + absolutePath);

                        iv_head.setImageBitmap(bitmap);
                    }
                }
            });
        }
    }
}
