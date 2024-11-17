package com.example.prueba.network

import com.example.prueba.Music
import retrofit2.Call
import retrofit2.http.GET

interface MusicApi {
    @GET("/music")
    fun getAllMusic(): Call<List<Music>>
}
