package com.example.manish.fuelpumplocator.Remote;

import com.example.manish.fuelpumplocator.Model.MyPlaces;
import com.example.manish.fuelpumplocator.Model.PlaceDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IGoogleAPIService {
    @GET
    Call<MyPlaces> getNearByPlaces(@Url String url);

    @GET
    Call<PlaceDetail> getDetailPlaces(@Url String url);

    @GET("maps/api/directions/json")
    Call<String> getDirections(@Query("origin") String origin, @Query("destination") String destination);
}
