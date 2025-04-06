package com.example.solidarityhub.android.ui.afectados

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.databinding.ActivityRegistrarAfectados2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrarAfectadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarAfectados2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarAfectados2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el botón Registrar
        binding.btnRegistrar.setOnClickListener {
            // Obtenemos los valores de los campos de texto
            val dni = binding.etDocumento.text.toString().trim()
            val nombre = binding.etNombre.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()

            // Estos campos se dejan vacíos por el momento
            val correo = "test@example.com"
            val contraseña = "123456"
            val rol = "AfectadoA"

            // Validar que los campos obligatorios contengan datos
            if (dni.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construir el DTO tal como lo requiere el backend (nombres y orden exacto)
            val dto = AfectadoADTO(
                dni = dni,
                nombre = nombre,
                telefono = telefono,
                contraseña = contraseña,
                correo = correo,
                rol = rol,
                direccion = direccion
            )

            // Llamar al servicio del backend en una corrutina
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.addAfectadoA(dto)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@RegistrarAfectadosActivity,
                                response.body()?.message ?: "Registro exitoso",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // Se obtiene y muestra el detalle del error desde el backend
                            val errorBody = response.errorBody()?.string()
                            Log.e("HTTP_ERROR", "Error: ${response.code()} - $errorBody")
                            Toast.makeText(
                                this@RegistrarAfectadosActivity,
                                "Error: ${response.code()} - $errorBody",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("HTTP_EXCEPTION", "Excepción: ${e.localizedMessage}", e)
                        Toast.makeText(
                            this@RegistrarAfectadosActivity,
                            "Excepción: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
