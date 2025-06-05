package com.example.solidarityhub.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.databinding.ActivityLoginBinding
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.data.remote.ApiResponse
import com.example.solidarityhub.android.data.remote.UsuarioResponse
import com.example.solidarityhub.android.util.SessionManager
import com.example.solidarityhub.android.inicio.Menu_activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar sessionManager
        sessionManager = SessionManager(this)

        // Configurar el botón de inicio de sesión
        binding.btnLogin.setOnClickListener {
            val dni = binding.etDni.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (dni.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Realizar inicio de sesión
            loginUser(dni, password)
        }

        // Configurar el botón de registro
        binding.btnRegister.setOnClickListener {
            // Navegar a la pantalla de registro
            val intent = Intent(this, RegistroUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(dni: String, password: String) {
        val loginData = mapOf("dni" to dni, "contraseña" to password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("LOGIN_REQUEST", "Enviando datos: $loginData")
                val response = RetrofitClient.usuarioApiService.loginUsuario(loginData)
                
                withContext(Dispatchers.Main) {
                    Log.d("LOGIN_RESPONSE", "Código de respuesta: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        Log.d("LOGIN_RESPONSE", "Respuesta completa: $apiResponse")
                        
                        val usuario = apiResponse?.usuario
                        Log.d("LOGIN_RESPONSE", "Usuario recibido: $usuario")

                        if (usuario != null) {
                            // Guardar información de sesión
                            sessionManager.saveDni(usuario.dni)
                            sessionManager.saveUserName(usuario.nombre)
                            sessionManager.saveUserEmail(usuario.correo)
                            sessionManager.saveUserTelefono(usuario.telefono)
                            
                            // Guardar ubicación del usuario si está disponible
                            if (usuario.latitud != null && usuario.longitud != null) {
                                val direccion = usuario.direccion ?: "Ubicación del usuario"
                                sessionManager.saveUserLocation(usuario.latitud, usuario.longitud, direccion)
                                Log.d("LOGIN_LOCATION", "Ubicación guardada: Lat=${usuario.latitud}, Lng=${usuario.longitud}, Dir=$direccion")
                            } else {
                                Log.d("LOGIN_LOCATION", "No se recibieron datos de ubicación")
                            }

                            Toast.makeText(
                                this@LoginActivity,
                                apiResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            // Verificar el rol del usuario
                            when (usuario.rol?.lowercase()) {
                                "voluntario", "afectado" -> {
                                    // Si tiene rol de voluntario o afectado, ir al menú principal
                                    sessionManager.saveUserRole(usuario.rol)
                                    Log.d("LOGIN_SUCCESS", "Redirigiendo al menú principal con rol: ${usuario.rol}")
                                    val intent = Intent(this@LoginActivity, Menu_activity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                "usuario", null -> {
                                    // Si es usuario o no tiene rol, ir a la pantalla de selección de rol
                                    Log.d("LOGIN_SUCCESS", "Usuario sin rol específico, redirigiendo a selección de rol")
                                    val intent = Intent(this@LoginActivity, SeleccionRolActivity::class.java)
                                    intent.putExtra("nombreUsuario", usuario.nombre)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            Log.e("LOGIN_ERROR", "Usuario es null en la respuesta")
                            Toast.makeText(
                                this@LoginActivity,
                                "Error: No se recibió información del usuario",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Log.e("LOGIN_ERROR", "Error ${response.code()}: $errorMsg")
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: Credenciales incorrectas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LOGIN_ERROR", "Error de conexión: ${e.message}", e)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}