package com.example.sunnyweather.logic.network

import android.util.Log
import com.example.sunnyweather.LogUtil
import retrofit2.Call
import retrofit2.Response
import retrofit2.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {

    private val placeService = ServiceCreator.create<PlaceService>()
    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun searchPlaces(query : String) = placeService.searchPlaces(query).await()

    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()
    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()
    private suspend fun <T> Call<T>.await() : T{
        return suspendCoroutine { continuation ->
            enqueue( object : retrofit2.Callback<T>{
                override fun onResponse(
                    call: Call<T?>,
                    response: Response<T?>
                ) {
                    LogUtil.d("debug", "r=${response}")
                    val body = response.body()
                    LogUtil.d("debug", "r.body=${body}")
                    if(body!=null){
                        continuation.resume(body)
                    }
                    else continuation.resumeWithException(
                        RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T?>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            }
            )
        }
    }
}
