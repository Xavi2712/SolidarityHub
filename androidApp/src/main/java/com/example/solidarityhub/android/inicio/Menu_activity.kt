package com.example.solidarityhub.android.inicio

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.databinding.ActivityMenuBinding
import com.example.solidarityhub.android.util.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent
import com.example.solidarityhub.android.Necesidad.AñadirNecesidadActivity
import com.example.solidarityhub.android.afectados.RegistrarAfectadosActivity
import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.remote.AfectadoApiService
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.ui.login.LoginActivity
import com.example.solidarityhub.android.data.voluntarios.RegistrarVoluntariosActivity

class Menu_activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var mMap: GoogleMap
    private lateinit var sessionManager: SessionManager
    private lateinit var afectados : List<AfectadoADTO>
    private lateinit var api: AfectadoApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Configurar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar el DrawerLayout
        setupDrawerLayout()

        // Configurar botones del menú
        setupMenuButtons()

        // Configurar datos de usuario en el menú
        setupUserData()


    }

    private fun setupDrawerLayout() {
        // Configurar botón hamburguesa para abrir el menú
        binding.btnMenuHamburguesa.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Configurar botón de centrar en ubicación
        binding.fabCenter.setOnClickListener {
            centerMapOnUserLocation()
        }
    }

    private fun setupMenuButtons() {
        // Configurar botón de perfil
        binding.btnPerfil.setOnClickListener {
            Toast.makeText(this, "Perfil seleccionado", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de mensajes
        binding.btnMensajes.setOnClickListener {
            Toast.makeText(this, "Mensajes seleccionados", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de registrar voluntario
        binding.btnRegistrarNecesidad.setOnClickListener {
            // Corregido: usar RegistrarVoluntariosActivity en lugar de RegistrarVoluntarioActivity
            val intent = Intent(this, AñadirNecesidadActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de registrar afectado


        // Configurar botón de ajustes
        binding.btnAjustes.setOnClickListener {
            Toast.makeText(this, "Ajustes seleccionados", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            // Limpiar datos de sesión
            sessionManager.logout() // Cambiado clearUserData() por logout()

            // Redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupUserData() {
        // Mostrar nombre de usuario y email desde SessionManager
        binding.txtUserName.text = sessionManager.getUserName() ?: "Usuario" // Cambiado getNombre por getUserName
        binding.txtUserEmail.text = sessionManager.getUserEmail() ?: "usuario@email.com" // Cambiado getEmail por getUserEmail
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Cargar y centrar en la ubicación del afectado (si existe)
        centerMapOnUserLocation()
    }

    private fun centerMapOnUserLocation() {

        val latitud = sessionManager.getUserLatitud() // Cambiado getLatitud por getUserLatitud
        val longitud = sessionManager.getUserLongitud() // Cambiado getLongitud por getUserLongitud
        val direccion = sessionManager.getUserDireccion() // Cambiado getDireccion por getUserDireccion

        if (latitud != 0.0 && longitud != 0.0) {
            // Si hay ubicación guardada, centrar mapa en ella
            val ubicacion = LatLng(latitud, longitud)
            mMap.clear()
            mMap.addMarker(MarkerOptions()
                .position(ubicacion)
                .title(direccion ?: "Mi ubicación"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f))
        } else {
            // Si no hay ubicación guardada, centrar en España
            val espana = LatLng(40.4168, -3.7038)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 5f))
        }
    }
}