package com.dataexpo.autogate.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.dataexpo.autogate.model.TestData;
import com.dataexpo.autogate.model.User;
import com.dataexpo.autogate.service.UserService;
import com.dataexpo.autogate.view.CircleImageView;

import java.util.List;

public class UsersActivity extends BascActivity implements OnItemClickListener, OnItemLongClickListener, View.OnClickListener {
    private static final String TAG = UsersActivity.class.getSimpleName();
    private UserAdapter mUserAdapter;
    private TextView btn_cancel;
    private TextView btn_delete;

    private Bitmap bitmap = null;

    private boolean isShowCheck = false;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mContext = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        users = UserService.getInstance().listAll();
        mUserAdapter.setDataList(users);
        mUserAdapter.notifyDataSetChanged();
    }

    private void initView() {
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.user_info_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(layoutManager);

        mUserAdapter = new UserAdapter();
        recyclerView.setAdapter(mUserAdapter);
        mUserAdapter.setItemClickListener(this);
        mUserAdapter.setOnItemLongClickListener(this);

        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_user_manager_back).setOnClickListener(this);

        btn_cancel = findViewById(R.id.btn_user_manager_cancel);
        btn_delete = findViewById(R.id.btn_user_manager_delete);
        btn_delete.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

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
            case R.id.btn_user_manager_back:
                finish();
                break;

                default:
        }
    }


    private static class FaceUserHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView text_name;
        private TextView text_code;
        private TextView text_cardcode;
        private CircleImageView image;
        private CheckBox check_btn;

        public FaceUserHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.user_info_image);
            text_name = itemView.findViewById(R.id.text_user_name);
            text_code = itemView.findViewById(R.id.text_user_code);
            text_cardcode = itemView.findViewById(R.id.text_user_cardcode);
            check_btn = itemView.findViewById(R.id.check_btn);
        }
    }

    public class UserAdapter extends RecyclerView.Adapter<FaceUserHolder> implements View.OnClickListener {
        private List<User> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;

        private void setDataList(List<User> list) {
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
        public FaceUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_info_list, parent, false);
            FaceUserHolder viewHolder = new FaceUserHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull FaceUserHolder holder, final int position) {
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
            User user = mList.get(position);
            holder.text_name.setText(user.name);
            String code = user.code;
            String cardcode = user.cardCode;

            holder.text_code.setText(code);
            holder.text_cardcode.setText(cardcode);

            Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
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
    private Bitmap getWhite() {
        if (bitmap == null) {
            bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.white1)).getBitmap();
        }
        return bitmap;
    }
}
