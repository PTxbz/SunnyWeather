package com.example.sunnyweather.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.example.sunnyweather.LogUtil
import com.example.sunnyweather.logic.dao.PlaceDao
import com.example.sunnyweather.logic.model.DailyResponse
import com.example.sunnyweather.logic.model.DailyResponse.Temperature
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.util.Date
import kotlin.coroutines.CoroutineContext


object Repository {

    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavedPlace() = PlaceDao.getSavedPlace()
    fun isPlaceSave() = PlaceDao.isPlacesSaved()

//    fun searchPlaces(query : String) = liveData(Dispatchers.IO) {
//        val result = try{
//            LogUtil.d("Debug", "Repository searchPlaces")
//            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
//            LogUtil.d("Debug", placeResponse.status)
//            if(placeResponse.status=="ok"){
//                val places = placeResponse.location
//                Result.success(places)
//            }else{
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//        }catch (e : Exception){
//            Result.failure<List<Place>>(e)
//        }
//        emit(result)
//    }
    fun searchPlaces(query : String) = fire(Dispatchers.IO){
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        LogUtil.d("Debug", placeResponse.status)
        if(placeResponse.status=="ok"){
            val places = placeResponse.location
            Result.success(places)
        }else{
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun buildDailyResponse(): DailyResponse {
        // 构建 Temperature 列表
        val temperatureList = listOf(
            DailyResponse.Temperature(5.6f, -2.15f),
            DailyResponse.Temperature(3.98f, -4.01f),
            DailyResponse.Temperature(3.95f, -5.4f)
        )

        // 构建 Skycon 列表
        val skyconList = listOf(
            DailyResponse.Skycon("CLEAR_DAY", parseDate("2025-12-29")),
            DailyResponse.Skycon("CLEAR_DAY", parseDate("2025-12-30")),
            DailyResponse.Skycon("PARTLY_CLOUDY_DAY", parseDate("2025-12-31"))
        )

        // 构建 LifeIndex 的各个部分
        val lifeIndex = DailyResponse.LifeIndex(
            coldRisk = listOf(
                DailyResponse.LifeDescription("极易发"),
                DailyResponse.LifeDescription("极易发"),
                DailyResponse.LifeDescription("极易发")
            ),
            carWashing = listOf(
                DailyResponse.LifeDescription("适宜"),
                DailyResponse.LifeDescription("适宜"),
                DailyResponse.LifeDescription("适宜")
            ),
            ultraviolet = listOf(
                DailyResponse.LifeDescription("强"),
                DailyResponse.LifeDescription("强"),
                DailyResponse.LifeDescription("强")
            ),
            dressing = listOf(
                DailyResponse.LifeDescription("寒冷"),
                DailyResponse.LifeDescription("极冷"),
                DailyResponse.LifeDescription("极冷")
            )
        )

        // 构建 Daily 对象
        val daily = DailyResponse.Daily(temperatureList, skyconList, lifeIndex)

        // 构建 Result 对象
        val result = DailyResponse.Result(daily)

        // 构建 DailyResponse 对象
        return DailyResponse(status = "ok", result = result)
    }

    // 日期字符串转换为 Date 对象
    fun parseDate(dateStr: String): Date {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd")
        return format.parse(dateStr)
    }


    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO){
        coroutineScope{
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
//            val deferredDaily = async {
//                SunnyWeatherNetwork.getDailyWeather(lng, lat)
//            }
            val realtimeResponse = deferredRealtime.await()
//            val dailyResponse = deferredDaily.await()
            val dailyResponse = buildDailyResponse()

            LogUtil.d("debug", "实时天气响应体: ${realtimeResponse.status}")
            LogUtil.d("debug", "每日天气响应体: ${dailyResponse.status}")

            if(realtimeResponse.status=="ok" && dailyResponse.status=="ok"){
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            }else{
                Result.failure(
                    RuntimeException("realtime response status is ${realtimeResponse.status}" +
                            "daily response status is ${dailyResponse.status}")
                )
            }
        }

//        val realtimeResponse = SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
//        val dailyResponse = buildDailyResponse()
//
//        LogUtil.d("debug", "实时天气响应体: ${realtimeResponse}")
//        LogUtil.d("debug", "每日天气响应体: ${dailyResponse}")
//
////        if(realtimeResponse.status=="ok" && dailyResponse.status=="ok"){
//        if(realtimeResponse.status=="ok"){
//            val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
//            Result.success(weather)
//        }else{
//            Result.failure(
//                RuntimeException("realtime response status is ${realtimeResponse.status}" +
//                        "daily response status is ${dailyResponse.status}")
//            )
//        }
    }
    private fun <T> fire(context : CoroutineContext, block:suspend ()-> Result<T>) =
        liveData<Result<T>>(context){
            val result = try {
                block()
            }catch (e: Exception){
                Result.failure<T>(e)
            }
            emit(result)
        }

}