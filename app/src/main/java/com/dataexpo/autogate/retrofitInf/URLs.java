package com.dataexpo.autogate.retrofitInf;

public class URLs {
    public static final String baseUrl = "http://192.168.1.17:8080/Doors/";

    private static final String postfix = ".do";

    // 登录
    public static final String tiktakUrl = "pda/tiktak" + postfix;

    // 保存当前设备配置
    public static final String saveDeviceUrl = "pda/device/save" + postfix;

    //查询同步用户数据
    public static final String queryUserUrl = "pda/user/queryByCondition" + postfix;

    //查询同步用户人像
    public static final String queryUserImageUrl = "upload/{eucode}";
}
