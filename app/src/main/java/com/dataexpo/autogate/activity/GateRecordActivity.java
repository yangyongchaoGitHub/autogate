package com.dataexpo.autogate.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.OnItemClickListener;
import com.dataexpo.autogate.listener.OnItemLongClickListener;
import com.dataexpo.autogate.model.gate.ReportData;
import com.dataexpo.autogate.service.CardService;
import com.dataexpo.autogate.service.GateService;
import com.dataexpo.autogate.view.CircleImageView;

import java.util.List;

public class GateRecordActivity extends BascActivity implements View.OnClickListener, OnItemClickListener, OnItemLongClickListener {
    private static String TAG = GateRecordActivity.class.getSimpleName();
    private Bitmap bitmap = null;
    private boolean isShowCheck = false;
    private List<ReportData> datas;
    private GateAdapter adapter;

    private TextView btn_cancel;
    private TextView btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_gate);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        datas = CardService.getInstance().listAll();
        adapter.setDataList(datas);
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.gate_record_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new GateAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(this);
        adapter.setOnItemLongClickListener(this);

        findViewById(R.id.btn_gate_record_add).setOnClickListener(this);
        findViewById(R.id.btn_gate_record_back).setOnClickListener(this);

        btn_cancel = findViewById(R.id.btn_gate_record_cancel);
        btn_delete = findViewById(R.id.btn_gate_record_delete);
        btn_delete.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        findViewById(R.id.btn_gate_record_filter).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                break;
            case R.id.btn_user_manager_back:
                finish();
                break;
            case R.id.btn_user_manager_cancel:
                break;
            case R.id.btn_user_manager_delete:
                break;
                default:
        }
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onLongItemClick(View view, int position) {

    }

    private static class GateDataHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView text_number;
        private TextView text_time;
        private TextView text_direction;
        private CheckBox check_btn;

        public GateDataHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            text_number = itemView.findViewById(R.id.text_gate_number);
            text_time = itemView.findViewById(R.id.text_gate_time);
            text_direction = itemView.findViewById(R.id.text_gate_direction);
            check_btn = itemView.findViewById(R.id.record_gate_check_btn);
        }
    }

    public class GateAdapter extends RecyclerView.Adapter<GateDataHolder> implements View.OnClickListener {
        private List<ReportData> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;

        private void setDataList(List<ReportData> list) {
            mList = list;
        }

        private void setShowCheckBox(boolean showCheckBox) {
            mShowCheckBox = showCheckBox;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, (Integer) v.getTag());
            }
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        private void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
            mItemLongClickListener = itemLongClickListener;
        }

        @NonNull
        @Override
        public GateDataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_record_gate, parent, false);
            GateDataHolder viewHolder = new GateDataHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull GateDataHolder holder, final int position) {
            holder.itemView.setTag(position);
            if (mShowCheckBox) {
                holder.check_btn.setVisibility(View.VISIBLE);
                if (mList.get(position).isCheck()) {
                    holder.check_btn.setChecked(true);
                } else {
                    holder.check_btn.setChecked(false);
                }
            } else {
                holder.check_btn.setVisibility(View.GONE);
            }
            // 添加数据
            ReportData data = mList.get(position);
            holder.text_direction.setText("In".equals(data.getDirection()) ? "进" : "出");

            holder.text_number.setText(data.getNumber());
            holder.text_time.setText(Utils.formatTime(Long.parseLong(data.getTime()), "yyyy年MM月dd日HH时mm分ss秒"));


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if  (mItemLongClickListener != null) {
                        mItemLongClickListener.onLongItemClick(v, position);
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }
    }

    //获取一个白底bitmap white1是一个一像素白点
    private Bitmap getWhite() {
        if (bitmap == null) {
            bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.white1)).getBitmap();
        }
        return bitmap;
    }
}
