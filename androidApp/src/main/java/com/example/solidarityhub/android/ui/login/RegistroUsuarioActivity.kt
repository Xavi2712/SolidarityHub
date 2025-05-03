package com.example.solidarityhub.android.ui.login

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.data.dto.UsuarioDTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.databinding.ActivityRegistroUsuarioBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class RegistroUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            val dni = binding.etDni.text.toString().trim()
            val nombre = binding.etNombre.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()

            if (!validarCampos(dni, nombre, password, telefono, correo)) {
                return@setOnClickListener
            }

            registrarUsuario(dni, nombre, password, telefono, correo)
        }
    }

    private fun validarCampos(dni: String, nombre: String, password: String, telefono: String, correo: String): Boolean {
        // Validar DNI/NIE (8 dígitos + letra o X/Y/Z + 7 dígitos + letra)
        val dniPattern = Pattern.compile("^[0-9]{8}[A-Z]$|^[XYZ][0-9]{7}[A-Z]$")
        if (dni.isEmpty() || !dniPattern.matcher(dni).matches()) {
            Toast.makeText(this, "DNI/NIE inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar nombre
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Nombre requerido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar contraseña (entre 6 y 20 caracteres)
        if (password.length < 6 || password.length > 20) {
            Toast.makeText(this, "La contraseña debe tener entre 6 y 20 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar teléfono
        if (telefono.isEmpty()) {
            Toast.makeText(this, "Teléfono requerido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar correo
        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registrarUsuario(dni: String, nombre: String, password: String, telefono: String, correo: String) {
        val usuarioDTO = UsuarioDTO(
            dni = dni,
            nombre = nombre,
            contraseña = password,
            telefono = telefono,
            correo = correo
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.usuarioApiService.registrarUsuario(usuarioDTO)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "Usuario registrado con éxito",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Log.e("REGISTRO_ERROR", "${response.code()}-$errorMsg")
                        Toast.makeText(
                            this@RegistroUsuarioActivity,
                            "Error: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("REGISTRO_EXCEPTION", e.localizedMessage ?: "Error desconocido", e)
                    Toast.makeText(
                        this@RegistroUsuarioActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}