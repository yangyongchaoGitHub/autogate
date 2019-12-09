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
import android.widget.ImageView;
import android.widget.TextView;

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

import static com.dataexpo.autogate.service.GateService.LED_GREEN;
import static com.dataexpo.autogate.service.GateService.LED_RED;

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
//        UserService.getInstance().insert("testU2", "dataexpo.Ltd", "development",
//                "E0040150C71459F3", 1, "u23124");
//        UserService.getInstance().insert("testU3", "dataexpo.Ltd", "development",
//                "E0040150C715D092", 1, "u23125");
//        UserService.getInstance().insert("testU1", "dataexpo.Ltd", "development",
//                "E0040150C714EA6A", 1, "u23123");
        //{"id":12,"name":"user1","company":"dataexpo.Ltd","position":"development","cardCode":"E0040150C714EA6A","code":"u23123","image":"test","gender":1}

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
                startActivity(new Intent(mContext, MainSettingActivity.class));
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
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    public void response(int status, final Vector<ReportData> mReports) {
        if (mReports != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mReports.size() > 0) {
                        ReportData data = mReports.get(0);
                        User user = new User();
//                        user.cardCode = "E0040150C714EA6B";
//                        user.code = "u23123";
//                        user.name = "测试用户";
                        user.cardCode = data.getNumber();
                        if ("FFFFFFFFFFFFFFFF".equals(data.getNumber())) {
                            Log.i(TAG, " GATE  FFFFFFFFFFFFFFFF!!!!!!!!!!!!!!");
                            MainApplication.getInstance().getService().ledCtrl(LED_RED);
                            return;
                        }

                        User res = UserService.getInstance().findUserByCardCode(user);
                        if (res != null) {
                            //有此用户
                            final Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(res.code));
                            Log.i(TAG, " response image path: " + FileUtils.getUserPic(res.code));

                            iv_head.setImageBitmap(bitmap);
                            tv_name.setText(res.name);
                            MainApplication.getInstance().getService().ledCtrl(LED_GREEN);
                        }
                    }
                }
            });
        }
    }
}
