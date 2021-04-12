package com.dataexpo.autogate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.OnItemClickListener;
import com.dataexpo.autogate.listener.OnItemLongClickListener;
import com.dataexpo.autogate.model.Rfid;
import com.dataexpo.autogate.model.RfidVo;
import com.dataexpo.autogate.service.GateService;
import com.dataexpo.autogate.service.MainApplication;
import com.dataexpo.autogate.service.data.RfidService;

import java.util.ArrayList;
import java.util.List;

import static android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE;
import static com.dataexpo.autogate.service.GateService.*;

public class RfidActivity extends BascActivity implements OnItemClickListener, OnItemLongClickListener, View.OnClickListener {
    private static final String TAG = RfidActivity.class.getSimpleName();
    private RfidAdapter mAdapter;
    private int index = 0;
    private int pageSize = 10;
    //加载更多数据时最后一项的索引
    private int lastLoadDataItemPosition;

    private List<Rfid> rfids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);
        mContext = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rfids.clear();
        index = 0;
        initData();
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
    public void responseStatus(final Rfid rfid) {
        Log.i(TAG, "responseStatus ------ " + rfid.status);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Rfid r: rfids) {
                    if (r.getId() == rfid.getId()) {
                        r.status = rfid.status;
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }

    private void initView() {
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.user_info_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new RfidAdapter();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE &&
                        lastLoadDataItemPosition == mAdapter.getItemCount()){
                    if (mAdapter.footVh != null) {
                        mAdapter.footVh.pb.setVisibility(View.VISIBLE);
                        mAdapter.footVh.tv.setVisibility(View.VISIBLE);
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

        recyclerView.setAdapter(mAdapter);
        mAdapter.setItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);

        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i(TAG, "onItemClick view: " + view + " position " + position);
        //点击item时进入详情界面
        Intent intent  = new Intent(mContext, RfidInfoActivity.class);
        Rfid rfid = rfids.get(position);
        RfidVo transf = new RfidVo(rfid.getId(), rfid.getIp(), rfid.getPort(), rfid.getName(), rfid.getRemark(), rfid.status);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Utils.EXTRA_RFID_INFO, transf);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onLongItemClick(View view, int position) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                this.finish();
                break;

            case R.id.btn_add:
                startActivity(new Intent(mContext, RfidAddActivity.class));
                break;
            default:
        }
    }

    public class RfidAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
        private static final int ITEM_FOOTER = 0x1;
        private static final int ITEM_DATA = 0x2;
        private List<Rfid> mList;
        private boolean mShowCheckBox;
        private OnItemClickListener mItemClickListener;
        private OnItemLongClickListener mItemLongClickListener;
        private FooterViewHolder footVh = null;

        private void setDataList(List<Rfid> list) {
            mList = list;
        }

        private void setShowCheckBox(boolean showCheckBox) {
            mShowCheckBox = showCheckBox;
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "------------------------------- " + v);
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
            View view;
            RecyclerView.ViewHolder vh = null;
            Log.i(TAG, "viewType: ------------------ " + viewType);
            switch (viewType){
                case ITEM_DATA:
                    view = LayoutInflater.from(mContext).inflate(R.layout.item_rfid_info_list, parent, false);
                    view.setOnClickListener(this);
                    vh = new DataViewHolder(view);

                    //使用代码设置宽高（xml布局设置无效时）
                    view.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
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

                // 添加数据
                Rfid rfid = mList.get(position);
                dataViewHolder.tv_name.setText(rfid.getName());

                dataViewHolder.tv_ip.setText(rfid.getIp());
                dataViewHolder.tv_port.setText(rfid.getPort());

                if (rfid.status == 0) {
                    rfid.status = GateService.getInstance().getmStatus(rfid.getId());
                }

                Log.i(TAG, " status: " + rfid.status);
                String statuValue = "··· ···";
                switch (rfid.status) {
                    case GATE_STATUS_INIT_NULL_SETTING:
                        statuValue = "设置的端口或者ip为空";
                        break;

                    case GATE_STATUS_START:
                        statuValue = "接口open成功,尝试通讯中，如未成功，需要检查端口是否正确";
                        break;

                    case GATE_STATUS_RUN:
                        statuValue = "正常运行中";
                        break;

                    case GATE_STATUS_INIT_OPEN_FAIL:
                        statuValue = "打开接口错误";
                        break;

                    case GATE_STATUS_INIT_NULL_CONTEXT:
                        statuValue = "上下文为空";
                        break;

                    case GATE_STATUS_READ_ERROR:
                        statuValue = "和设备通信失败";
                        break;

                    case GATE_STATUS_NULL:
                        statuValue = "设备不存在";
                        break;
                    default:
                }
                dataViewHolder.tv_status.setText(statuValue);

                dataViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mItemLongClickListener != null) {
                            mItemLongClickListener.onLongItemClick(v, position);
                            return true;
                        }
                        return false;
                    }
                });

            }else if (holder instanceof UsersActivity.UserAdapter.FooterViewHolder){

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
            private TextView tv_name;
            private TextView tv_ip;
            private TextView tv_port;
            private TextView tv_status;

            public DataViewHolder(@NonNull View itemView) {
                super(itemView);
                this.itemView = itemView;
                tv_name = itemView.findViewById(R.id.tv_name);
                tv_ip = itemView.findViewById(R.id.tv_ip);
                tv_port = itemView.findViewById(R.id.tv_port);
                tv_status = itemView.findViewById(R.id.tv_status);
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
                pb.setVisibility(View.INVISIBLE);
                tv.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 动态加载数据线程
     */
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

    private void initData() {
        ArrayList<Rfid> list = RfidService.getInstance().limitList(index, pageSize);
        Log.i(TAG, "limitList end size: " + list.size() + " rfids size: " + rfids.size() +
                " lastLoadDataItemPosition " + lastLoadDataItemPosition);
        if (list.size() > 0) {
            index += list.size();
            rfids.addAll(list);
            mAdapter.setDataList(rfids);
            mAdapter.notifyDataSetChanged();

        } else {
            //隐藏底部
            if (mAdapter.footVh != null) {
                mAdapter.footVh.pb.setVisibility(View.INVISIBLE);
                mAdapter.footVh.tv.setVisibility(View.INVISIBLE);
            }
        }
    }
}
