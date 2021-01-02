package com.nanb.alpha;

import com.nanb.alpha.Notification.MyResponse;
import com.nanb.alpha.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=API KEY"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
