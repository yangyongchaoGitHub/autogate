package com.dataexpo.autogate.activity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE;

public class UsersActivity extends BascActivity implements OnItemClickListener, OnItemLongClickListener, View.OnClickListener {
    private static final String TAG = UsersActivity.class.getSimpleName();
    private UserAdapter mUserAdapter;
    private TextView btn_cancel;
    private TextView btn_delete;

    private Bitmap bitmap = null;

    private boolean isShowCheck = false;
    private List<User> users = new ArrayList<>();

    private int index = 0;
    private int pageSize = 10;
    //加载更多数据时最后一项的索引
    private int lastLoadDataItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mContext = this;
        initView();
        //test();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        ArrayList<User> list = UserService.getInstance().limitList(index, pageSize);
        Log.i(TAG, "limitList end size: " + list.size() + " user size: " + users.size() +
                " lastLoadDataItemPosition " + lastLoadDataItemPosition);
        if (list.size() > 0) {
            if (list.size() != pageSize) {
                index = index + list.size();
            } else {
                index = index + pageSize;
            }
            users.addAll(list);
            mUserAdapter.setDataList(users);
            mUserAdapter.notifyDataSetChanged();

        } else {
            //隐藏底部
            if (mUserAdapter.footVh != null) {
                mUserAdapter.footVh.pb.setVisibility(View.INVISIBLE);
                mUserAdapter.footVh.tv.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void initView() {
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.user_info_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(layoutManager);

        mUserAdapter = new UserAdapter();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE &&
                        lastLoadDataItemPosition == mUserAdapter.getItemCount()){
                    if (mUserAdapter.footVh != null) {
                        mUserAdapter.footVh.pb.setVisibility(View.VISIBLE);
                        mUserAdapter.footVh.tv.setVisibility(View.VISIBLE);
                    }
                    new LoadDataThread().start();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager){
                    LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
                    int l = manager.findLastCompletelyVisibleItemPosition();
                    lastLoadDataItemPosition = firstVisibleItem+(l-firstVisibleItem)+1;
                }
            }
        });

        recyclerView.setAdapter(mUserAdapter);
        mUserAdapter.setItemClickListener(this);
        mUserAdapter.setOnItemLongClickListener(this);

        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_user_manager_back).setOnClickListener(this);

        btn_cancel = findViewById(R.id.btn_user_manager_cancel);
        btn_delete = findViewById(R.id.btn_user_manager_delete);
        btn_delete.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        findViewById(R.id.btn_user_manager_filter).setOnClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i(TAG, "onItemClick view: " + view + " position " + position);
        Intent intent  = new Intent(this, UserDetails.class);
        User u = users.get(position);
        intent.putExtra(Utils.EXTRA_EXPO_USRE, u);
        startActivity(intent);
    }

    @Override
    public void onLongItemClick(View view, int position) {
        Log.i(TAG, "onLongItemClick view: " + view + " position " + position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_user_manager_filter:
                Intent intent = new Intent(mContext, UserFilterActivity.class);
                startActivityForResult(intent, 1);
                break;

            case R.id.btn_user_manager_back:
                finish();
                break;

                default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActityResult!!!! ");
        users = UserService.getInstance().findUserNoFaceRegist();
        mUserAdapter.notifyDataSetChanged();
    }

//    private static class FaceUserHolder extends RecyclerView.ViewHolder {
//        private View itemView;
//        private TextView text_name;
//        private TextView text_code;
//        private TextView text_cardcode;
//        private CircleImageView image;
//        private CheckBox check_btn;
//
//        public FaceUserHolder(@NonNull View itemView) {
//            super(itemView);
//            this.itemView = itemView;
//            image = itemView.findViewById(R.id.user_info_image);
//            text_name = itemView.findViewById(R.id.text_user_name);
//            text_code = itemView.findViewById(R.id.text_user_code);
//            text_cardcode = itemView.findViewById(R.id.text_user_cardcode);
//            check_btn = itemView.findViewById(R.id.check_btn);
//        }
//    }

    public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
        private static final int ITEM_FOOTER = 0x1;
        private static final int ITEM_DATA = 0x2;
        private List<User> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;
        private FooterViewHolder footVh = null;

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
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_user_info_list, parent, false);
//            FaceUserHolder viewHolder = new FaceUserHolder(view);
//            view.setOnClickListener(this);
//
//            return viewHolder;

            View view;
            RecyclerView.ViewHolder vh = null;
            Log.i(TAG, "viewType: ------------------ " + viewType);
            switch (viewType){
                case ITEM_DATA:
                    view = LayoutInflater.from(mContext).inflate(R.layout.item_user_info_list, parent, false);
                    view.setOnClickListener(this);
                    vh = new DataViewHolder(view);
                    //使用代码设置宽高（xml布局设置无效时）
//                    view.setLayoutParams(new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    break;
                case ITEM_FOOTER:
                    view = LayoutInflater.from(mContext).inflate(R.layout.item_footer,null);
                    //使用代码设置宽高（xml布局设置无效时）
                    vh = new FooterViewHolder(view);
                    footVh = (FooterViewHolder) vh;
                    view.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    break;
            }
            return vh;
        }

        /**
         * 获取Item的View类型
         * @param position
         * @return
         */
        @Override
        public int getItemViewType(int position) {
            //根据 Item 的 position 返回不同的 Viewtype
            if (position == (getItemCount())-1){
                return ITEM_FOOTER;
            }else{
                return ITEM_DATA;
            }
        }

        /**
         * 绑定数据
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof DataViewHolder){
                DataViewHolder dataViewHolder = (DataViewHolder) holder;

                dataViewHolder.itemView.setTag(position);
                if (mShowCheckBox) {
                    dataViewHolder.check_btn.setVisibility(View.VISIBLE);
                    if (mList.get(position).isCheck()) {
                        dataViewHolder.check_btn.setChecked(true);
                    } else {
                        dataViewHolder.check_btn.setChecked(false);
                    }
                } else {
                    dataViewHolder.check_btn.setVisibility(View.GONE);
                }
                // 添加数据
                User user = mList.get(position);
                dataViewHolder.text_name.setText(user.name);
                String code = user.code;
                String cardcode = user.cardCode;

                dataViewHolder.text_code.setText(code);
                dataViewHolder.text_cardcode.setText(cardcode);

                Bitmap bitmap = BitmapFactory.decodeFile(FileUtils.getUserPic(user.image_name));
                Log.i(TAG, "bitmap: " + bitmap);
                if (bitmap == null) {
                    bitmap = getWhite();
                }
                dataViewHolder.image.setImageBitmap(bitmap);

                dataViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if  (mItemLongClickListener != null) {
                            mItemLongClickListener.onLongItemClick(v, position);
                            return true;
                        }
                        return false;
                    }
                });
            }else if (holder instanceof FooterViewHolder){

            }
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() + 1 : 0;
        }

        /**
         * 创建ViewHolder
         */
        public class DataViewHolder extends RecyclerView.ViewHolder{
            private View itemView;
            private TextView text_name;
            private TextView text_code;
            private TextView text_cardcode;
            private CircleImageView image;
            private CheckBox check_btn;

            public DataViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemView = itemView;
                image = itemView.findViewById(R.id.user_info_image);
                text_name = itemView.findViewById(R.id.text_user_name);
                text_code = itemView.findViewById(R.id.text_user_code);
                text_cardcode = itemView.findViewById(R.id.text_user_cardcode);
                check_btn = itemView.findViewById(R.id.check_btn);
            }
        }

        /**
         * 创建footer的ViewHolder
         */
        public class FooterViewHolder extends RecyclerView.ViewHolder{
            private ProgressBar pb;
            private TextView tv;
            public FooterViewHolder(View itemView) {
                super(itemView);
                pb = itemView.findViewById(R.id.progressBar);
                tv = itemView.findViewById(R.id.textView);
            }
        }
    }

    class LoadDataThread extends Thread{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initData();
                }
            });
        }
    }

    //获取一个白底bitmap white1是一个一像素白点
    private Bitmap getWhite() {
        if (bitmap == null) {
            bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.white1)).getBitmap();
        }
        return bitmap;
    }
//
//    private RecyclerView rv;
//    RvAdapter1 adapter;
//
//    private ArrayList<String> arrayList = new ArrayList<>();
//    //加载更多数据时最后一项的索引
//    private int lastLoadDataItemPosition;
//
//    private void test() {
//        rv = findViewById(R.id.user_info_recyclerview1);
//        rv.setLayoutManager(new LinearLayoutManager(this));//线性
//        for (int i=0;i<25;i++){
//            arrayList.add("第"+i+"条数据");
//        }
//
//        adapter = new RvAdapter1(this, arrayList);
//
//        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if (newState == SCROLL_STATE_IDLE &&
//                        lastLoadDataItemPosition == adapter.getItemCount()){
//                    new LoadDataThread().start();
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//
//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//                if (layoutManager instanceof LinearLayoutManager){
//                    LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
//                    int firstVisibleItem = manager.findFirstVisibleItemPosition();
//                    int l = manager.findLastCompletelyVisibleItemPosition();
//                    lastLoadDataItemPosition = firstVisibleItem+(l-firstVisibleItem)+1;
//                }
//            }
//        });
//
//        rv.setAdapter(adapter);
//    }
//
//    class LoadDataThread extends Thread{
//        @Override
//        public void run() {
//            //initData();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 55; i < 70; i++) {
//                        arrayList.add("第-"+i+"-条数据");
//                    }
//                    adapter.setmList(arrayList);
//                    adapter.notifyDataSetChanged();
//                }
//            });
//
//        }
//    }
//
//    public class RvAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
//            View.OnClickListener{
//        private static final int ITEM_FOOTER = 0x1;
//        private static final int ITEM_DATA = 0x2;
//        private Context mContext;
//        private RecyclerView recyclerView;
//        private ArrayList<String> mList;
//
//        public RvAdapter1() {}
//
//        public RvAdapter1(Context mContext, ArrayList<String> mList) {
//            this.mContext = mContext;
//            this.mList = mList;
//        }
//
//        public void setmList(ArrayList<String> mList) {
//            this.mList = mList;
//        }
//
//        /**
//         * 用于创建ViewHolder
//         * @param parent
//         * @return
//         */
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view ;
//            RecyclerView.ViewHolder vh = null;
//            switch (viewType){
//                case ITEM_DATA:
//                    view = LayoutInflater.from(mContext).inflate(R.layout.testitem,null);
//                    view.setOnClickListener(this);
//                    vh = new DataViewHolder(view);
//                    //使用代码设置宽高（xml布局设置无效时）
//                    view.setLayoutParams(new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
//                    break;
//                case ITEM_FOOTER:
//                    view = LayoutInflater.from(mContext).inflate(R.layout.item_footer,null);
//                    //使用代码设置宽高（xml布局设置无效时）
//                    vh = new FooterViewHolder(view);
//                    view.setLayoutParams(new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
//                    break;
//            }
//            return vh;
//        }
//
//        /**
//         * 获取Item的View类型
//         * @param position
//         * @return
//         */
//        @Override
//        public int getItemViewType(int position) {
//            //根据 Item 的 position 返回不同的 Viewtype
//            if (position == (getItemCount())-1){
//                return ITEM_FOOTER;
//            }else{
//                return ITEM_DATA;
//            }
//        }
//
//        /**
//         * 绑定数据
//         * @param holder
//         * @param position
//         */
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            if (holder instanceof DataViewHolder){
//                DataViewHolder dataViewHolder = (DataViewHolder) holder;
//                dataViewHolder.tv_data.setText(mList.get(position));
//            }else if (holder instanceof FooterViewHolder){
//
//            }
//        }
//
//        /**
//         * 选项总数
//         * @return
//         */
//        @Override
//        public int getItemCount() {
//            return mList.size()+1;
//        }
//
//        @Override
//        public void onClick(View view) {
//            //根据RecyclerView获得当前View的位置
//            int position = recyclerView.getChildAdapterPosition(view);
//            //程序执行到此，会去执行具体实现的onItemClick()方法
//
//        }
//
//        /**
//         * 创建ViewHolder
//         */
//        public class DataViewHolder extends RecyclerView.ViewHolder{
//            TextView tv_data;
//            public DataViewHolder(View itemView) {
//                super(itemView);
//                tv_data = (TextView) itemView.findViewById(R.id.tv_recycle);
//            }
//        }
//
//        /**
//         * 创建footer的ViewHolder
//         */
//        public class FooterViewHolder extends RecyclerView.ViewHolder{
//            public FooterViewHolder(View itemView) {
//                super(itemView);
//            }
//        }
//
//        /**
//         * 将RecycleView附加到Adapter上
//         */
//        @Override
//        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//            super.onAttachedToRecyclerView(recyclerView);
//            this.recyclerView= recyclerView;
//        }
//        /**
//         * 将RecycleView从Adapter解除
//         */
//        @Override
//        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
//            super.onDetachedFromRecyclerView(recyclerView);
//            this.recyclerView = null;
//        }
//    }
}
