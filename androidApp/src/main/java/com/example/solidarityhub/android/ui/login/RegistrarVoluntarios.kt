package com.example.solidarityhub.android.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntariosBinding

import com.example.solidarityhub.android.R
import com.google.android.material.chip.Chip

class RegistrarVoluntarios : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
private lateinit var binding: ActivityRegistrarVoluntariosBinding

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

        // Asignar listeners a los chips
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

        // Configurar el CalendarView
        findViewById<CalendarView>(R.id.calendarView).setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Manejar fecha seleccionada
        }
    }
}
