package com.example.prueba

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import com.example.prueba.network.RetrofitInstance
import com.example.prueba.network.SocketManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.emitter.Emitter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private val socket = SocketManager.getSocket()
    private lateinit var musicListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        socket.connect()
        socket.on("musicList", onMusic)

        musicListContainer = findViewById(R.id.musicListContainer)
        fetchMusic()
    }

    // Listener para las actualizaciones de música
    private val onMusic = Emitter.Listener { args ->
        // Asegurarse de que el primer argumento sea un JSONArray
        if (args.isNotEmpty() && args[0] is JSONArray) {
            val jsonArray = args[0] as JSONArray
            val musicList: List<Music> = Gson().fromJson(jsonArray.toString(), object : TypeToken<List<Music>>() {}.type)
            runOnUiThread { updateMusicList(musicList) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()  // Desconectar el socket directamente
    }

    // Obtener los datos de música desde la API
    private fun fetchMusic() {
        RetrofitInstance.api.getAllMusic().enqueue(object : Callback<List<Music>> {
            override fun onResponse(call: Call<List<Music>>, response: Response<List<Music>>) {
                val musicList = response.body() ?: emptyList()
                socket.emit("musicList", Gson().toJson(musicList))  // Emitir la lista
                runOnUiThread { updateMusicList(musicList) }
            }
            override fun onFailure(call: Call<List<Music>>, t: Throwable) {
                runOnUiThread { updateMusicList(emptyList()) }
            }
        })
    }

    // Actualizar la UI con los datos de música
    private fun updateMusicList(musicList: List<Music>) {
        musicListContainer.removeAllViews()
        musicList.forEach { music ->
            LayoutInflater.from(this).inflate(R.layout.item_layout, musicListContainer, false).apply {
                findViewById<TextView>(R.id.titleText).text = music.title
                findViewById<TextView>(R.id.artistText).text = "Artist: ${music.artist}"
                findViewById<TextView>(R.id.albumText).text = "Album: ${music.album}"
                musicListContainer.addView(this)
            }
        }
    }
}