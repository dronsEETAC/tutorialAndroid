package com.example.androidtutorial.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.ResponseBody

interface FlightPlanService {
    @GET("data")
    fun getData(): Call<List<FlightPlan>>

    @POST("data")
    fun postFlightPlan(@Body flightPlan: FlightPlan): Call<ResponseBody>
}