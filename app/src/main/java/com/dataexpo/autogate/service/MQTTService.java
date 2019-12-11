package com.dataexpo.autogate.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dataexpo.autogate.comm.JsonUtil;
import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.IGetMessageCallBack;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.dataexpo.autogate.model.User;

import static com.dataexpo.autogate.comm.Utils.*;

public class MQTTService {
    private static final String TAG = MQTTService.class.getSimpleName();
    private Context mContext;
    private String host = "";
    private String port = "";
    private String name = "";
    private String pswd = "";
    private String topic = "";
    private String clientId = "";


    private static final int MQTT_CONNECT_INIT = 1;
    private static final int MQTT_CONNECT_ING = 2;
    private static final int MQTT_CONNECT_SUCCESS = 3;
    private int conn_status = MQTT_CONNECT_INIT;

    private static MqttAndroidClient client = null;
    private MqttConnectOptions options;
    private IGetMessageCallBack iGetMessageCallBack;
    private ConnectThread connectThread = null;
    private IMqttToken token = null;

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

    public void restart() {
        if (conn_status == MQTT_CONNECT_SUCCESS) {
            destroy();
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
            conn_status = MQTT_CONNECT_SUCCESS;
            Log.i(TAG, "连接成功 将订阅" + topic);
            try {
                // 订阅myTopic话题
                token = client.subscribe(topic,2);

                if (token != null) {
                    for (String s:token.getTopics()) {
                        Log.i(TAG, "topics: " + s);
                    }
                }

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            Log.i(TAG, "onFailure " + arg1.getMessage());
            client = null;
            conn_status = MQTT_CONNECT_INIT;
            // 连接失败，重连
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) {
            try {
                String str1 = new String(message.getPayload());

                Log.i(TAG, "messageArrived!!!!!!!!! topic is " + topic +
                        " : " + str1);

                if (iGetMessageCallBack != null) {
                    iGetMessageCallBack.setMessage(str1);
                }
                User user = JsonUtil.getInstance().json2obj(str1, User.class);
                //TestData data = JsonUtil.getInstance().json2obj(str1, TestData.class);

                String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
                Log.i(TAG, "messageArrived:" + str1);
                Log.i(TAG, str2);
                Log.i(TAG, "data: " + user.code);
                //Log.i(TAG, "user name " + user.name + " cardCode " + user.cardCode + " image:" + user.image);
                // UserService.getInstance().insert(user);
                UserService.getInstance().insert(user);



//            Log.i(TAG, String.valueOf(Environment.getExternalStorageDirectory()));
//            File file = FileUtils.isFileExist(String.valueOf(Environment.getExternalStorageDirectory()), "etstpg");
//            Log.i(TAG, "file: " + file);
//            String sss = FileUtils.toBase64(file);
//            Log.i(TAG, "file base64: " + sss);
                //FileUtils.writeTxtFile(sss, String.valueOf(Environment.getExternalStorageDirectory()) + "/testbase64");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
            Log.i(TAG, "connectionLost!!!!!!!!!!!!! ");
            conn_status = MQTT_CONNECT_INIT;
        }
    };

    public void setIGetMessageCallBack(IGetMessageCallBack iGetMessageCallBack){
        this.iGetMessageCallBack = iGetMessageCallBack;
    }

    private class ConnectThread extends Thread {
        @Override
        public void run() {
            while (true) {
                Log.i(TAG, "thread status:" + conn_status);
                if (conn_status == MQTT_CONNECT_INIT) {
                    doClientConnection();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
