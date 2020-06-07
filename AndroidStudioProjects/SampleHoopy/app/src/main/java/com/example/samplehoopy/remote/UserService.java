package com.example.samplehoopy.remote;

import com.example.samplehoopy.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {
    @POST("fetch_data_test")
    Call<List<User>> getUsers(@Body User user);

    @POST("insert_test")
    Call<User> addUser(@Body User user);

    @PUT("update/{id}")
    Call<User> updateUser(@Path("id") int id, @Body User user);

    @DELETE("delete/{id}")
    Call<User> deleteUser(@Path("id") int id);
}
