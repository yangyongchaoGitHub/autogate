package com.dataexpo.autogate.retrofitInf;

import com.dataexpo.autogate.model.service.Device;
import com.dataexpo.autogate.model.service.PageResult;
import com.dataexpo.autogate.model.service.UserEntityVo;
import com.dataexpo.autogate.model.service.UserQueryConditionVo;
import com.dataexpo.autogate.retrofitInf.rentity.NetResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import static com.dataexpo.autogate.retrofitInf.URLs.*;

public interface ApiService {

    @GET(tiktakUrl) //网络请求路径
    Call<NetResult<String>> tiktak();

    @POST(saveDeviceUrl)
    Call<NetResult<String>> saveDeviceConfig(@Body Device device);

    @POST(queryUserUrl)
    Call<NetResult<PageResult<UserEntityVo>>> querySyncUser(@Body UserQueryConditionVo vo);
}
