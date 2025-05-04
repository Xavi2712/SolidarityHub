package com.example.solidarityhub.android

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlinx.coroutines.launch

// 1️⃣ Define la data class que enviarás al servidor
data class NecesidadRequest(
    val tipo: String,
    val descripcion: String,
    val afectadoId: Int  // si lo necesitas
)

// 2️⃣ Define la interfaz Retrofit
interface AfectadoApi {
    @POST("api/necesidades")
    suspend fun crearNecesidad(@Body req: NecesidadRequest)
}

class AñadirNecesidadActivity : AppCompatActivity() {
    private lateinit var api: AfectadoApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_necesidad)

        // Inicializa Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://tu-backend.com/") // cambia por tu URL real
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(AfectadoApi::class.java)

        // Vistas
        val spinner: Spinner = findViewById(R.id.spinnerNecesidades)
        val descripcionInput: EditText = findViewById(R.id.editTextDescripcion)
        val botonGuardar: Button = findViewById(R.id.btnGuardarNecesidad)

        // Spinner con opciones fijas
        val opciones = listOf("Comida", "Agua", "Ropa", "Medicamentos", "Alojamiento")
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
                    val afectadoId = intent.getIntExtra("AFECTADO_ID", 0)
                    val req = NecesidadRequest(tipo, descripcion, afectadoId)
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
