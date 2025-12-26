package com.example.sunnyweather.logic.network

import android.util.Log
import com.example.sunnyweather.LogUtil
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {

    private val placeService = ServiceCreator.create<PlaceService>()

    suspend fun searchPlaces(query : String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await() : T{
        return suspendCoroutine { continuation ->
            enqueue( object : retrofit2.Callback<T>{
                override fun onResponse(
                    call: Call<T?>,
                    response: Response<T?>
                ) {
                    val body = response.body()
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
