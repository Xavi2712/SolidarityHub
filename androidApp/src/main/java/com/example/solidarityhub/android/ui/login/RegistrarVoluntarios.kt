package com.example.solidarityhub.android.ui.login
import com.example.solidarityhub.android.adapters.PlacesAdapter
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

import androidx.core.util.Pair
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntariosBinding
import java.util.Locale
import com.example.solidarityhub.android.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.example.solidarityhub.android.data.model.PlaceSuggestion


class RegistrarVoluntarios : AppCompatActivity(), OnMapReadyCallback{

    private var marker: Marker? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var viewModel: LoginViewModel
    private lateinit var dateRangePicker: MaterialDatePicker<Pair<Long, Long>>
    private lateinit var binding: ActivityRegistrarVoluntariosBinding
    private val year: Int = 0
    private val month: Int = 0
    private val day: Int = 0
    private var fechaInicio: Calendar? = null
    private var fechaFin: Calendar? = null
    private lateinit var suggestionsAdapter: PlacesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrarVoluntariosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Configurar los chips
        val chipIds = listOf(
            R.id.Limpieza,
            R.id.Transporte,
            R.id.Reparaciones,
            R.id.Busqueda,
            R.id.Alojamiento
        )

        val capacidadesStrings = viewModel.getCapacidadesIds().map { id ->
            when (id) {
                R.id.Limpieza -> getString(R.string.Limpieza)
                R.id.Transporte -> getString(R.string.Transporte)
                R.id.Reparaciones -> getString(R.string.Reparaciones)
                R.id.Busqueda -> getString(R.string.Búsqueda)
                R.id.Alojamiento -> getString(R.string.Alojamiento)
                else -> ""
            }.takeIf { it.isNotBlank() }
        }.filterNotNull()

        configurarChips(chipIds, capacidadesStrings)
        configurarCalendario()
        configurarBtonSeleccionRango()
        configurarResetButton()
        configurarsearchBar()
    }




    // Configurar el CalendarView
    private fun configurarCalendario() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)

                set(Calendar.MILLISECOND, 0)
            }

            when {
                // No hay fecha inicial seleccionada
                fechaInicio == null -> {
                    fechaInicio = selectedDate
                    showToast("Selecciona la fecha final")
                    updateDateRangeText()
                }
                // Hay fecha inicial pero no final
                fechaFin == null -> {
                    // Siempre asignar como fecha final (aunque sea anterior)
                    fechaFin = selectedDate

                    // Mostrar las fechas en el orden seleccionado
                    updateDateRangeText()
                    showToast("Rango seleccionado: ${formatDateRange()}")
    }
                else -> {
                    fechaInicio = selectedDate
                    showToast("Selecciona la fecha final")

                    fechaFin = null
                    updateDateRangeText()
                }
            }
        }
    }

    private fun updateDateRangeText() {
        binding.tvRango?.text = when {
            fechaInicio== null -> "No se ha seleccionado rango"
            fechaFin == null -> "Fecha inicial: ${formatDate(fechaInicio)} - Selecciona fecha final"
            else -> "Rango seleccionado: ${formatDate(fechaInicio)} - ${formatDate(fechaFin)}"
        }
    }

    private fun formatDate(date: Calendar?): String {
        return date?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time)
        } ?: "No seleccionada"
    }

    private fun formatDateRange(): String {
        return "${formatDate(fechaInicio)} - ${formatDate(fechaFin)}"
    }
    private fun configurarResetButton() {
        binding.btnReset?.setOnClickListener {
            fechaInicio = null
            fechaFin = null
            updateDateRangeText()
            showToast("Selección reiniciada")
        }
    }
    private fun configurarBtonSeleccionRango() {
        binding.seleccionRango?.setOnClickListener {
            updateDateRangeText()
            showToast("Selección reiniciada")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }







   private fun configurarChips(chipIds: List<Int>, capacidadesStrings: List<String>) {
        chipIds.forEach { chipId ->
            findViewById<Chip>(chipId).setOnClickListener {
                viewModel.cambioSeleccion(chipId)
            }
        }

        // Observar cambios en las selecciones
        viewModel.capacidadesSeleccionadas.observe(this) { selecciones ->
            selecciones.forEach { (chipId, seleccionado) ->
                findViewById<Chip>(chipId).isChecked = seleccionado
            }
        }
    }

    private fun configurarsearchBar() {
        // Conectar SearchBar con SearchView
        binding.searchView?.setupWithSearchBar(binding.searchBar)

        // Configurar RecyclerView para sugerencias
         suggestionsAdapter = PlacesAdapter { place ->
            place.latLng?.let { moveCameraToPlace(it) }
            binding.searchBar?.setText(place.name)
            binding.searchView?.hide()
        }
        binding.rvSuggestions?.adapter = suggestionsAdapter
        binding.rvSuggestions?.layoutManager = LinearLayoutManager(this)

        // Escuchar cambios de texto
        binding.searchView?.editText?.doOnTextChanged { text, _, _, _ ->
            if (text?.length ?: 0 > 2) {
                fetchPlacePredictions(text.toString())
            }
        }


    }

    private fun fetchPlacePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val places = response.autocompletePredictions.map {
                    PlaceSuggestion(
                        it.getFullText(null).toString(), it.placeId,
                        latLng = TODO()
                    )
                }
                suggestionsAdapter.submitList(places)
            }
    }

    private fun moveCameraToPlace(latLng: LatLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        googleMap.addMarker(MarkerOptions().position(latLng))
    }

    // Búsqueda por voz
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un lugar para buscar")
        }
        startActivityForResult(intent, VOICE_REQUEST_CODE)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    companion object {
        private const val VOICE_REQUEST_CODE = 101
    }
}