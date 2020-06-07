package com.example.samplehoopy.remote;

public class APIUtils {
    private APIUtils(){
    };

    public static final String API_URL = " https://www.team.hoopy.in/api/1.0/testApis/";

    public static UserService getUserService(){
        return RetrofitClient.getClient(API_URL).create(UserService.class);
    }
}
