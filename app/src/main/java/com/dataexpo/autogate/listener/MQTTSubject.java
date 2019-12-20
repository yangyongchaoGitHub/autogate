package com.dataexpo.autogate.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class MQTTSubject {
    //观察者列表
    protected List<MQTTObserver> observers =  new ArrayList<>();

    //增加观察者的方法
    public void add(MQTTObserver observer)
    {
        observers.add(observer);
    }

    //删除观察者的方法
    public void remove(MQTTObserver observer)
    {
        observers.remove(observer);
    }

    //通知观察者状态有变化
    public abstract void notifyStatus(int status);
}
