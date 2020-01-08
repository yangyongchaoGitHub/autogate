package com.dataexpo.autogate.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.dataexpo.autogate.comm.Utils;
import com.dataexpo.autogate.listener.MQTTObserver;
import com.dataexpo.autogate.listener.MQTTSubject;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

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
    Mqtt3AsyncClient client;
    //初始化线程
    private ConnectThread connectThread = null;

    //连接失败
    public static final int MQTT_CONNECT_INIT = 1;
    //正在连接
    public static final int MQTT_CONNECT_ING = 2;
    //连接成功
    public static final int MQTT_CONNECT_SUCCESS = 3;

    private int conn_status = MQTT_CONNECT_INIT;

    private MQTTHiveMQService(){};

    public void init(Context context) {
        mContext = context;

        if (connectThread == null) {
            connectThread = new ConnectThread();
            connectThread.start();
        }
    }

    private static class HolderClass {
        private static final MQTTHiveMQService instance = new MQTTHiveMQService();
    }

    /**
     * 单例模式
     */
    public static MQTTHiveMQService getInstance() {
        return MQTTHiveMQService.HolderClass.instance;
    }

    @Override
    public void notifyStatus(int status) {
        for(Object obs: observers) {
            ((MQTTObserver)obs).responseMQTTStatus(conn_status);
        }
    }

    public void restart() {

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

    @SuppressLint("NewApi")
    private void doClientConnection() {
        if (!initParameter() && isConnectIsNormal()) {
            conn_status = MQTT_CONNECT_ING;

            client = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(clientId)
                    .serverHost(host)
                    .serverPort(Integer.parseInt(port))
                    .addDisconnectedListener(listener -> {
                        Log.i(TAG, "connetcion lost!!!");
                        conn_status = MQTT_CONNECT_INIT;
                    })
                    .buildAsync();

            client.connectWith()
                    .simpleAuth()
                    .username(name)
                    .password(pswd.getBytes())
                    .applySimpleAuth()
                    .send()
                    .thenCompose(v -> client.disconnect())
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            // handle failure
                            Log.i(TAG, "连接失败 ");
                            if (client != null) {
                                client = null;
                            }
                            conn_status = MQTT_CONNECT_INIT;

                        } else {
                            if (conn_status != MQTT_CONNECT_SUCCESS) {
                                conn_status = MQTT_CONNECT_SUCCESS;
                                Log.i(TAG, "连接成功 ");
                                subscribe();
                            }
                            // setup subscribes or start publishing
                        }
                    });
        }
    }

    @SuppressLint("NewApi")
    private void subscribe() {
        client.subscribeWith()
                .topicFilter("topic9")
                .callback(publish -> {
                    // Process the received message
                    byte[] payload = publish.getPayloadAsBytes();
                    Log.i(TAG, "payload: " + new String(payload));

                    Thread thread = Thread.currentThread();
                    Log.i(TAG, "收到消息 thread name " + thread.getName());

                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.i(TAG, "监听主题失败 " );
                        // Handle failure to subscribe
                    } else {
                        Log.i(TAG, "监听主题成功 ");
                        // Handle successful subscription, e.g. logging or incrementing a metric
                    }
                });
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
}
