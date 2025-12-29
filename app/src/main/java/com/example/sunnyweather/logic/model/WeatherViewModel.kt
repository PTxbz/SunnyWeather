package com.example.sunnyweather.logic.model

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.sunnyweather.LogUtil
import com.example.sunnyweather.logic.Repository

class WeatherViewModel: ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()

    var locationLng = ""
    var locationLat = ""
    var placeName = ""

    val weatherLiveData = locationLiveData.switchMap {
        Repository.refreshWeather(it.lng, it.lat)
    }

    fun refreshWeather(lng: String, lat: String){
        LogUtil.d("debug","refresh Weather lng=${lng}, lat=${lat}")
        locationLiveData.value = Location(lng, lat)
    }
}