package com.dataexpo.autogate.model.service;

import java.io.Serializable;

public class DeviceQueryResult implements Serializable {
    public  Integer errcode;

    public String errmsg;

    public Device data;
}
