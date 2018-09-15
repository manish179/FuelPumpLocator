package com.example.manish.fuelpumplocator;

import com.example.manish.fuelpumplocator.Remote.IGoogleAPIService;
import com.example.manish.fuelpumplocator.Remote.RetrofitClient;

import retrofit2.Retrofit;

public class Common {
    private static final String GOOGLE_API_URL ="https://maps.googleapis.com/";

    public static IGoogleAPIService getGoogleAPIService()
    {
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);

    }



}


