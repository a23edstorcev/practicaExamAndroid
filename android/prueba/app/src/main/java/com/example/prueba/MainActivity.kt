package com.example.prueba

// Importaciones necesarias para trabajar con Android, Retrofit y LiveData
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.prueba.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// La clase MainActivity hereda de ComponentActivity, que es una actividad base más ligera, ideal para trabajar con Jetpack Compose y UI moderna.
class MainActivity : ComponentActivity() {

    // Variable para contener el LinearLayout donde se mostrará la lista de canciones
    private lateinit var musicListContainer: LinearLayout

    // Este método se llama cuando se crea la actividad. Es el lugar donde inicializamos la UI y configuramos todo.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecemos el layout de la actividad (el archivo activity_main.xml) como la vista principal
        setContentView(R.layout.activity_main)

        // Inicializamos el contenedor de la lista (un LinearLayout en el XML)
        musicListContainer = findViewById(R.id.musicListContainer)

        // Inicializamos el ViewModel usando ViewModelProvider. El ViewModel es responsable de manejar los datos de la UI.
        val musicViewModel = ViewModelProvider(this).get(MusicViewModel::class.java)

        // Aquí observamos el LiveData del ViewModel, es decir, escuchamos cualquier cambio en la lista de música
        musicViewModel.musicList.observe(this) { musicList ->
            // Cada vez que los datos cambian, llamamos a esta función para actualizar la UI con la nueva lista
            updateMusicList(musicList)
        }

        // Iniciamos la carga de canciones llamando al método fetchMusic() del ViewModel
        musicViewModel.fetchMusic()
    }

    // Esta función recibe una lista de música y actualiza la UI (el LinearLayout) con esos datos
    private fun updateMusicList(musicList: List<Music>) {
        // Limpiamos el contenedor antes de agregar nuevos elementos
        musicListContainer.removeAllViews()

        // Iteramos sobre cada canción en la lista
        for (music in musicList) {
            // Inflamos el layout (esto crea la vista de cada ítem) a partir de un archivo XML
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_layout, musicListContainer, false)

            // Obtenemos las referencias a los TextViews en el layout inflado para actualizar su contenido
            val titleText: TextView = itemView.findViewById(R.id.titleText)
            val artistText: TextView = itemView.findViewById(R.id.artistText)
            val albumText: TextView = itemView.findViewById(R.id.albumText)

            // Asignamos los valores de la canción a los TextViews
            titleText.text = music.title
            artistText.text = "Artist: ${music.artist}"
            albumText.text = "Album: ${music.album}"

            // Agregamos este ítem al contenedor de la lista (es decir, lo mostramos en la pantalla)
            musicListContainer.addView(itemView)
        }
    }
}

// El ViewModel es responsable de manejar los datos de la UI y de mantener esos datos durante los cambios de configuración (como la rotación de pantalla).
class MusicViewModel : ViewModel() {

    // MutableLiveData para la lista de canciones. Esta variable se usa para cambiar y observar los datos.
    private val _musicList = MutableLiveData<List<Music>>(emptyList())

    // LiveData solo de lectura. Esto es lo que la actividad observará para detectar cambios.
    val musicList: LiveData<List<Music>> = _musicList

    // Método para obtener la lista de canciones desde la API
    fun fetchMusic() {
        // Usamos Retrofit para hacer una llamada a la API que devuelve una lista de música
        RetrofitInstance.api.getAllMusic().enqueue(object : Callback<List<Music>> {
            override fun onResponse(call: Call<List<Music>>, response: Response<List<Music>>) {
                // Si la respuesta es exitosa (código 200), actualizamos el LiveData con la lista de canciones
                if (response.isSuccessful) {
                    // Actualizamos la lista de música con los datos obtenidos
                    _musicList.value = response.body() ?: emptyList()
                } else {
                    // Si hubo un error en la respuesta, establecemos la lista como vacía
                    _musicList.value = emptyList()
                }
            }

            override fun onFailure(call: Call<List<Music>>, t: Throwable) {
                // Si hubo un error en la llamada (como un problema de red), también establecemos la lista vacía
                _musicList.value = emptyList()
            }
        })
    }
}
