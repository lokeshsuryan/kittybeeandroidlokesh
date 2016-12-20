package com.kittyapplication.rest;

import com.kittyapplication.utils.AppConstant;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServiceGenerator {
    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    /**
     * @param serviceClass
     * @param <S>
     * @return
     */
    public static <S> S createService(Class<S> serviceClass) {

        // set your desired log level
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.authenticator(new TokenAuthenticator());
        builder.addInterceptor(httpLoggingInterceptor);
        builder.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .build();
                return chain.proceed(newRequest);

            }
        });


        builder.readTimeout(AppConstant.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).
                connectTimeout(AppConstant.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        // Set the custom client when building adapter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        return retrofit.create(serviceClass);
    }

    public static <S> S createAuthenticated(Class<S> serviceClass) {

        // set your desired log level
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(httpLoggingInterceptor);
        builder.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
//                        .addHeader("Authorization", PreferanceUtils.getUserToken(SchoolApplication.getContext()))
                        .build();
                return chain.proceed(newRequest);

            }
        });


        builder.readTimeout(AppConstant.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).
                connectTimeout(AppConstant.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

        // Set the custom client when building adapter
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        return retrofit.create(serviceClass);
    }
}
