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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.FileUtils;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.OnItemClickListener;
import com.dataexpo.autogate.listener.OnItemLongClickListener;
import com.dataexpo.autogate.model.FaceRecord;
import com.dataexpo.autogate.service.FaceService;
import com.dataexpo.autogate.view.CircleImageView;

import java.util.List;

public class FaceRecordActivity extends BascActivity implements OnItemClickListener, OnItemLongClickListener, View.OnClickListener {
    private static String TAG = FaceRecordActivity.class.getSimpleName();
    private Bitmap bitmap = null;
    private boolean isShowCheck = false;
    private List<FaceRecord> datas = null;
    private RecordAdapter adapter;

    private TextView btn_cancel;
    private TextView btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_face);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        datas = FaceService.getInstance().listAll();
        Log.i(TAG, "datas: " + datas + " adapter " + adapter);
        if (datas != null) {
            adapter.setDataList(datas);
            adapter.notifyDataSetChanged();
        }
    }

    private void initView() {
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.face_record_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecordAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(this);
        adapter.setOnItemLongClickListener(this);

        findViewById(R.id.btn_face_record_add).setOnClickListener(this);
        findViewById(R.id.btn_face_record_back).setOnClickListener(this);

        btn_cancel = findViewById(R.id.btn_face_record_cancel);
        btn_delete = findViewById(R.id.btn_face_record_delete);
        btn_delete.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        findViewById(R.id.btn_face_record_filter).setOnClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onLongItemClick(View view, int position) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_face_record_back:
                finish();
                break;
                default:
        }
    }

    private static class FaceRecordHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView text_name;
        private TextView text_time;
        private TextView text_cardcode;
        private ImageView image;
        private CheckBox check_btn;

        public FaceRecordHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.record_face_image);
            text_name = itemView.findViewById(R.id.text_face_name);
            text_time = itemView.findViewById(R.id.text_face_time);
            text_cardcode = itemView.findViewById(R.id.text_face_cardcode);
            check_btn = itemView.findViewById(R.id.record_face_check_btn);
        }
    }

    public class RecordAdapter extends RecyclerView.Adapter<FaceRecordHolder> implements View.OnClickListener {
        private List<FaceRecord> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;

        private void setDataList(List<FaceRecord> list) {
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
        public FaceRecordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_record_face, parent, false);
            FaceRecordHolder viewHolder = new FaceRecordHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull FaceRecordHolder holder, final int position) {
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
            FaceRecord record = mList.get(position);
            holder.text_name.setText(record.name);
            holder.text_cardcode.setText(record.cardcode);
            holder.text_time.setText(Utils.formatTime(Long.parseLong(record.time), "yyyy年MM月dd日HH时mm分ss秒"));

            Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getFacePicPath(record.imageName));
            Log.i(TAG, "bitmap: " + bitmap);
            if (bitmap == null) {
                bitmap = getWhite();
            }
            holder.image.setImageBitmap(bitmap);

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
