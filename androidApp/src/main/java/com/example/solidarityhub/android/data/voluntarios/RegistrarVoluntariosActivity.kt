package com.example.solidarityhub.android.voluntarios

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntariosBinding
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistrarVoluntariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarVoluntariosBinding
    private val selectedCapacidades = mutableSetOf<String>()
    private val selectedDates = mutableSetOf<Long>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarVoluntariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la barra de acción para mostrar el botón de retroceso
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Registro de Voluntario"
        }

        setupCardListeners()
      //  setupCalendar()
        setupProvinciasSpinner()
        setupRegisterButton()
        updateSelectedDatesText() // Inicializar texto de fechas seleccionadas
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupCardListeners() {
        val cards = mapOf(
            binding.cardLimpieza to "Limpieza",
            binding.cardMaterialPesado to "Material Pesado",
            binding.cardSuministros to "Suministros",
            binding.cardSanidad to "Sanidad",
            binding.cardHospedaje to "Hospedaje",
            binding.cardTransporte to "Transporte"
        )

        cards.forEach { (card, capacidad) ->
            card.setOnClickListener {
                toggleCardSelection(card, capacidad)
            }
        }
    }

    private fun toggleCardSelection(card: MaterialCardView, capacidad: String) {
        card.isChecked = !card.isChecked

        if (card.isChecked) {
            selectedCapacidades.add(capacidad)
        } else {
            selectedCapacidades.remove(capacidad)
        }
    }

    /*/  private fun setupCalendar() {
        // Obtenemos la referencia al MaterialCalendarView
        val calendarView = binding.calendarView

        // Configurar para selección múltiple
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE

        // Establecer fecha mínima como hoy
        val today = CalendarDay.today()
        calendarView.state().edit()
            .setMinimumDate(today)
            .commit()

        // Listener para fechas seleccionadas
        calendarView.setOnDateChangedListener { _, date, selected ->
            // Convertimos CalendarDay a Date
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.month - 1, date.day)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dateInMillis = calendar.timeInMillis

            // Añadir o eliminar fecha según corresponda
            if (selected) {
                selectedDates.add(dateInMillis)
                Toast.makeText(this, "Fecha añadida: ${dateFormat.format(Date(dateInMillis))}", Toast.LENGTH_SHORT).show()
            } else {
                selectedDates.remove(dateInMillis)
                Toast.makeText(this, "Fecha eliminada", Toast.LENGTH_SHORT).show()
            }

            // Actualizar texto de fechas seleccionadas
            updateSelectedDatesText()
        }
    }*/

    private fun updateSelectedDatesText() {
        if (selectedDates.isEmpty()) {
            binding.tvFechasSeleccionadas.text = "Fechas seleccionadas: Ninguna"
        } else {
            val formatText = selectedDates
                .sortedBy { it }
                .joinToString("\n") { "• ${dateFormat.format(Date(it))}" }

            binding.tvFechasSeleccionadas.text = "Fechas seleccionadas:\n$formatText"
        }
    }

    private fun setupProvinciasSpinner() {
        val provincias = arrayOf(
            "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz",
            "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real",
            "Córdoba", "Cuenca", "Gerona", "Granada", "Guadalajara", "Guipúzcoa", "Huelva",
            "Huesca", "Islas Baleares", "Jaén", "La Coruña", "La Rioja", "Las Palmas",
            "León", "Lérida", "Lugo", "Madrid", "Málaga", "Murcia", "Navarra", "Orense",
            "Palencia", "Pontevedra", "Salamanca", "Santa Cruz de Tenerife", "Segovia",
            "Sevilla", "Soria", "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid",
            "Vizcaya", "Zamora", "Zaragoza"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, provincias)
        binding.actvProvincias.setAdapter(adapter)
    }

    private fun setupRegisterButton() {
        binding.btnRegistrarVoluntario.setOnClickListener {
            if (validateForm()) {
                registerVoluntario()
            }
        }
    }

    private fun getFormattedDateList(): String {
        return selectedDates
            .sortedBy { it }
            .joinToString("\n") { "• ${dateFormat.format(Date(it))}" }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validar capacidades
        if (selectedCapacidades.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una capacidad", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validar fechas
        if (selectedDates.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una fecha disponible", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Validar provincia
        if (binding.actvProvincias.text.toString().isEmpty()) {
            binding.tilProvincias.error = "Selecciona una provincia"
            isValid = false
        } else {
            binding.tilProvincias.error = null
        }

        return isValid
    }

    private fun registerVoluntario() {
        val provincia = binding.actvProvincias.text.toString()
        val capacidadesStr = selectedCapacidades.joinToString(", ")

        val mensaje = "Voluntario registrado exitosamente en $provincia\n" +
                "Capacidades: $capacidadesStr\n" +
                "Fechas disponibles:\n${getFormattedDateList()}"

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}