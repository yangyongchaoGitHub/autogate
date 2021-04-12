package com.dataexpo.autogate.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.model.service.PageResult;
import com.dataexpo.autogate.model.service.UserEntityVo;
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.UserService;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SyncActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = SyncActivity.class.getSimpleName();

    private Button btn_sync_user;
    private Button btn_sync_record;
    //同步用户数据接口
    private Button btn_check;

    private ConstraintLayout cl_select;
    private ConstraintLayout cl_user;
    private ConstraintLayout cl_record;

    private CircularProgressView progress_view;

    private TextView tv_server_count_value;
    private TextView tv_local_count_value;
    private TextView tv_curr_count;

    private ImageView iv_sync_curr;

    private Retrofit mRetrofit;

    //同步用户数据时的参数
    private int totalServiceSize = 0;
    private int totalLocalSize = 0;
    private int currChange = 0;

    private int requestNo = 0;

    //同步线程
    UserSyncThread ust = null;

    UserImgSyncThread uist = null;

    //请求参数
    List<UserQueryConditionVo> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        mContext = this;
        mRetrofit = MainApplication.getmRetrofit();
        initView();
    }

    private void initView() {
        btn_sync_user = findViewById(R.id.btn_sync_user);
        btn_sync_record = findViewById(R.id.btn_sync_record);

        btn_sync_user.setOnClickListener(this);
        btn_sync_record.setOnClickListener(this);

        cl_select = findViewById(R.id.cl_select);
        cl_user = findViewById(R.id.cl_user);
        cl_record = findViewById(R.id.cl_record);

        btn_check = findViewById(R.id.btn_check);
        btn_check.setOnClickListener(this);

        progress_view = findViewById(R.id.progress_view);
        //progress_view.setProgress(50);

        tv_server_count_value = findViewById(R.id.tv_server_count_value);
        tv_local_count_value = findViewById(R.id.tv_local_count_value);
        tv_curr_count = findViewById(R.id.tv_curr_count);

        iv_sync_curr = findViewById(R.id.iv_sync_curr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                if (btn_check.getText().toString().equals("取消")) {
                    btn_check.setText("检查数据完整性");
                    if (ust != null) {
                        ust.setRunningStop();
                    }
                    return;
                }
                if (btn_check.getText().toString().equals("取消同步人像")) {
                    btn_check.setText("检查数据完整性");
                    if (uist != null) {
                        uist.setRunningStop();
                    }
                    return;
                }


                if (ust != null && ust.running) {
                    Toast.makeText(mContext, "正在同步", Toast.LENGTH_SHORT).show();
                } else {
                    ust = new UserSyncThread();
                    ust.start();
                    btn_check.setText("取消");
                }

                break;

            case R.id.btn_sync_user:
                cl_select.setVisibility(View.INVISIBLE);
                cl_user.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_sync_record:
                cl_select.setVisibility(View.INVISIBLE);
                cl_record.setVisibility(View.VISIBLE);
                break;

            default:
        }
    }

    //获取用户数据
    private UserQueryConditionVo queryUser(UserQueryConditionVo vo) {
        Log.i(TAG, "queryUser " + vo.getPageNo());
        ApiService apiService = mRetrofit.create(ApiService.class);

        vo.setRequestStatus(UserQueryConditionVo.STATUS_REQUESTING);

        Call<NetResult<PageResult<UserEntityVo>>> call = apiService.querySyncUser(vo);

        call.enqueue(new Callback<NetResult<PageResult<UserEntityVo>>>() {
            @Override
            public void onResponse(Call<NetResult<PageResult<UserEntityVo>>> call, Response<NetResult<PageResult<UserEntityVo>>> response) {
                Log.i(TAG, "onResponse" + response);

                NetResult<PageResult<UserEntityVo>> result = response.body();
                if (result == null) {
                    return;
                }

                fixLocal(result.getData());
            }

            @Override
            public void onFailure(Call<NetResult<PageResult<UserEntityVo>>> call, Throwable t) {
                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        u.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                        break;
                    }
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
        requestList.add(vo);
        return vo;
    }

    //获取用户数据
    private UserQueryConditionVo queryUserImage(UserQueryConditionVo vo) {
        Log.i(TAG, "queryUserImage " + vo.getPageNo());
        ApiService apiService = mRetrofit.create(ApiService.class);

        vo.setRequestStatus(UserQueryConditionVo.STATUS_REQUESTING);

        Call<ResponseBody> call = apiService.querySyncUserImage(vo.getEucode() + ".jpg");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "onResponse" + response);

                ResponseBody result = response.body();

                UserQueryConditionVo queryConditionVo = null;

                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        queryConditionVo = u;
                        break;
                    }
                }

                if (queryConditionVo == null) {
                    Toast.makeText(mContext, "请求过期", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (result != null) {
                    // 获取图片
                    Bitmap bitmap = BitmapFactory.decodeStream(result.byteStream());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_sync_curr.setImageBitmap(bitmap);
                        }
                    });

                    fixImgLocal(bitmap, queryConditionVo);
                } else {
                    Log.i(TAG, "图像不存在 " + queryConditionVo.getEucode() + " euid: " +queryConditionVo.getStartEuId());
                    //取消待同步状态
                    int res = UserService.getInstance().updateToNoSyncImg(queryConditionVo.getStartEuId());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                for (UserQueryConditionVo u: requestList) {
                    if (u.getRequestId() == requestNo) {
                        u.setRequestStatus(UserQueryConditionVo.STATUS_FAIL);
                        break;
                    }
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
        return vo;
    }

    //将请求到的图像
    private void fixImgLocal(Bitmap bitmap, UserQueryConditionVo queryConditionVo) {
        //把bitmap存到本地文件中
        File rootFile = FileUtils.getRegistedDirectory();
        File file = new File(rootFile.getPath() + "/" + queryConditionVo.getEucode() + ".jpg");

        if (FileUtils.saveBitmap(file, bitmap)) {
            UserService.getInstance().updateToSyncOk(queryConditionVo.getStartEuId(), queryConditionVo.getEucode());
            queryConditionVo.setRequestStatus(UserQueryConditionVo.STATUS_RESPONSE);
        }
    }

    //将请求到的数据和本地的数据进行比较
    private void fixLocal(PageResult<UserEntityVo> objectList) {
        List<UserEntityVo> userEntityVos = objectList.getObjectList();
        totalServiceSize = objectList.getCount();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_server_count_value.setText(totalServiceSize + "");
            }
        });

        String minId = "", maxId = "";
        if (userEntityVos.size() > 1) {
            minId = userEntityVos.get(0).getEuId() + "";
            maxId = userEntityVos.get(userEntityVos.size() - 1).getEuId() + "";
        }

        // 根据最上，最下两个euid进行数据库查询，
        //一个list
        List<User> users = UserService.getInstance().findUserByEuId(minId, maxId);

        List<UserEntityVo> sUpdateList = new ArrayList<>();
        List<User> updateList = new ArrayList<>();

        boolean bExist = false;
        //从数据里面对比，不管多少还是相等，有没有多的，有没有少的
        //多的list进行新增
        Iterator<UserEntityVo> iue = userEntityVos.iterator();
        Iterator<User> iu;

        //Log.i(TAG, "userEntityVos size " + userEntityVos.size() + " users size : " + users.size());

        while (iue.hasNext()) {
            UserEntityVo ue = iue.next();

            bExist = false;
            iu = users.iterator();
            //Log.i(TAG, "name: " + ue.getName() + " " + ue.getEuId());
            while (iu.hasNext()) {
                User u = iu.next();
                //Log.i(TAG, "name: " + ue.getName() + " " + ue.getEuId() + " " + u.pid + " " + u.position);
                if (u.pid == ue.getEuId()) {
                    //原有已同步数据
                    //检查是否有更改
                    if (!UserService.getInstance().compareUser(u, ue)) {
                        updateList.add(u);
                        //有更改
                    }
                    //已经校验有修改或者没有修改，都将他们去除
                    iue.remove();
                    iu.remove();
                    fixProgressShow();

                    bExist = true;
                    break;
                }
            }
            if (!bExist) {
                sUpdateList.add(ue);
                iue.remove();
                fixProgressShow();
            }
        }

        //新增
        if (sUpdateList.size() > 0) {
            Log.i(TAG, "sUpdateList.size() " + sUpdateList.size());
            UserService.getInstance().addDataFromService(sUpdateList);
        }

        //修改
        if (updateList.size() > 0) {
            Log.i(TAG, "updateList.size() " + updateList.size());
            UserService.getInstance().updateFromService(updateList);
        }

        //删除
        if (users.size() > 0) {
            Log.i(TAG, "users.size() " + users.size());
            UserService.getInstance().removeServiceNoExist(users);
        }

        //查询时的查询条件
        for (UserQueryConditionVo u: requestList) {
            if (u.getRequestId() == requestNo) {
                u.setRequestStatus(UserQueryConditionVo.STATUS_RESPONSE);
                break;
            }
        }
    }

    private void fixProgressShow() {
        currChange++;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_curr_count.setText("已同步: " + currChange + "");
                progress_view.setProgress((float) ((double) currChange / (double) totalServiceSize) * 100);
            }
        });
    }

    //用户数据同步线程
    class UserSyncThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            totalServiceSize = 0;
            currChange = 0;
            totalLocalSize = UserService.getInstance().countLocal();

            int pageNo = 0;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_local_count_value.setText(totalLocalSize + "");
                }
            });

            UserQueryConditionVo conditionVo = null;
            while (running) {
                //按批次进行服务器用户数据进行同步
                if (conditionVo == null) {
                    conditionVo = new UserQueryConditionVo();
                    conditionVo.setPageNo(0);
                    conditionVo.setPageSize(100);
                    conditionVo.setRequestId(++requestNo);
                    conditionVo.setThreadName(this.getName());
                    conditionVo = queryUser(conditionVo);

                } else if (conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_RESPONSE) {
                    //已经取完
                    if ((pageNo + 1) * 100 > totalServiceSize) {
                        setRunningStop();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_check.setText("取消同步人像");
                                Toast.makeText(mContext, "基础数据同步结束", Toast.LENGTH_SHORT).show();
                                totalLocalSize = UserService.getInstance().countLocal();
                                tv_local_count_value.setText(totalLocalSize + "");

                                int syncCount = UserService.getInstance().countImageLocalNoSync();
                                if (syncCount > 0) {
                                    if (uist == null) {
                                        uist = new UserImgSyncThread();
                                        uist.start();
                                    }
                                } else {
                                    btn_check.setText("检查数据完整性");
                                    Toast.makeText(mContext, "同步结束", Toast.LENGTH_SHORT).show();
                                    setRunningStop();
                                }
                            }
                        });
                        return;
                    }

                    requestList.remove(conditionVo);
                    conditionVo = new UserQueryConditionVo();
                    conditionVo.setPageNo(++pageNo);
                    conditionVo.setPageSize(100);
                    conditionVo.setRequestId(++requestNo);
                    conditionVo.setThreadName(this.getName());
                    conditionVo = queryUser(conditionVo);

                } else if (conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_FAIL) {
                    conditionVo = queryUser(conditionVo);
                }

                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

    //用户图像数据同步线程
    class UserImgSyncThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            totalServiceSize = 0;
            currChange = 0;
            totalLocalSize = UserService.getInstance().countImageLocalNoSync();

            int pageNo = 0;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress_view.setProgress(0);
                    tv_local_count_value.setText(totalLocalSize + "");
                }
            });

            UserQueryConditionVo conditionVo = null;
            while (running) {
                if (conditionVo == null || conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_RESPONSE) {
                    //一条一条进行查询
                    User user = UserService.getInstance().findNoSyncImgOne();
                    if (user != null) {
                        conditionVo = new UserQueryConditionVo();
                        conditionVo.setEucode(user.code);
                        conditionVo.setStartEuId(user.pid);
                        conditionVo.setRequestId(++requestNo);
                        conditionVo.setThreadName(this.getName());
                        queryUserImage(conditionVo);
                        requestList.add(conditionVo);

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_check.setText("检查数据完整性");
                                Toast.makeText(mContext, "同步结束", Toast.LENGTH_SHORT).show();
                                setRunningStop();
                            }
                        });

                    }
                } else if (conditionVo.getRequestStatus() == UserQueryConditionVo.STATUS_FAIL) {
                    queryUserImage(conditionVo);
                }

                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
