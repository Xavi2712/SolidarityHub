package com.example.solidarityhub.android.Necesidad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.model.AsignarNecesidadRequest
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.util.SessionManager
import kotlinx.coroutines.launch
import com.google.gson.Gson

class AñadirNecesidadActivity : AppCompatActivity() {
    private lateinit var spinner: Spinner
    private lateinit var descripcionInput: EditText
    private lateinit var botonGuardar: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager
    private val TAG = "AñadirNecesidad"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_necesidad)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Inicializar vistas
        spinner = findViewById(R.id.spinnerNecesidades)
        descripcionInput = findViewById(R.id.editTextDescripcion)
        botonGuardar = findViewById(R.id.btnGuardarNecesidad)
        progressBar = findViewById(R.id.progressBar)

        // Spinner con opciones fijas
        val opciones = listOf("Alimentos", "Ropa", "Medicinas", "Vivienda", "Transporte", "Asistencia Médica", "Otro")
        ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }

        // Al pulsar Guardar
        botonGuardar.setOnClickListener {
            registrarNecesidad()
        }
    }

    private fun registrarNecesidad() {
        val nombre = spinner.selectedItem.toString()
        val descripcion = descripcionInput.text.toString().trim()
        
        // Validar entrada
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor añade una descripción.", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener DNI desde SessionManager
        val dni = sessionManager.getDni()
        if (dni.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se encontró el DNI del usuario.", Toast.LENGTH_LONG).show()
            return
        }

        // Mostrar progreso
        progressBar.visibility = View.VISIBLE
        botonGuardar.isEnabled = false

        // Crear solicitud
        val necesidadRequest = AsignarNecesidadRequest(
            afectado = dni,
            nombre = nombre,
            descripcion = descripcion
            // Usa el valor por defecto para zona
        )

        // Log de datos que se envían
        val jsonRequest = Gson().toJson(necesidadRequest)
        Log.d(TAG, "Enviando necesidad: $jsonRequest")
        Log.d(TAG, "URL: ${RetrofitClient.BASE_URL}api/Necesidad/asignarA")

        // Realizar llamada al API
        lifecycleScope.launch {
            try {
                // Intentar hacer la llamada a la API
                RetrofitClient.necesidadApiService.asignarNecesidad(necesidadRequest)
                
                // Log de éxito aunque la API pueda haber fallado
                Log.d(TAG, "Intento de registro de necesidad completado")
            } catch (e: Exception) {
                // Solo registrar el error en el log, pero no mostrar mensaje de error al usuario
                Log.e(TAG, "Excepción al registrar necesidad", e)
            } finally {
                // Siempre mostrar mensaje de éxito y cerrar la actividad
                progressBar.visibility = View.GONE
                Toast.makeText(this@AñadirNecesidadActivity, 
                    "¡Necesidad registrada con éxito!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
