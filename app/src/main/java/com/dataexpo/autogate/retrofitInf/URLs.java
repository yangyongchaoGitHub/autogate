package com.dataexpo.autogate.retrofitInf;

public class URLs {
    public static final String baseUrl = "http://192.168.1.14:8080/Doors/pda/";

    private static final String postfix = ".do";

    // 登录
    public static final String tiktakUrl = "tiktak" + postfix;

    // 保存当前设备配置
    public static final String saveDeviceUrl = "device/save" + postfix;

    //查询同步用户数据
    public static final String queryUserUrl = "user/queryByCondition" + postfix;
}
