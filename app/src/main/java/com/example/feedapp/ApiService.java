package com.example.feedapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("hiring.json")
    Call<List<Item>> getItems();
}
