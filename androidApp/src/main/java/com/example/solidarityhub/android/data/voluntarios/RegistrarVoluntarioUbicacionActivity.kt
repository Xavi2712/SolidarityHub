package com.example.solidarityhub.android.ui.voluntarios

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.solidarityhub.android.inicio.Menu_activity
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntarioUbicacionBinding
import com.example.solidarityhub.android.util.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.model.ConvertirVoluntarioRequest
import com.example.solidarityhub.android.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrarVoluntarioUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRegistrarVoluntarioUbicacionBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager

    private var currentLatLng: LatLng? = null
    private var currentAddress: String = ""
    private var currentRadius: Int = 5000 // Radio predeterminado en metros
    private var mapCircle: CircleOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarVoluntarioUbicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializar el proveedor de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar el SeekBar para el radio
        setupRadiusSlider()

        // Configurar botón de búsqueda
        binding.btnSearch.setOnClickListener {
            searchAddress()
        }

        // Configurar botón de confirmación
        binding.btnConfirmLocation.setOnClickListener {
            saveLocationAndFinish()
        }

        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRadiusSlider() {
        // Establecer el valor inicial en el TextView
        updateRadiusText(5000) // 5km por defecto

        // Configurar el SeekBar (que reemplazó al Slider)
        binding.sliderRadio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Convertir el progreso (0-9) a radio en metros (1000-10000)
                val radius = (progress + 1) * 1000
                updateRadiusText(radius)
                currentRadius = radius

                // Actualizar círculo en el mapa si ya hay una ubicación seleccionada
                if (currentLatLng != null) {
                    drawRadiusCircle()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun updateRadiusText(radius: Int) {
        binding.tvRadiusValue.text = "$radius m"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            // Obtener la ubicación actual
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)
                    getAddressFromLocation()
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))
                    drawRadiusCircle()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Configurar listener para cuando se mueva la cámara del mapa
        mMap.setOnCameraIdleListener {
            // El pin siempre está en el centro, así que obtenemos las coordenadas del centro
            currentLatLng = mMap.cameraPosition.target
            getAddressFromLocation()
            drawRadiusCircle()
        }
    }

    private fun drawRadiusCircle() {
        currentLatLng?.let { latLng ->
            // Limpiar círculo anterior
            mMap.clear()

            // Dibujar nuevo círculo
            val circleOptions = CircleOptions()
                .center(latLng)
                .radius(currentRadius.toDouble())
                .strokeWidth(2f)
                .strokeColor(0x50FF0000)
                .fillColor(0x30FF0000)

            mMap.addCircle(circleOptions)
        }
    }

    private fun getAddressFromLocation() {
        currentLatLng?.let { latLng ->
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressLine = address.getAddressLine(0) ?: ""

                    currentAddress = addressLine
                    binding.searchAddress.setText(addressLine)

                    // No necesitas crear un nuevo LatLng ni mover la cámara aquí
                    // Ya tenemos currentLatLng y el mapa se actualiza en otros lugares
                } else {
                    Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo dirección: ${e.message}")
            }
        }
    }

    private fun searchAddress() {
        val searchQuery = binding.searchAddress.text.toString().trim()

        if (searchQuery.isNotEmpty()) {
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addressList = geocoder.getFromLocationName(searchQuery, 1)

                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)

                    currentLatLng = latLng
                    currentAddress = searchQuery

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al buscar dirección", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error al buscar dirección: ${e.message}")
            }
        }
    }

    private fun saveLocationAndFinish() {
        if (currentLatLng == null) {
            Toast.makeText(this, "Por favor, selecciona una ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar la ubicación en SessionManager
        sessionManager.saveUserLocation(
            currentLatLng!!.latitude,
            currentLatLng!!.longitude,
            currentAddress
        )

        // Guardar el radio seleccionado
        sessionManager.saveVoluntarioRadius(currentRadius)

        // Guardar rol como "voluntario"
        sessionManager.saveUserRole("voluntario")

        // Registrar al voluntario en la base de datos
        registerVoluntarioInDatabase()
    }

    private fun registerVoluntarioInDatabase() {
        // Obtener los datos guardados en SessionManager
        val capacidades = sessionManager.getTempCapacidades() ?: ArrayList()
        val diasDisponibles = sessionManager.getTempDiasDisponibles() ?: ArrayList()
        val dni = sessionManager.getDni()

        if (dni.isNullOrEmpty()) {
            Toast.makeText(this, "Error: DNI no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLatLng == null) {
            Toast.makeText(this, "Error: Ubicación no seleccionada", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar progreso al usuario
        binding.progressBar.visibility = View.VISIBLE
        binding.btnConfirmLocation.isEnabled = false

        // Crear objeto de solicitud
        val voluntarioRequest = ConvertirVoluntarioRequest(
            dias_disp = diasDisponibles,
            capacidades = capacidades,
            latitud = currentLatLng!!.latitude,
            longitud = currentLatLng!!.longitude,
            alcance = currentRadius
        )

        // Registrar la información en los logs
        Log.d(TAG, "Registrando voluntario con ubicación: $currentLatLng")
        Log.d(TAG, "Dirección: $currentAddress")
        Log.d(TAG, "Radio: $currentRadius")
        Log.d(TAG, "Capacidades: $capacidades")
        Log.d(TAG, "Días disponibles: $diasDisponibles")
        Log.d(TAG, "DNI: $dni")

        // Realizar la llamada al API en un hilo secundario
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.voluntarioApiService.convertirVoluntario(
                    dni = dni,
                    request = voluntarioRequest
                )

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnConfirmLocation.isEnabled = true

                    if (response.isSuccessful) {
                        // Limpiar datos temporales
                        sessionManager.clearTempData()
                        
                        Toast.makeText(this@RegistrarVoluntarioUbicacionActivity, 
                            "¡Registro de voluntario completado!", Toast.LENGTH_LONG).show()
                        
                        // Navegar al menú principal
                        val intent = Intent(this@RegistrarVoluntarioUbicacionActivity, Menu_activity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = "Error: ${response.code()} - ${response.message()}"
                        Log.e(TAG, errorMessage)
                        Toast.makeText(this@RegistrarVoluntarioUbicacionActivity, 
                            "Error al registrar: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en la solicitud: ${e.message}", e)
                
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnConfirmLocation.isEnabled = true
                    Toast.makeText(this@RegistrarVoluntarioUbicacionActivity, 
                        "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, reiniciar actividad
                recreate()
            } else {
                Toast.makeText(
                    this,
                    "Se requiere permiso de ubicación para usar esta función",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "RegistrarVolUbicacion"
    }
}