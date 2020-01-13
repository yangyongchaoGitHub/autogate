package com.dataexpo.autogate.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dataexpo.autogate.comm.JsonUtil;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.IGetMessageCallBack;

import com.dataexpo.autogate.listener.MQTTObserver;
import com.dataexpo.autogate.listener.MQTTSubject;
import com.dataexpo.autogate.model.User;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.dataexpo.autogate.comm.Utils.*;

public class MQTTService extends MQTTSubject {
    private static final String TAG = MQTTService.class.getSimpleName();
    private Context mContext;
    private String host = "";
    private String port = "";
    private String name = "";
    private String pswd = "";
    private static String topic = "";
    private String clientId = "";
    private boolean bRun = true;

    //连接失败
    public static final int MQTT_CONNECT_INIT = 1;
    //正在连接
    public static final int MQTT_CONNECT_ING = 2;
    //连接成功
    public static final int MQTT_CONNECT_SUCCESS = 3;

    private int conn_status = MQTT_CONNECT_INIT;

    private static MqttAndroidClient client = null;
    private MqttConnectOptions options;
    private IGetMessageCallBack iGetMessageCallBack;
    private ConnectThread connectThread = null;
    private ConsumerThread consumerThread = null;
    private IMqttToken token = null;

    //线程池 5个线程同时进行解析
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    //异步处理消息的消息体队列
    private List<User> messages = new ArrayList<>();

    private static final int threadCount = 5;
    private volatile int num = 0;
    private User[] currUsers = new User[threadCount];
    private final Object syncThreadUser = new User();

    private MQTTService (){};

    public void destroy() {
        Log.i(TAG, "destroy");
        try {
            if (client != null) {
                client.disconnect();
                client = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private synchronized int getThreadNumber() {
        return num++;
    }

    public void exit() {
        destroy();
        bRun = false;
    }

    public void restart() {
        if (conn_status == MQTT_CONNECT_SUCCESS) {
            destroy();
        }
    }

    @Override
    public void notifyStatus(int status) {
        for(Object obs: observers) {
            ((MQTTObserver)obs).responseMQTTStatus(conn_status);
        }
    }

    private static class HolderClass {
        private static final MQTTService instance = new MQTTService();
    }

    /**
     * 单例模式
     */
    public static MQTTService getInstance() {
        return MQTTService.HolderClass.instance;
    }

    public void init(Context context) {
        for (int i = 0; i < threadCount; i++) {
            currUsers[i] = null;
        }
        mContext = context;
        options = new MqttConnectOptions();

//        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
//        Log.e(TAG, "遗嘱 是:" + message);
//
//        //设置的是遗嘱的qos 和 retained
//        Integer qos = 2;
//        Boolean retained = true;
//
//        if ((!message.equals("")) || (!topic.equals(""))) {
//            // 最后的遗嘱
//            // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
//            //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
//            //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。
//
//            try {
//                options.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
//            } catch (Exception e) {
//                Log.i(TAG, "Exception Occured", e);
//                doConnect = false;
//                if (iMqttActionListener != null) {
//                    iMqttActionListener.onFailure(null, e);
//                }
//            }
//        }
        if (connectThread == null) {
            connectThread = new ConnectThread();
            connectThread.start();
        }

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(new ConsumerThread());
        }
    }

    /** 连接MQTT服务器 */
    private void doClientConnection() {
        if (!initParameter() && isConnectIsNormal()) {
            try {
                conn_status = MQTT_CONNECT_ING;
                options.setCleanSession(false);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(20);
                options.setUserName(name);
                options.setPassword(pswd.toCharArray());

                client = new MqttAndroidClient(mContext, host + ":" + port, clientId);
                client.setCallback(mqttCallback);

                token = client.connect(options, null, iMqttActionListener);

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean initParameter() {
        host = Utils.getMQTTConfig(mContext, MQTT_HOST);
        port = Utils.getMQTTConfig(mContext, MQTT_PORT);
        name = Utils.getMQTTConfig(mContext, MQTT_NAME);
        pswd = Utils.getMQTTConfig(mContext, MQTT_PSWD);
        clientId = Utils.getMQTTConfig(mContext, MQTT_CLIENTID);
        topic = Utils.getMQTTConfig(mContext, MQTT_TOPIC);
        return "".equals(host) || "".equals(port) || "".equals(name) ||
                "".equals(pswd) || "".equals(clientId) || "".equals(topic);
    }

    /** 判断网络是否连接 */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken arg0) {
            if (conn_status != MQTT_CONNECT_SUCCESS) {
                conn_status = MQTT_CONNECT_SUCCESS;
                Log.i(TAG, "连接成功 将订阅" + topic);
                try {
                    // 订阅myTopic话题
                    token = client.subscribe(topic, 2);

                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            //Log.i(TAG, "onFailure " + arg1.getMessage());
            //TODO: if client not set will set repeat
            if (client != null) {
                client.setCallback(null);
                client = null;
            }
            conn_status = MQTT_CONNECT_INIT;
            // 连接失败，重连
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) {
            //此处因为设置的回调实在主线程，所以所有的耗时操作都需要放到子线程,先将数据暂存
            if (MQTTService.topic.equals(topic)) {
                boolean bExist = false;
                User user = null;
                try {
                    user = JsonUtil.getInstance().json2obj(new String(message.getPayload()), User.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (user == null) {
                    return;
                }

                synchronized (MQTTService.class) {
                    //将待处理数据放入messages
                    // 对待的情况是 broker同时发送了多条重复的消息，若是都放到messages，则有可能导致内存溢出
                    for (int i = messages.size() - 1; (i > 0 && i > messages.size() - threadCount); i--) {
                        if (user.code.equals(messages.get(i).code)) {
                            bExist = true;
                            break;
                        }
                    }
                    if (!bExist) {
                        Log.i(TAG, "messageArrived 1 " + (new Date()).getTime());
                        messages.add(user);
                    }
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
            Log.i(TAG, "connectionLost!!!!!!!!!!!!! ");
            if (arg0 != null) {
                Log.i(TAG, Objects.requireNonNull(arg0.getMessage()));
            }
            conn_status = MQTT_CONNECT_INIT;
        }
    };

    public void setIGetMessageCallBack(IGetMessageCallBack iGetMessageCallBack){
        this.iGetMessageCallBack = iGetMessageCallBack;
    }

    private class ConnectThread extends Thread {
        @Override
        public void run() {
            int delay = 1000;
            while (bRun) {
                if (conn_status == MQTT_CONNECT_INIT) {
                    Thread th=Thread.currentThread();
                    System.out.println("ConnectThread main is " + th.getName());
                    doClientConnection();
                }

                notifyStatus(conn_status);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ConsumerThread implements Runnable {
        @Override
        public void run() {
            User user = null;
            int number = getThreadNumber();
            Log.i(TAG, "my number is " + number);
            boolean bInOther = false;
            while (bRun) {
                synchronized (MQTTService.class) {
                    if (messages.size() > 0) {
                        Log.i(TAG, "messages wait " + messages.size());
                        user = messages.get(0);
                        messages.remove(0);
                    }
                }

                if (user == null) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                try {
//                    if (iGetMessageCallBack != null) {
//                        iGetMessageCallBack.setMessage(payload);
//                    }
                    //TestData data = JsonUtil.getInstance().json2obj(str1, TestData.class);

                    //String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
                    //Log.i(TAG, " topic：" + topic + " " + payload);
                    //Log.i(TAG, str2);

                    Log.i(TAG, "ConsumerThread insert che code number " + number + " " + user.code + " " + (new Date()).getTime());
                    //检测是否在数据库有重复用户并告知正在处理此code的id给其他线程
                    if (UserService.getInstance().haveByCode(user)) {
                        Log.i(TAG, "用户： " + user.code + " 重复！！！！！");
                        continue;
                    }
                    //告知正在处理此code的id给其他线程
                    bInOther = false;
                    synchronized (syncThreadUser) {
                        for (int i = 0; i < threadCount; i++) {
                            if (currUsers[i] != null && user.code.equals(currUsers[i].code)) {
                                bInOther = true;
                                break;
                            }
                        }
                    }

                    if (bInOther) {
                        continue;
                    }

                    synchronized (syncThreadUser) {
                        currUsers[number] = user;
                    }

                    Log.i(TAG, "ConsumerThread insert sta code number " + number + " " + user.code + " " + (new Date()).getTime());
                    //Log.i(TAG, "user name " + user.name + " cardCode " + user.cardCode + " image:" + user.image);
                    UserService.getInstance().insert(user);

//            Log.i(TAG, String.valueOf(Environment.getExternalStorageDirectory()));
//            File file = FileUtils.isFileExist(String.valueOf(Environment.getExternalStorageDirectory()), "etstpg");
//            Log.i(TAG, "file: " + file);
//            String sss = FileUtils.toBase64(file);
//            Log.i(TAG, "file base64: " + sss);
                    //FileUtils.writeTxtFile(sss, String.valueOf(Environment.getExternalStorageDirectory()) + "/testbase64");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    user = null;
                }
                Log.i(TAG, "ConsumerThread insert end " + (new Date()).getTime());
            }
        }
    }
}
