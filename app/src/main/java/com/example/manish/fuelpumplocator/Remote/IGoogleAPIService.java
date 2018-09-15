package com.example.manish.fuelpumplocator.Remote;

import com.example.manish.fuelpumplocator.Model.MyPlaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleAPIService {
    @GET
    Call<MyPlaces> getNearByPlaces(@Url String url);
}