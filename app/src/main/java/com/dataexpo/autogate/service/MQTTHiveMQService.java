package com.dataexpo.autogate.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dataexpo.autogate.comm.JsonUtil;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.MQTTObserver;
import com.dataexpo.autogate.listener.MQTTSubject;
import com.dataexpo.autogate.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dataexpo.autogate.comm.Utils.MQTT_CLIENTID;
import static com.dataexpo.autogate.comm.Utils.MQTT_HOST;
import static com.dataexpo.autogate.comm.Utils.MQTT_NAME;
import static com.dataexpo.autogate.comm.Utils.MQTT_PORT;
import static com.dataexpo.autogate.comm.Utils.MQTT_PSWD;
import static com.dataexpo.autogate.comm.Utils.MQTT_TOPIC;

public class MQTTHiveMQService extends MQTTSubject {
    private static final String TAG = MQTTService.class.getSimpleName();
    private Context mContext;
    private String host = "";
    private String port = "";
    private String name = "";
    private String pswd = "";
    private static String topic = "";
    private String clientId = "";
    private boolean bRun = true;
    //hiveMQ 消费者实例
//    Mqtt3AsyncClient client;
//    //初始化线程
//    private ConnectThread connectThread = null;
//
    //连接失败
    public static final int MQTT_CONNECT_INIT = 1;
    //正在连接
    public static final int MQTT_CONNECT_ING = 2;
    //连接成功
    public static final int MQTT_CONNECT_SUCCESS = 3;
    //连接中断
    public static final int MQTT_CONNETC_FAIL = 4;

    private int conn_status = MQTT_CONNECT_INIT;
//
//    private volatile int num = 0;
//
//    private static final int threadCount = 5;
//    //    //线程池 5个线程同时进行解析
//    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//    private List<User> currUsers = new ArrayList<>();
//    private User[] syncUser = new User[threadCount];
//
//    private final Object syncThreadUser = new User();
//
//    private MQTTHiveMQService(){};
//
//    public void init(Context context) {
//        mContext = context;
//        for (int i = 0; i < threadCount; i++) {
//            syncUser[i] = null;
//        }
//
//        if (connectThread == null) {
//            connectThread = new ConnectThread();
//            connectThread.start();
//        }
//
//        //TODO: bRun
//        for (int i = 0; i < threadCount; i++) {
//            executorService.execute(new ConsumerThread());
//        }
//    }
//
//    private static class HolderClass {
//        private static final MQTTHiveMQService instance = new MQTTHiveMQService();
//    }
//
//    /**
//     * 单例模式
//     */
//    public static MQTTHiveMQService getInstance() {
//        return MQTTHiveMQService.HolderClass.instance;
//    }

    @Override
    public void notifyStatus(int status) {
        for(Object obs: observers) {
            ((MQTTObserver)obs).responseMQTTStatus(conn_status);
        }
    }
//
//    public void restart() {
//        if (conn_status == MQTT_CONNECT_SUCCESS) {
//            client.disconnect();
//        }
//    }
//
//    private class ConnectThread extends Thread {
//        @Override
//        public void run() {
//            int delay = 1000;
//            while (bRun) {
//                if (conn_status == MQTT_CONNETC_FAIL) {
//                    client = null;
//                    conn_status = MQTT_CONNECT_INIT;
//                }
//
//                if (conn_status == MQTT_CONNECT_INIT) {
//                    Thread th=Thread.currentThread();
//                    System.out.println("ConnectThread main is " + th.getName());
//                    doClientConnection();
//                }
//
//                notifyStatus(conn_status);
//
//                try {
//                    Thread.sleep(delay);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    @SuppressLint("NewApi")
//    private void doClientConnection() {
//        if (!initParameter() && isConnectIsNormal()) {
//            conn_status = MQTT_CONNECT_ING;
//            client = MqttClient.builder()
//                    .useMqttVersion3()
//                    .identifier(clientId)
//                    .serverHost(host)
//                    .serverPort(Integer.parseInt(port))
//                    .addDisconnectedListener(listener -> {
//                        if (conn_status != MQTT_CONNECT_SUCCESS) {
//                            Log.i(TAG, "connetcion lost!!!");
//                            conn_status = MQTT_CONNETC_FAIL;
//                        }
//                    })
//                    .buildAsync();
//
//            client.connectWith()
//                    .simpleAuth()
//                    .username(name)
//                    .password(pswd.getBytes())
//                    .applySimpleAuth()
//                    .keepAlive(10)
//                    .send()
//                    .whenComplete((connAck, throwable) -> {
//                        if (throwable != null) {
//                            // handle failure
//                            Log.i(TAG, "连接失败 ");
//                            if (client != null) {
//                                client = null;
//                            }
//                            conn_status = MQTT_CONNECT_INIT;
//
//                        } else {
//                            if (conn_status != MQTT_CONNECT_SUCCESS) {
//                                conn_status = MQTT_CONNECT_SUCCESS;
//                                Log.i(TAG, "连接成功 ");
//                                subscribe();
//                            }
//                            // setup subscribes or start publishing
//                        }
//                    });
//        }
//    }
//
//    private synchronized int getThreadNumber() {
//        return num++;
//    }
//
//    private class ConsumerThread implements Runnable {
//        @Override
//        public void run() {
//            User user = null;
//            int number = getThreadNumber();
//            Log.i(TAG, "my number is " + number);
//            boolean bInOther = false;
//            while (bRun) {
//                synchronized (MQTTService.class) {
//                    if (currUsers.size() > 0) {
//                        Log.i(TAG, "messages wait " + currUsers.size());
//                        user = currUsers.get(0);
//                        currUsers.remove(0);
//                    }
//                }
//
//                if (user == null) {
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    continue;
//                }
//
//                try {
//                    Log.i(TAG, "ConsumerThread insert che code number " + number + " " + user.code + " " + (new Date()).getTime());
//                    //检测是否在数据库有重复用户并告知正在处理此code的id给其他线程
//                    if (UserService.getInstance().haveByCode(user)) {
//                        Log.i(TAG, "用户： " + user.code + " 重复！！！！！");
//                        continue;
//                    }
//                    //告知正在处理此code的id给其他线程
//                    bInOther = false;
//                    synchronized (syncThreadUser) {
//                        for (int i = 0; i < threadCount; i++) {
//                            if (syncUser[i] != null && user.code.equals(syncUser[i].code)) {
//                                bInOther = true;
//                                break;
//                            }
//                        }
//                        if (bInOther) {
//                            continue;
//                        }
//                        syncUser[number] = user;
//                    }
//
//                    Log.i(TAG, "ConsumerThread insert sta code number " + number + " " + user.code + " " + (new Date()).getTime());
//                    //Log.i(TAG, "user name " + user.name + " cardCode " + user.cardCode + " image:" + user.image);
//                    UserService.getInstance().insert(user);
//
////            Log.i(TAG, String.valueOf(Environment.getExternalStorageDirectory()));
////            File file = FileUtils.isFileExist(String.valueOf(Environment.getExternalStorageDirectory()), "etstpg");
////            Log.i(TAG, "file: " + file);
////            String sss = FileUtils.toBase64(file);
////            Log.i(TAG, "file base64: " + sss);
//                    //FileUtils.writeTxtFile(sss, String.valueOf(Environment.getExternalStorageDirectory()) + "/testbase64");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    user = null;
//                }
//                Log.i(TAG, "ConsumerThread insert end " + (new Date()).getTime());
//            }
//        }
//    }
//
//    @SuppressLint("NewApi")
//    private void subscribe() {
//        client.subscribeWith()
//                .topicFilter("topic9")
//                .callback(publish -> {
//                    //只接受指定的主题消息
//                    if (publish.getTopic().toString().equals(topic)) {
//                        callback(publish.getPayloadAsBytes());
//                    }
//                })
//                .send()
//                .whenComplete((subAck, throwable) -> {
//                    if (throwable != null) {
//                        Log.i(TAG, "监听主题失败 " );
//                        // Handle failure to subscribe
//                    } else {
//                        Log.i(TAG, "监听主题成功 ");
//                        // Handle successful subscription, e.g. logging or incrementing a metric
//                    }
//                });
//    }
//
//    private void callback(byte[] payloadAsBytes) {
//        User user = null;
//        try {
//            user = JsonUtil.getInstance().json2obj(new String(payloadAsBytes), User.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (user == null) {
//            return;
//        }
//
//        while (currUsers.size() > 20) {
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        synchronized (syncThreadUser) {
//            //将待处理数据放入messages
//            // 对待的情况是 broker同时发送了多条重复的消息，若是都放到messages，则有可能导致内存溢出
//            for (int i = 0; i < currUsers.size(); i++) {
//                if (currUsers.get(i) != null && user.code.equals(currUsers.get(i).code)) {
//                    return;
//                }
//            }
//            currUsers.add(user);
//        }
//    }
//
//    private boolean initParameter() {
//        host = Utils.getMQTTConfig(mContext, MQTT_HOST);
//        port = Utils.getMQTTConfig(mContext, MQTT_PORT);
//        name = Utils.getMQTTConfig(mContext, MQTT_NAME);
//        pswd = Utils.getMQTTConfig(mContext, MQTT_PSWD);
//        clientId = Utils.getMQTTConfig(mContext, MQTT_CLIENTID);
//        topic = Utils.getMQTTConfig(mContext, MQTT_TOPIC);
//        return "".equals(host) || "".equals(port) || "".equals(name) ||
//                "".equals(pswd) || "".equals(clientId) || "".equals(topic);
//    }
//
//    /** 判断网络是否连接 */
//    private boolean isConnectIsNormal() {
//        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
//        if (info != null && info.isAvailable()) {
//            String name = info.getTypeName();
//            Log.i(TAG, "MQTT当前网络名称：" + name);
//            return true;
//        } else {
//            Log.i(TAG, "MQTT 没有可用网络");
//            return false;
//        }
//    }
}
