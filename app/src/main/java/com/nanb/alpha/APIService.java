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
            "Authorization:key=AAAADk7ZBhk:APA91bFBV3YjbAe8GBXqf8YTgoi-6psKkmpgkBitxFQdYtWKB4rk2q7lP3xmrimBfIQ_fUk7TSDxG-qkvi5A2LbL9ZPgx-DNx8l-jVWH0j7zZwDMVzChxEPJOwyAnoBvom7jGwQss04A"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
