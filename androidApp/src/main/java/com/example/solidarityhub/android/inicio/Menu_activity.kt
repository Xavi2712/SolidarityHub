package com.example.solidarityhub.android.inicio

import android.os.Bundle
import android.util.Log
import android.view.View
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

        // Verificar si el usuario tiene un rol asignado
        val userRole = sessionManager.getUserRole()
        if (userRole.isNullOrEmpty()) {
            // Si no tiene rol, redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Configurar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar el DrawerLayout
        setupDrawerLayout()

        // Configurar botones del menú
        setupMenuButtons()

        // Configurar datos de usuario en el menú
        setupUserData()

        // Configurar botones según el rol del usuario
        setupRoleSpecificButtons(userRole)
    }

    private fun setupRoleSpecificButtons(userRole: String) {
        // Ocultar o mostrar botones según el rol
        when (userRole) {
            "voluntario" -> {
                binding.btnRegistrarNecesidad.visibility = View.GONE
            }
            "afectado" -> {
                // Los afectados pueden registrar necesidades
                binding.btnRegistrarNecesidad.visibility = View.VISIBLE
            }
        }
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
        setupSimpleToastButton(binding.btnPerfil, "Perfil seleccionado")
        setupSimpleToastButton(binding.btnMensajes, "Mensajes seleccionados")
        setupSimpleToastButton(binding.btnAjustes, "Ajustes seleccionados")

        binding.btnRegistrarNecesidad.setOnClickListener {
            startActivity(Intent(this, AñadirNecesidadActivity::class.java))
            closeDrawer()
        }

        binding.btnCerrarSesion.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupSimpleToastButton(button: View, message: String) {
        button.setOnClickListener {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            closeDrawer()
        }
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }


    private fun setupUserData() {
        // Mostrar nombre de usuario y email desde SessionManager
        binding.txtUserName.text = sessionManager.getUserName() ?: "Usuario"
        binding.txtUserEmail.text = sessionManager.getUserEmail() ?: "usuario@email.com"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Cargar y centrar en la ubicación del usuario
        centerMapOnUserLocation()
    }

    private fun centerMapOnUserLocation() {
        val latitud = sessionManager.getUserLatitud()
        val longitud = sessionManager.getUserLongitud()
        val direccion = sessionManager.getUserDireccion()

        if (latitud != 0.0 && longitud != 0.0) {
            val ubicacion = LatLng(latitud, longitud)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 12f))
            
            // Añadir marcador en la ubicación
            mMap.addMarker(MarkerOptions()
                .position(ubicacion)
                .title(direccion ?: "Mi ubicación"))
        } else {
            // Si no hay ubicación guardada, centrar en España
            val espana = LatLng(40.4168, -3.7038)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 6f))
        }
    }
}