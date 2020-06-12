package com.example.uploadwithphp.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitCient {
    private static Retrofit retrofitCient = null;

    public static Retrofit getClient(String baseUrl)
    {
        if(retrofitCient == null)
        {
             retrofitCient = new Retrofit.Builder()
                     .baseUrl(baseUrl)
                     .addConverterFactory(ScalarsConverterFactory.create())
                     .build();
        }
        return retrofitCient;
    }
}
