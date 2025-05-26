package com.example.solidarityhub.android.Necesidad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.model.NecesidadRequest
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.util.SessionManager
import kotlinx.coroutines.launch
import com.google.gson.Gson
import java.time.Instant
import java.time.format.DateTimeFormatter

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
            if (validarCampos()) {
                registrarNecesidad()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val descripcion = descripcionInput.text.toString().trim()
        
        if (descripcion.isEmpty()) {
            descripcionInput.error = "Por favor añade una descripción"
            Toast.makeText(this, "Por favor añade una descripción.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (descripcion.length < 10) {
            descripcionInput.error = "La descripción debe tener al menos 10 caracteres"
            Toast.makeText(this, "La descripción debe tener al menos 10 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registrarNecesidad() {
        val nombre = spinner.selectedItem.toString()
        val descripcion = descripcionInput.text.toString().trim()
        
        // Obtener DNI desde SessionManager
        val dni = sessionManager.getDni()
        if (dni.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se encontró el DNI del usuario.", Toast.LENGTH_LONG).show()
            return
        }

        // Obtener zona desde SessionManager
        val zona = sessionManager.getUserAddress() ?: "Valencia"

        // Obtener fecha actual en formato ISO 8601
        val fechaCreacion = Instant.now().toString()

        // Mostrar progreso
        progressBar.visibility = View.VISIBLE
        botonGuardar.isEnabled = false

        // Crear solicitud
        val necesidadRequest = NecesidadRequest(
            afectado = dni,
            descripcion = descripcion,
            nombre = nombre,
            zona = zona,
            estado = "SIN COMENZAR",
            fecha_creacion = fechaCreacion
        )

        // Log de datos que se envían
        val jsonRequest = Gson().toJson(necesidadRequest)
        Log.d(TAG, "Enviando necesidad: $jsonRequest")
        Log.d(TAG, "URL: ${RetrofitClient.BASE_URL}api/Necesidad/registrar")

        // Realizar llamada al API
        lifecycleScope.launch {
            try {
                // Intentar hacer la llamada a la API
                val response = RetrofitClient.necesidadApiService.registrarNecesidad(necesidadRequest)
                
                if (response.isSuccessful) {
                    Toast.makeText(this@AñadirNecesidadActivity, 
                        "¡Necesidad registrada con éxito!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    val mensajeError = when (response.code()) {
                        400 -> "Datos inválidos. Por favor, verifica la información."
                        401 -> "Sesión expirada. Por favor, vuelve a iniciar sesión."
                        403 -> "No tienes permiso para registrar necesidades."
                        404 -> "Servicio no encontrado."
                        500 -> "Error del servidor. Por favor, intenta más tarde."
                        else -> "Error: $errorMsg"
                    }
                    Toast.makeText(this@AñadirNecesidadActivity, 
                        mensajeError, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error: ${response.code()} - $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al registrar necesidad", e)
                val mensajeError = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "Error de conexión. Verifica tu conexión a internet."
                    e.message?.contains("timeout") == true -> 
                        "Tiempo de espera agotado. Por favor, intenta de nuevo."
                    else -> "Error de conexión: ${e.message}"
                }
                Toast.makeText(this@AñadirNecesidadActivity, 
                    mensajeError, Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                botonGuardar.isEnabled = true
            }
        }
    }
}
