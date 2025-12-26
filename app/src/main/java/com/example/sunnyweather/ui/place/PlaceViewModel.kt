package com.example.sunnyweather.ui.place

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.sunnyweather.LogUtil
import com.example.sunnyweather.logic.Repository
import com.example.sunnyweather.logic.model.Place

class PlaceViewModel : ViewModel() {
    private val searchLiveDate = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = searchLiveDate.switchMap {
        LogUtil.d("Debug", "searchLiveDate.switchMap")
            Repository.searchPlaces(it)
    }

    fun searchPlaces(query: String){
        LogUtil.d("Debug", "LiveDate is changed")
        searchLiveDate.value = query
    }
}