package com.dataexpo.autogate.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.OnItemClickListener;
import com.dataexpo.autogate.model.service.PermissionBean;
import com.dataexpo.autogate.model.service.Permissions;
import com.dataexpo.autogate.retrofitInf.ApiService;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.CardService;
import com.dataexpo.autogate.service.data.UserService;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.dataexpo.autogate.comm.Utils.MODEL_NAME;
import static com.dataexpo.autogate.comm.Utils.PERMISSION_ID;
import static com.dataexpo.autogate.comm.Utils.PERMISSION_NAME;

//选择运行模式
public class ModelSelectActivity extends BascActivity implements OnItemClickListener, View.OnClickListener {
    private static final String TAG = ModelSelectActivity.class.getSimpleName();
    private PermissionAdapter mAdapter;
    private RecyclerView recyclerView;
    private List<Permissions> permissions;

    private Retrofit mRetrofit;

    private Spinner spinner_modle;
    private TextView btn_back;
    private Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_select);
        mContext = this;
        mRetrofit = MainApplication.getmRetrofit();
        initView();
        initData();
    }

    private void initData() {
        mAdapter = new PermissionAdapter();
        recyclerView.setAdapter(mAdapter);
        goQuery();
        mAdapter.setItemClickListener(this);

        String model = Utils.getModel(mContext, MODEL_NAME);
        if (model.equals("普通模式")) {
            spinner_modle.setSelection(0);
        } else if (model.equals("验证识别模式")) {
            spinner_modle.setSelection(1);
        }
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler_model_select);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        spinner_modle = findViewById(R.id.spinner_modle);
        spinner_modle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "position: " + position);
                if (position == 0) {
                    recyclerView.setVisibility(View.INVISIBLE);
                } else if (position == 1) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "onNothingSelected: ");
            }
        });

        btn_back = findViewById(R.id.btn_back);
        btn_save = findViewById(R.id.btn_save);

        btn_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);
    }

    private void goQuery() {
        ApiService apiService = mRetrofit.create(ApiService.class);

        retrofit2.Call<PermissionBean> call = apiService.queryAccessGroup("10000");

        call.enqueue(new Callback<PermissionBean>() {
            @Override
            public void onResponse(retrofit2.Call<PermissionBean> call, Response<PermissionBean> response) {
                Log.i(TAG, "onResponse " + response);
                PermissionBean result = response.body();
                if (result == null) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.data != null && result.data.size() > 0) {
                            permissions = result.data;
                            mAdapter.setData(permissions);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(mContext, "项目查询门禁列表失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(retrofit2.Call<PermissionBean> call, Throwable t) {
                Log.i(TAG, "onFailure " + t.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "接口访问失败，请检查网络或联系服务器管理员", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "onFailure" + t.toString());
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        for (int i = 0; i < permissions.size(); i++) {
            if (i == position) {
                permissions.get(i).setbShow(true);
            } else {
                permissions.get(i).setbShow(false);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()) {
           case R.id.btn_back:
               this.finish();
               break;

           case R.id.btn_save:
               String modelName = "";
               if (spinner_modle.getSelectedItemPosition() == 0) {
                   modelName = "普通模式";
                   MainApplication.getInstance().setpModel(0);
               } else if (spinner_modle.getSelectedItemPosition() == 1) {
                   Permissions chooceP = null;
                   for (Permissions p: permissions) {
                       if (p.isbShow()) {
                           chooceP = p;
                           break;
                       }
                   }
                   if (chooceP == null) {
                       Toast.makeText(mContext, "请先选择权限地址", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   modelName = "验证识别模式";
                   MainApplication.getInstance().setpModel(1);
                   MainApplication.getInstance().setPermissions(chooceP);
                   Utils.saveModel(mContext, PERMISSION_ID, chooceP.getId() + "");
                   Utils.saveModel(mContext, PERMISSION_NAME, chooceP.getNames());
               }
               Utils.saveModel(mContext, MODEL_NAME, modelName);

               Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
               break;
           default:
       }
    }

    private static class PermissionHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private ImageView iv_image_show;
        private TextView tv_permission;

        public PermissionHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            iv_image_show = itemView.findViewById(R.id.iv_permission_show);
            tv_permission = itemView.findViewById(R.id.tv_item_permission);
        }
    }

    public class PermissionAdapter extends RecyclerView.Adapter<PermissionHolder> implements View.OnClickListener {
        private List<Permissions> mList;
        private OnItemClickListener mItemClickListener;

        public void setData(List<Permissions> list) {
            mList = list;
        }

        @Override
        public void onClick(View v) {
//            if (mItemClickListener != null) {
//                mItemClickListener.onItemClick(v, (Integer) v.getTag());
//            }
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public PermissionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_record, parent, false);
            PermissionHolder viewHolder = new PermissionHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionHolder holder, final int position) {
            holder.itemView.setTag(position);

            if (mList.get(position).isbShow()) {
                holder.iv_image_show.setVisibility(View.VISIBLE);
            } else {
                holder.iv_image_show.setVisibility(View.INVISIBLE);
            }
            holder.tv_permission.setText(mList.get(position).getNames());

            holder.iv_image_show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener!= null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });
            holder.tv_permission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener!= null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }
    }

    //设置点击输入框外隐藏键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
