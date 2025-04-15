package com.example.solidarityhub.android.ui.voluntarios

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.dto.VoluntarioDTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntarios2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RegistrarVoluntariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarVoluntarios2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarVoluntarios2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se usa para capturar la fecha seleccionada del CalendarView.
        var fechaSeleccionada: Long = binding.calendarView.date
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            fechaSeleccionada = cal.timeInMillis
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.tvrango.text = "Fecha seleccionada: ${sdf.format(Date(fechaSeleccionada))}"
        }

        binding.btnRegistrar.setOnClickListener {
            // --- Capturar la fecha seleccionada ---
            val fecha = Date(fechaSeleccionada)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val diasDisponibles: List<String> = listOf(sdf.format(fecha))

            // --- Recoger las capacidades seleccionadas a través de los chips ---
            val capacidadesSeleccionadas = mutableListOf<String>()
            if (binding.chipLimpieza.isChecked) { capacidadesSeleccionadas.add("Limpieza") }
            if (binding.chipTransporte.isChecked) { capacidadesSeleccionadas.add("Transporte") }
            if (binding.chipOtro.isChecked) { capacidadesSeleccionadas.add("Otro") }
            if (binding.chipReparaciones.isChecked) { capacidadesSeleccionadas.add("Reparaciones") }
            if (binding.chipBusqueda.isChecked) { capacidadesSeleccionadas.add("Búsqueda") }
            if (binding.chipAlojamiento.isChecked) { capacidadesSeleccionadas.add("Alojamiento") }

            // --- Recoger la dirección, que se usará como provincia ---
            val provincia = binding.etDireccion.text.toString().trim()
            if (provincia.isEmpty()) {
                Toast.makeText(this, "Debe introducir una dirección", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Hardcodear DNI (u obtenerlo de otro modo en la aplicación) ---
            val dni = "12345678A"

            // --- Construir el objeto DTO conforme a la API ---
            val voluntarioDTO = VoluntarioDTO(
                dni = dni,
                dias_disp = diasDisponibles,
                provincia = provincia,
                capacidades = capacidadesSeleccionadas
            )

            // --- Mostrar un ProgressDialog mientras se realiza la llamada ---
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.procesando))
            progressDialog.setCancelable(false)
            progressDialog.show()

            // --- Llamada al servicio usando corrutinas ---
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.voluntarioApiService.addVoluntario(voluntarioDTO)
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Voluntario registrado correctamente",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Error al registrar el voluntario",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            applicationContext,
                            "Se produjo un error: ${ex.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        binding.btnReset?.setOnClickListener {
            // Reiniciamos los campos de la UI
            binding.etDireccion.text?.clear()
            binding.chipLimpieza.isChecked = false
            binding.chipTransporte.isChecked = false
            binding.chipOtro.isChecked = false
            binding.chipReparaciones.isChecked = false
            binding.chipBusqueda.isChecked = false
            binding.chipAlojamiento.isChecked = false
            binding.calendarView.date = Calendar.getInstance().timeInMillis
            binding.tvrango.text = ""
            Toast.makeText(this, "Campos reiniciados", Toast.LENGTH_SHORT).show()
        }
    }
}
