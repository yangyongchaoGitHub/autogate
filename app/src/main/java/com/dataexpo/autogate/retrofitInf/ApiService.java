package com.dataexpo.autogate.retrofitInf;

import com.dataexpo.autogate.retrofitInf.rentity.NetResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import static com.dataexpo.autogate.retrofitInf.URLs.*;

public interface ApiService {

    @GET(tiktakUrl) //网络请求路径
    Call<NetResult<String>> tiktak();


}
