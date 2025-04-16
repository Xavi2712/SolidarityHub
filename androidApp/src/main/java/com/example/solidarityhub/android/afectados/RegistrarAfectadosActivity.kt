package com.example.solidarityhub.android.afectados

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.databinding.ActivityRegistrarAfectadosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrarAfectadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarAfectadosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarAfectadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            val dni       = binding.etDocumento.text.toString().trim()
            val nombre    = binding.etNombre.text.toString().trim()
            val telefono  = binding.etTelefono.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()

            if (dni.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dto = AfectadoADTO(
                dni        = dni,
                nombre     = nombre,
                telefono   = telefono,
                contraseña = "123456",
                correo     = "test@example.com",
                rol        = "AfectadoA",
                direccion  = direccion
            )

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
                            val msg = response.errorBody()?.string()
                            Log.e("HTTP_ERROR", "${response.code()}-$msg")
                            Toast.makeText(
                                this@RegistrarAfectadosActivity,
                                "Error ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("HTTP_EXCEPTION", e.localizedMessage ?: "err", e)
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
