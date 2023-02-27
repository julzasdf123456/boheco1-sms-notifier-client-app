package com.lopez.julz.textrequest.api;

import com.lopez.julz.textrequest.SMSNotifications;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RequestPlaceHolder {
    @GET("get-random-notification")
    Call<SMSNotifications> getRandom();

    @GET("update-sms")
    Call<Void> updateSMS(@Query("id") String id);
}
