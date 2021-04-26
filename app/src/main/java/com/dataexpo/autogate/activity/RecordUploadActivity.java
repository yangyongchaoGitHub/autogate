package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.model.service.PageResult;
import com.dataexpo.autogate.model.service.RecordVo;
import com.dataexpo.autogate.model.service.UserEntityVo;
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.model.service.mp.MsgBean;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.comm.Utils.EXPO_ADDRESS;
import static com.dataexpo.autogate.comm.Utils.EXPO_ID;

public class RecordUploadActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = RecordUploadActivity.class.getSimpleName();
    private Button btn_check;
    private TextView tv_total_count_value;          // 本地数据总数
    private TextView tv_up_count_value;             // 当前上传总数

    private CircularProgressView progress_view;             // 总进度
    private CircularProgressView progress_view_mini;             // 进度等待
    private ImageView iv_success_base;             // 成功图标

    //同步记录时的参数
    private int totalLocalSize = 0;
    private int currChange = 0;

    private Retrofit mRetrofit;
    //同步线程
    UploadThread ut = null;

    String serialNum = Utils.getSerialNumber();

    private Map<Integer, UploadRequest> requestMap = new HashMap<>();

    class UploadRequest {
        public static final int STATUS_INIT = 0;
        public static final int STATUS_REQUESTING = 1;
        public static final int STATUS_RESPONSE = 2;
        public static final int STATUS_FAIL = 3;
        public static final int STATUS_TIMEOUT = 4;

        int hscode;
        ReportData reportData;
        int requestStatus = STATUS_INIT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_upload);
        mContext = this;
        mRetrofit = MainApplication.getmRetrofit();
        initView();
    }

    private void initView() {
        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);
        btn_check = findViewById(R.id.btn_check);
        btn_check.setOnClickListener(this);
        tv_total_count_value = findViewById(R.id.tv_total_count_value);
        tv_up_count_value = findViewById(R.id.tv_up_count_value);
        progress_view = findViewById(R.id.progress_view);
        progress_view_mini = findViewById(R.id.progress_view_mini);
        iv_success_base = findViewById(R.id.iv_success_base);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                if (btn_check.getText().toString().equals("取消")) {
                    btn_check.setText("开始上传");
                    progress_view_mini.setVisibility(View.INVISIBLE);
                    if (ut != null) {
                        ut.setRunningStop();
                    }
                    return;
                }
                if (ut != null && ut.running) {
                    Toast.makeText(mContext, "正在同步", Toast.LENGTH_SHORT).show();
                } else {

                    iv_success_base.setVisibility(View.INVISIBLE);
                    progress_view_mini.setVisibility(View.VISIBLE);
                    tv_total_count_value.setText("");
                    tv_up_count_value.setText("");
                    ut = new UploadThread();
                    ut.start();
                    btn_check.setText("取消");
                }
                break;

            case R.id.ib_back:
            case R.id.tv_back:
                this.finish();
                break;
                default:
        }
    }

    //上传数据
    private int mpUpload(UploadRequest uploadRequest) {
        ApiService apiService = mRetrofit.create(ApiService.class);
        uploadRequest.reportData.setSerialNum(serialNum);

        ReportData reportData = uploadRequest.reportData;
        RecordVo recordVo = new RecordVo(reportData.getEucode(),
                new Date(Long.parseLong(reportData.getTime())), serialNum,
                Integer.parseInt(Utils.getEXPOConfig(mContext, EXPO_ID)),
                Utils.getEXPOConfig(mContext, EXPO_ADDRESS), reportData.getNumber(),
                reportData.getPid() + "");

        Call<MsgBean<String>> call = apiService.mpUploadRecord(recordVo);

        call.enqueue(new Callback<MsgBean<String>>() {
            @Override
            public void onResponse(Call<MsgBean<String>> call, Response<MsgBean<String>> response) {
                Log.i(TAG, "onResponse" + response);

                MsgBean<String> result = response.body();
                UploadRequest request = requestMap.get(call.hashCode());

                if (result != null && result.code != null && result.code.equals(200)) {
                    currChange++;
                    fixProgressShow();

                    if (request != null) {
                        CardService.getInstance().removeById(request.reportData.getId());
                        request.requestStatus = UploadRequest.STATUS_RESPONSE;
                    }

                } else {
                    if (request != null) {
                        request.requestStatus = UploadRequest.STATUS_FAIL;
                    }
                }
            }

            @Override
            public void onFailure(Call<MsgBean<String>> call, Throwable t) {
                UploadRequest request = requestMap.get(call.hashCode());
                if (request != null) {
                    request.requestStatus = UploadRequest.STATUS_FAIL;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
        uploadRequest.hscode = call.hashCode();
        uploadRequest.requestStatus = UploadRequest.STATUS_REQUESTING;
        requestMap.put(call.hashCode(), uploadRequest);
        return call.hashCode();
    }

    //上传数据
    private int upload(UploadRequest uploadRequest) {
        ApiService apiService = mRetrofit.create(ApiService.class);
        uploadRequest.reportData.setSerialNum(serialNum);
        Call<NetResult<String>> call = apiService.uploadRecord(uploadRequest.reportData);

        call.enqueue(new Callback<NetResult<String>>() {
            @Override
            public void onResponse(Call<NetResult<String>> call, Response<NetResult<String>> response) {
                Log.i(TAG, "onResponse" + response);

                NetResult<String> result = response.body();
                UploadRequest request = requestMap.get(call.hashCode());

                if (result != null && result.getErrcode() != null && result.getErrcode().equals(0)) {
                    currChange++;
                    fixProgressShow();

                    if (request != null) {
                        CardService.getInstance().removeById(request.reportData.getId());
                        request.requestStatus = UploadRequest.STATUS_RESPONSE;
                    }

                } else {
                    if (request != null) {
                        request.requestStatus = UploadRequest.STATUS_FAIL;
                    }
                }
            }

            @Override
            public void onFailure(Call<NetResult<String>> call, Throwable t) {
                UploadRequest request = requestMap.get(call.hashCode());
                if (request != null) {
                    request.requestStatus = UploadRequest.STATUS_FAIL;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
        uploadRequest.hscode = call.hashCode();
        uploadRequest.requestStatus = UploadRequest.STATUS_REQUESTING;
        requestMap.put(call.hashCode(), uploadRequest);
        return call.hashCode();
    }

    private void fixProgressShow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress_view_mini.setVisibility(View.INVISIBLE);
                tv_up_count_value.setText("" + currChange);
                progress_view.setProgress((float) ((double) currChange / (double) totalLocalSize) * 100);
            }
        });
    }

    //上传数据线程
    class UploadThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            currChange = 0;
            totalLocalSize = CardService.getInstance().countOfTotalGate();

            int pageNo = 0;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_total_count_value.setText(totalLocalSize + "");
                }
            });

            UploadRequest uploadRequest = null;
            while (running) {
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //按批次进行服务器用户数据进行同步
                if (uploadRequest == null || uploadRequest.requestStatus == UploadRequest.STATUS_RESPONSE) {
                    if (uploadRequest == null) {
                        uploadRequest = new UploadRequest();
                    }

                    uploadRequest.reportData = CardService.getInstance().getOne();
                    if (uploadRequest.reportData != null) {
                        requestMap.remove(uploadRequest.hscode);

                        int hscode = mpUpload(uploadRequest);

                        requestMap.put(hscode, uploadRequest);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress_view.setProgress(0);
                                iv_success_base.setVisibility(View.VISIBLE);
                                btn_check.setText("开始上传");
                                Toast.makeText(mContext, "同步结束", Toast.LENGTH_SHORT).show();
                                setRunningStop();
                                ut = null;
                            }
                        });
                    }

                } else if (uploadRequest.requestStatus == UploadRequest.STATUS_FAIL) {
                    mpUpload(uploadRequest);
                }
            }
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunningStop() {
            running = false;
        }
    }
}
