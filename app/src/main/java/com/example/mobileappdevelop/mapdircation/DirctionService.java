package com.example.mobileappdevelop.mapdircation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Mobile App Develop on 1/17/2018.
 */

public interface DirctionService {
    @GET
    Call<DirectionRecponse> getDirection(@Url String url);
}
