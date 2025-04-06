package com.example.solidarityhub.android.ui.login
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.util.Pair
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntariosBinding
import java.util.Locale
import com.example.solidarityhub.android.R
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker


class RegistrarVoluntarios : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var dateRangePicker: MaterialDatePicker<Pair<Long, Long>>
    private lateinit var binding: ActivityRegistrarVoluntariosBinding
    private val year: Int = 0
    private val month: Int = 0
    private val day: Int = 0
    private var fechaInicio: Calendar? = null
    private var fechaFin: Calendar? = null
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
    }




    // Configurar el CalendarView
    fun configurarCalendario() {
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







    fun configurarChips(chipIds: List<Int>, capacidadesStrings: List<String>) {
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
}