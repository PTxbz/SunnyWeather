package com.example.sunnyweather.logic.dao

import android.content.Context
import com.example.sunnyweather.SunnyWeatherApplication
import com.example.sunnyweather.logic.model.Place
import com.google.gson.Gson

object PlaceDao {
    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather",
        Context.MODE_PRIVATE)

    fun isPlacesSaved() = sharedPreferences().contains("place")

    fun getSavedPlace() : Place{
        val placeJson = sharedPreferences().getString("place","")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun savePlace(place : Place){
        val edit = sharedPreferences().edit()
        edit.putString("place", Gson().toJson(place))
        edit.apply()
    }
}