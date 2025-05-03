package com.example.solidarityhub.android.afectados

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.data.remote.UbicacionRequest
import com.example.solidarityhub.android.databinding.ActivityRegistrarAfectadosBinding
import com.example.solidarityhub.android.util.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.location.Geocoder
import java.io.IOException
import java.util.Locale

class RegistrarAfectadosActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRegistrarAfectadosBinding
    private lateinit var mMap: GoogleMap
    private lateinit var sessionManager: SessionManager
    private var currentLatLng: LatLng? = null
    private var currentAddress: String = ""

    // Autocompletar
    private var autocompleteJob: Job? = null
    private lateinit var adapter: ArrayAdapter<String>
    private val geocoder: Geocoder by lazy { Geocoder(this, Locale("es", "ES")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarAfectadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar session manager para obtener el DNI
        sessionManager = SessionManager(this)

        // Crear adaptador para sugerencias (corregido)
        adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ArrayList<String>())
        val autoCompleteTextView = binding.etBuscarDireccion as AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter) // Modificado para usar correctamente setAdapter
        autoCompleteTextView.threshold = 3 // Mostrar sugerencias después de 3 caracteres

        // Permitir caracteres especiales
        autoCompleteTextView.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            // No filtrar ningún carácter - permite todos los caracteres incluidos ñ, ç, etc.
            null
        })

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar buscador con autocompletado
        setupAutoCompleteSearch()

        // Configurar botón de registro
        binding.btnRegistrarUbicacion.setOnClickListener {
            registerAfectado()
        }
    }

    private fun setupAutoCompleteSearch() {
        // Configurar el autocompletado
        val autoCompleteTextView = binding.etBuscarDireccion as AutoCompleteTextView

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
                    // Cancelar búsqueda anterior si existe
                    autocompleteJob?.cancel()

                    // Iniciar nueva búsqueda con debounce
                    autocompleteJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300) // debounce
                        getAddressPredictions(query)
                    }
                }
            }
        })
    }

    private fun getAddressPredictions(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Usar Geocoder para obtener predicciones de direcciones
                val addresses = geocoder.getFromLocationName(query, 5) ?: emptyList()
                val suggestions = addresses.map {
                    it.getAddressLine(0) ?: ""
                }.filter { it.isNotEmpty() }

                withContext(Dispatchers.Main) {
                    // Actualizar adaptador con nuevas sugerencias
                    adapter.clear()
                    adapter.addAll(suggestions)
                    adapter.notifyDataSetChanged()

                    // Forzar mostrar dropdown
                    if (suggestions.isNotEmpty()) {
                        (binding.etBuscarDireccion as AutoCompleteTextView).showDropDown()
                    }
                }
            } catch (e: IOException) {
                Log.e("Geocoder", "Error obteniendo sugerencias: ${e.message}")
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
                        // Actualizar información actual
                        currentLatLng = latLng
                        currentAddress = address

                        // Actualizar UI
                        updateMapLocation(latLng, address)
                        binding.tvDireccionSeleccionada.text = "Dirección seleccionada: $address"
                        binding.tvCoordenadas.text = "Coordenadas: ${location.latitude}, ${location.longitude}"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegistrarAfectadosActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("Geocoder", "Error: ${e.message}")
                    Toast.makeText(this@RegistrarAfectadosActivity, "Error al buscar dirección", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMapLocation(latLng: LatLng, title: String) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun registerAfectado() {
        if (currentLatLng == null) {
            Toast.makeText(this, "Por favor seleccione una ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el DNI desde SessionManager
        val dni = sessionManager.getDni()
        if (dni.isNullOrEmpty()) {
            Toast.makeText(this, "Error: DNI no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Datos para enviar al servidor
        val ubicacionData = UbicacionRequest(
            latitud = currentLatLng!!.latitude,
            longitud = currentLatLng!!.longitude,
            direccion = currentAddress
        )

        // Llamar al endpoint
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.afectadoApiService.convertirUsuarioAfectado(dni, ubicacionData)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegistrarAfectadosActivity, "Registro exitoso como afectado", Toast.LENGTH_LONG).show()
                        finish() // Cerrar actividad y volver a la principal
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                        Toast.makeText(this@RegistrarAfectadosActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                        Log.e("Afectado", "Error: ${response.code()} - $errorMsg")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegistrarAfectadosActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("Afectado", "Excepción: ${e.message}")
                }
            }
        }
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

                            // Actualizar el mapa y la interfaz
                            updateMapLocation(latLng, addressText)
                            binding.etBuscarDireccion.setText(addressText)
                            binding.tvDireccionSeleccionada.text = "Dirección seleccionada: $addressText"
                            binding.tvCoordenadas.text = "Coordenadas: ${latLng.latitude}, ${latLng.longitude}"
                        } else {
                            currentAddress = "${latLng.latitude}, ${latLng.longitude}"
                            updateMapLocation(latLng, "Ubicación seleccionada")
                            binding.tvDireccionSeleccionada.text = "Dirección seleccionada: (Sin datos)"
                            binding.tvCoordenadas.text = "Coordenadas: ${latLng.latitude}, ${latLng.longitude}"
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Log.e("Geocoder", "Error: ${e.message}")
                        currentAddress = "${latLng.latitude}, ${latLng.longitude}"
                        updateMapLocation(latLng, "Ubicación seleccionada")
                        binding.tvDireccionSeleccionada.text = "Dirección seleccionada: (Sin datos)"
                        binding.tvCoordenadas.text = "Coordenadas: ${latLng.latitude}, ${latLng.longitude}"
                    }
                }
            }
        }

        // Iniciar con una ubicación por defecto (España)
        val madrid = LatLng(40.4168, -3.7038)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 5f))
    }

    override fun onDestroy() {
        super.onDestroy()
        autocompleteJob?.cancel()
    }
}