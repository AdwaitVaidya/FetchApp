package com.example.feedapp;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APICalls {
        @GET("hiring.json")
                Call<List<Item>> getItems();





}
