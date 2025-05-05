package com.example.solidarityhub.android.inicio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.afectados.RegistrarAfectadosActivity
import com.example.solidarityhub.android.voluntarios.RegistrarVoluntariosActivity
import com.google.android.material.navigation.NavigationView
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.remote.AfectadoApiService
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
private lateinit var googleMap: GoogleMap
private val afectados = mutableListOf<AfectadoADTO>()
private lateinit var apiService: AfectadoApiService


class Menu_activity :AppCompatActivity(), OnMapReadyCallback {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnHamburguesa: MaterialButton
    private lateinit var btnPerfil: MaterialButton
    private lateinit var btnMensajes: MaterialButton
    private lateinit var btnRegistrarVoluntario: MaterialButton
    private lateinit var btnRegistrarAfectado: MaterialButton
    private lateinit var btnAjustes: MaterialButton
    private lateinit var btnCerrarSesion: MaterialButton

    private lateinit var googleMap: GoogleMap
    private lateinit var apiService: AfectadoApiService
    private val afectados = mutableListOf<AfectadoADTO>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        drawerLayout = findViewById(R.id.drawerLayout)
        btnHamburguesa = findViewById(R.id.btn_menu)
        btnPerfil = findViewById(R.id.btnPerfil)
        btnMensajes = findViewById(R.id.btnMensajes)
        btnRegistrarVoluntario = findViewById(R.id.btnRegistrarVoluntario)
        btnRegistrarAfectado = findViewById(R.id.btnRegistrarAfectado)
        btnAjustes = findViewById(R.id.btnAjustes)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        apiService = RetrofitClient.afectadoApiService

        // Obtener lista inicial de afectados del intent (si existe)
        intent.getParcelableArrayListExtra<AfectadoADTO>("afectados")?.let {
            afectados.addAll(it)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<FloatingActionButton>(R.id.fab_center).setOnClickListener {
            centrarMapaEnAfectados()
        }

        btnHamburguesa.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        /*
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnMensajes.setOnClickListener {
            startActivity(Intent(this, MensajesActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnAjustes.setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnCerrarSesion.setOnClickListener {
            startActivity(Intent(this, CerrarSesionActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        */


        btnRegistrarVoluntario.setOnClickListener {
            startActivity(Intent(this, RegistrarVoluntariosActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        btnRegistrarAfectado.setOnClickListener {
            startActivity(Intent(this, RegistrarAfectadosActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        if (afectados.isNotEmpty()) {
            agregarMarcadores(afectados)
        }

        cargarAfectadosDesdeAPI()
        centrarMapaEnAfectados()
    }

    private fun cargarAfectadosDesdeAPI() {
        lifecycleScope.launch {
            try {


                val nuevosAfectados = withContext(Dispatchers.IO) {
                    apiService.getAllAfectados()
                }

                // Actualizar UI en el hilo principal
                afectados.clear()
                afectados.addAll(nuevosAfectados)

                agregarMarcadores(nuevosAfectados)
                centrarMapaEnAfectados()

            } catch (e: Exception) {
                // Manejo de errores
                Log.e("API", "Error al cargar afectados", e)

            } finally {
                // Ocultar progreso

            }
        }
    }

    private fun agregarMarcadores(afectados: List<AfectadoADTO>) {
        afectados.forEach { afectado ->
            val color = BitmapDescriptorFactory.HUE_RED

            val markerOptions = MarkerOptions()
                .position(LatLng(afectado.latitud, afectado.longitud))
                .title(afectado.nombre)
                .snippet("Estado: ${afectado.necesidades}")
                .icon(BitmapDescriptorFactory.defaultMarker(color))

            googleMap.addMarker(markerOptions)
        }
    }

    private fun centrarMapaEnAfectados() {
        if (afectados.isEmpty()) return

        val builder = LatLngBounds.Builder()
        afectados.forEach {
            builder.include(LatLng(it.latitud, it.longitud))
        }

        val bounds = builder.build()
        val padding = 100
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        try {
            googleMap.animateCamera(cu)
        } catch (e: IllegalStateException) {
            Handler(Looper.getMainLooper()).postDelayed({
                googleMap.animateCamera(cu)
            }, 500)
        }
    }
}
