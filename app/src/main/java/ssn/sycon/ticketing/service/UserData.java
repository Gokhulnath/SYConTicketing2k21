package ssn.sycon.ticketing.service;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

import retrofit2.http.Query;

public interface UserData {

    @GET("registration/getRegisteredUsers")
    public Call<String> user(@Header("Authorization") String authorization,
                             @Query("eventCode") String eventCode);
}
