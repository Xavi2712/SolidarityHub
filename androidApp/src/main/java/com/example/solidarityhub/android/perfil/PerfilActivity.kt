package com.example.solidarityhub.android.perfil

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.databinding.ActivityPerfilBinding
import com.example.solidarityhub.android.util.SessionManager
import com.example.solidarityhub.android.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var sessionManager: SessionManager
    
    // Variables para almacenar valores originales
    private var originalNombre = ""
    private var originalCorreo = ""
    private var originalTelefono = ""
    private var originalDireccion = ""
    
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        setupToolbar()
        loadUserData()
        setupTextWatchers()
        setupPasswordToggle()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Mi Perfil"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        binding.apply {
            // Cargar datos desde SessionManager
            val nombre = sessionManager.getUserName() ?: ""
            val correo = sessionManager.getUserEmail() ?: ""
            val telefono = sessionManager.getUserTelefono() ?: ""
            val direccion = sessionManager.getUserAddress() ?: ""
            val rol = sessionManager.getUserRole() ?: ""
            val dni = sessionManager.getDni() ?: ""
            
            // Mostrar datos no editables
            tvDniValue.text = dni
            tvRolValue.text = rol
            
            // Mostrar datos editables y guardar valores originales
            etNombre.setText(nombre)
            etCorreo.setText(correo)
            etTelefono.setText(telefono)
            etDireccion.setText(direccion)
            
            // Guardar valores originales
            originalNombre = nombre
            originalCorreo = correo
            originalTelefono = telefono
            originalDireccion = direccion
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkForChanges()
            }
        }
        
        binding.etNombre.addTextChangedListener(textWatcher)
        binding.etCorreo.addTextChangedListener(textWatcher)
        binding.etTelefono.addTextChangedListener(textWatcher)
        binding.etDireccion.addTextChangedListener(textWatcher)
        binding.etContrasena.addTextChangedListener(textWatcher)
    }

    private fun checkForChanges() {
        val hasChanges = 
            binding.etNombre.text.toString() != originalNombre ||
            binding.etCorreo.text.toString() != originalCorreo ||
            binding.etTelefono.text.toString() != originalTelefono ||
            binding.etDireccion.text.toString() != originalDireccion ||
            binding.etContrasena.text.toString().isNotEmpty()
        
        binding.btnGuardar.visibility = if (hasChanges) View.VISIBLE else View.GONE
    }

    private fun setupPasswordToggle() {
        binding.btnTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                binding.etContrasena.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.btnTogglePassword.text = "👁"
                isPasswordVisible = false
            } else {
                binding.etContrasena.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.btnTogglePassword.text = "👁‍🗨"
                isPasswordVisible = true
            }
            binding.etContrasena.setSelection(binding.etContrasena.text.length)
        }
    }

    private fun setupSaveButton() {
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            
            // Validaciones básicas
            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (telefono.isEmpty()) {
                Toast.makeText(this, "El teléfono no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            updateUserProfile(nombre, correo, telefono, direccion, contrasena)
        }
    }

    private fun updateUserProfile(nombre: String, correo: String, telefono: String, direccion: String, contrasena: String) {
        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "🔄 Guardando..."
        
        val dni = sessionManager.getDni() ?: ""
        val updateData = mutableMapOf(
            "dni" to dni,
            "nombre" to nombre,
            "correo" to correo,
            "telefono" to telefono,
            "direccion" to direccion
        )
        
        // Solo incluir contraseña si se proporcionó una nueva
        if (contrasena.isNotEmpty()) {
            updateData["contraseña"] = contrasena
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.usuarioApiService.actualizarUsuario(updateData)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        Toast.makeText(this@PerfilActivity, 
                            apiResponse?.message ?: "Perfil actualizado correctamente", 
                            Toast.LENGTH_SHORT).show()
                        
                        // Actualizar SessionManager con los nuevos datos
                        sessionManager.saveUserName(nombre)
                        sessionManager.saveUserEmail(correo)
                        sessionManager.saveUserTelefono(telefono)
                        sessionManager.saveUserLocation(
                            sessionManager.getUserLatitud(),
                            sessionManager.getUserLongitud(),
                            direccion
                        )
                        
                        // Actualizar valores originales
                        originalNombre = nombre
                        originalCorreo = correo
                        originalTelefono = telefono
                        originalDireccion = direccion
                        
                        // Limpiar campo de contraseña
                        binding.etContrasena.setText("")
                        
                        // Ocultar botón de guardar
                        binding.btnGuardar.visibility = View.GONE
                        
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Toast.makeText(this@PerfilActivity, 
                            "Error al actualizar: ${response.code()}", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, 
                        "Error de conexión: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnGuardar.isEnabled = true
                    binding.btnGuardar.text = "💾 Guardar Cambios"
                }
            }
        }
    }
} 