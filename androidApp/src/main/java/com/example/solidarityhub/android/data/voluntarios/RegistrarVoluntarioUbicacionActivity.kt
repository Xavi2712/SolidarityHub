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
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.model.ConvertirVoluntarioRequest
import com.example.solidarityhub.android.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.AdapterView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.IOException

class RegistrarVoluntarioUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRegistrarVoluntarioUbicacionBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sessionManager: SessionManager

    private var currentLatLng: LatLng? = null
    private var currentAddress: String = ""
    private var currentRadius: Int = 5000 // Radio predeterminado en metros
    private var mapCircle: CircleOptions? = null

    // Autocompletar
    private var autocompleteJob: Job? = null
    private lateinit var adapter: ArrayAdapter<String>
    private val geocoder: Geocoder by lazy { Geocoder(this, Locale("es", "ES")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarVoluntarioUbicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Crear adaptador para sugerencias
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ArrayList<String>())
        val autoCompleteTextView = binding.searchAddress as AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.threshold = 3

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializar el proveedor de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar el SeekBar para el radio
        setupRadiusSlider()

        // Configurar buscador con autocompletado
        setupAutoCompleteSearch()

        // Configurar botón de confirmación
        binding.btnConfirmLocation.setOnClickListener {
            saveLocationAndFinish()
        }

        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupAutoCompleteSearch() {
        val autoCompleteTextView = binding.searchAddress as AutoCompleteTextView

        // Manejo del cambio de texto para autocompletado
        autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedAddress = adapter.getItem(position) ?: return@OnItemClickListener
            searchLocation(selectedAddress)
        }

        // Detectar cambios de texto para buscar sugerencias
        autoCompleteTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (autoCompleteTextView.text.toString().isNotEmpty()) {
                    searchLocation(autoCompleteTextView.text.toString())
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // Implementar autocompletado al escribir
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.length >= 3) {
                    autocompleteJob?.cancel()
                    autocompleteJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        getAddressPredictions(query)
                    }
                }
            }
        })
    }

    private fun getAddressPredictions(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocationName(query, 5) ?: emptyList()
                val suggestions = addresses.map {
                    it.getAddressLine(0) ?: ""
                }.filter { it.isNotEmpty() }

                withContext(Dispatchers.Main) {
                    adapter.clear()
                    adapter.addAll(suggestions)
                    adapter.notifyDataSetChanged()

                    if (suggestions.isNotEmpty()) {
                        (binding.searchAddress as AutoCompleteTextView).showDropDown()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error obteniendo sugerencias: ${e.message}")
            }
        }
    }

    private fun searchLocation(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocationName(address, 1) ?: emptyList()
                if (addresses.isNotEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)

                    withContext(Dispatchers.Main) {
                        currentLatLng = latLng
                        currentAddress = address
                        updateMapLocation(latLng, address)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegistrarVoluntarioUbicacionActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error: ${e.message}")
                    Toast.makeText(this@RegistrarVoluntarioUbicacionActivity, "Error al buscar dirección", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMapLocation(latLng: LatLng, title: String) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        drawRadiusCircle()
    }

    private fun setupRadiusSlider() {
        updateRadiusText(5000)

        binding.sliderRadio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val radius = (progress + 1) * 1000
                updateRadiusText(radius)
                currentRadius = radius
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

        // Configurar el mapa para permitir seleccionar ubicación al hacer clic
        mMap.setOnMapClickListener { latLng ->
            currentLatLng = latLng

            // Geocodificar en reversa para obtener la dirección
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) ?: emptyList()

                    withContext(Dispatchers.Main) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val addressText = address.getAddressLine(0) ?: "${latLng.latitude}, ${latLng.longitude}"
                            currentAddress = addressText

                            updateMapLocation(latLng, addressText)
                            binding.searchAddress.setText(addressText)
                        } else {
                            currentAddress = "${latLng.latitude}, ${latLng.longitude}"
                            updateMapLocation(latLng, "Ubicación seleccionada")
                            binding.searchAddress.setText("${latLng.latitude}, ${latLng.longitude}")
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Error: ${e.message}")
                        currentAddress = "${latLng.latitude}, ${latLng.longitude}"
                        updateMapLocation(latLng, "Ubicación seleccionada")
                        binding.searchAddress.setText("${latLng.latitude}, ${latLng.longitude}")
                    }
                }
            }
        }

        // Iniciar con una ubicación por defecto (España)
        val madrid = LatLng(40.4168, -3.7038)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 6f))
    }

    private fun drawRadiusCircle() {
        currentLatLng?.let { latLng ->
            mMap.clear()
            val circleOptions = CircleOptions()
                .center(latLng)
                .radius(currentRadius.toDouble())
                .strokeWidth(2f)
                .strokeColor(0x50FF0000)
                .fillColor(0x30FF0000)

            mMap.addCircle(circleOptions)
            mMap.addMarker(MarkerOptions().position(latLng).title(currentAddress))
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

    override fun onDestroy() {
        super.onDestroy()
        autocompleteJob?.cancel()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "RegistrarVolUbicacion"
    }
}