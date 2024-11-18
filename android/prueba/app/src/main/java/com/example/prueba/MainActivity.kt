package com.example.prueba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prueba.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Clase principal de la actividad
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuramos Jetpack Compose como el contenido de la actividad
        setContent {
            MusicApp()
        }
    }
}

// Función raíz de la aplicación
@Composable
fun MusicApp() {
    // Obtenemos el ViewModel usando la función de Compose `viewModel()`
    val musicViewModel: MusicViewModel = viewModel()

    // Observamos el estado de la lista de canciones desde el ViewModel
    val musicList by musicViewModel.musicList.collectAsState(initial = emptyList())

    // Llamamos a `fetchMusic` al primer inicio de la composición para cargar los datos
    LaunchedEffect(Unit) {
        musicViewModel.fetchMusic()
    }

    // Tema de Material Design
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Music List",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Mostramos la lista de música
                MusicList(musicList)
            }
        }
    }
}

// Función que muestra la lista de canciones usando LazyColumn
@Composable
fun MusicList(musicList: List<Music>) {
    // Usamos LazyColumn para mostrar las canciones de forma eficiente
    LazyColumn {
        items(musicList) { music ->
            MusicItem(music)
        }
    }
}

// Función para mostrar los datos de cada canción
@Composable
fun MusicItem(music: Music) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = music.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Artist: ${music.artist}")
            Text(text = "Album: ${music.album}")
        }
    }
}

// ViewModel para gestionar los datos de música
class MusicViewModel : ViewModel() {

    // MutableStateFlow para la lista de canciones. Esto reemplaza LiveData en Compose
    private val _musicList = MutableStateFlow<List<Music>>(emptyList())
    val musicList: StateFlow<List<Music>> = _musicList

    // Método para obtener la lista de canciones desde la API
    fun fetchMusic() {
        RetrofitInstance.api.getAllMusic().enqueue(object : Callback<List<Music>> {
            override fun onResponse(call: Call<List<Music>>, response: Response<List<Music>>) {
                if (response.isSuccessful) {
                    // Si la respuesta es exitosa, actualizamos el estado con los datos de la música
                    _musicList.value = response.body() ?: emptyList()
                } else {
                    // Si hubo un error en la respuesta, dejamos la lista vacía
                    _musicList.value = emptyList()
                }
            }

            override fun onFailure(call: Call<List<Music>>, t: Throwable) {
                // En caso de un fallo de red, también dejamos la lista vacía
                _musicList.value = emptyList()
            }
        })
    }
}
