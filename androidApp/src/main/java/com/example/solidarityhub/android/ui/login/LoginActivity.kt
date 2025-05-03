package com.example.solidarityhub.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.databinding.ActivityLoginBinding
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.util.SessionManager
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
                val response = RetrofitClient.usuarioApiService.loginUsuario(loginData)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()

                        // Guardar información de sesión
                        sessionManager.saveDni(dni)
                        loginResponse?.token?.let { sessionManager.saveAuthToken(it) }
                        loginResponse?.userName?.let { sessionManager.saveUserName(it) }

                        Toast.makeText(
                            this@LoginActivity,
                            "Inicio de sesión exitoso",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navegar a la pantalla de selección de rol
                        val intent = Intent(this@LoginActivity, SeleccionRolActivity::class.java)
                        loginResponse?.userName?.let {
                            intent.putExtra("nombreUsuario", it)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Log.e("LOGIN_ERROR", "${response.code()}-$errorMsg")
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: Credenciales incorrectas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LOGIN_EXCEPTION", e.localizedMessage ?: "Error desconocido", e)
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