package com.example.androidtutorial.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object FlightPlanClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: FlightPlanService = retrofit.create(FlightPlanService::class.java)

}