package com.example.solidarityhub.android.Necesidad

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.data.model.NecesidadRequest
import com.example.solidarityhub.android.data.remote.AfectadoApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch

// 1️⃣ Define la data class que enviarás al servidor


// 2️⃣ Define la interfaz Retrofit

class AñadirNecesidadActivity : AppCompatActivity() {
    private lateinit var api: AfectadoApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_necesidad)

        // Inicializa Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://tu-backend.com/") // cambia por tu URL real
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(AfectadoApiService::class.java)

        // Vistas
        val spinner: Spinner = findViewById(R.id.spinnerNecesidades)
        val descripcionInput: EditText = findViewById(R.id.editTextDescripcion)
        val botonGuardar: Button = findViewById(R.id.btnGuardarNecesidad)

        // Spinner con opciones fijas
        val opciones = listOf("Alimentos","Ropa","Medicinas","Vivienda","Transporte","Asistencia Médica", "Otro")
        ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }

        // Al pulsar Guardar:
        botonGuardar.setOnClickListener {
            val tipo = spinner.selectedItem.toString()
            val descripcion = descripcionInput.text.toString().trim()
            if (descripcion.isEmpty()) {
                Toast.makeText(this, "Por favor añade una descripción.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Envía la petición al backend
            lifecycleScope.launch {
                try {
                    // Obtener el DNI desde SharedPreferences
                    val prefs = getSharedPreferences("SolidarityHubPrefs", MODE_PRIVATE)
                    val dni = prefs.getString("dni", null)

                    if (dni == null) {
                        Toast.makeText(this@AñadirNecesidadActivity, "Error: no se encontró el DNI del usuario.", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    // Ahora puedes usar el DNI directamente o buscar el ID del afectado con él
                    val req = NecesidadRequest(tipo, descripcion, dni)

                    // Enviar la necesidad
                    api.crearNecesidad(req)
                    Toast.makeText(this@AñadirNecesidadActivity, "Necesidad registrada", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@AñadirNecesidadActivity, "Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
