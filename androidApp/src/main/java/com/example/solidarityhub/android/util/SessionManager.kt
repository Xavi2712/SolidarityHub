package com.example.solidarityhub.android.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
        const val USER_DNI = "user_dni"
        const val USER_ROLE = "user_role"
    }

    // Guardar el token de autenticación
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    // Obtener el token de autenticación
    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    // Guardar DNI del usuario
    fun saveDni(dni: String) {
        val editor = prefs.edit()
        editor.putString(USER_DNI, dni)
        editor.apply()
    }

    // Obtener DNI del usuario
    fun getDni(): String? {
        return prefs.getString(USER_DNI, null)
    }

    fun saveUserRole(role: String) {
        val editor = prefs.edit()
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    // Obtener rol del usuario
    fun getUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }
    // Guardar y obtener el radio del voluntario
    fun saveVoluntarioRadius(radius: Int) {
        val editor = prefs.edit()
        editor.putInt("voluntario_radius", radius)
        editor.apply()
    }

    fun getVoluntarioRadius(): Int {
        return prefs.getInt("voluntario_radius", 5000) // 5km por defecto
    }
    // Guardar días disponibles temporalmente (para el proceso de registro)
    fun saveTempDiasDisponibles(dias: ArrayList<String>) {
        val editor = prefs.edit()
        editor.putStringSet("temp_dias_disp", dias.toSet())
        editor.apply()
    }

    // Obtener días disponibles temporales
    fun getTempDiasDisponibles(): ArrayList<String> {
        val diasSet = prefs.getStringSet("temp_dias_disp", setOf())
        return ArrayList(diasSet ?: setOf())
    }

    // Guardar capacidades temporalmente (para el proceso de registro)
    fun saveTempCapacidades(capacidades: ArrayList<String>) {
        val editor = prefs.edit()
        editor.putStringSet("temp_capacidades", capacidades.toSet())
        editor.apply()
    }

    // Obtener capacidades temporales
    fun getTempCapacidades(): ArrayList<String> {
        val capSet = prefs.getStringSet("temp_capacidades", setOf())
        return ArrayList(capSet ?: setOf())
    }

    // Limpiar datos temporales después de registro
    fun clearTempData() {
        val editor = prefs.edit()
        editor.remove("temp_dias_disp")
        editor.remove("temp_capacidades")
        editor.apply()
    }
    fun saveUserLocation(latitude: Double, longitude: Double, address: String) {
        val editor = prefs.edit()
        editor.putString("user_latitude", latitude.toString())
        editor.putString("user_longitude", longitude.toString())
        editor.putString("user_address", address)
        editor.apply()
    }

    // Obtener latitud guardada
    fun getUserLatitude(): Double? {
        val latStr = prefs.getString("user_latitude", null)
        return latStr?.toDoubleOrNull()
    }

    // Obtener longitud guardada
    fun getUserLongitude(): Double? {
        val lngStr = prefs.getString("user_longitude", null)
        return lngStr?.toDoubleOrNull()
    }
    // Método para cerrar sesión
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    // Guardar y obtener email del usuario
    fun saveUserEmail(email: String) {
        val editor = prefs.edit()
        editor.putString("user_email", email)
        editor.apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    // Guardar y obtener nombre del usuario
    fun saveUserName(name: String) {
        val editor = prefs.edit()
        editor.putString("user_name", name)
        editor.apply()
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    // Guardar y obtener teléfono del usuario
    fun saveUserTelefono(telefono: String) {
        val editor = prefs.edit()
        editor.putString("user_telefono", telefono)
        editor.apply()
    }

    fun getUserTelefono(): String? {
        return prefs.getString("user_telefono", null)
    }

    // Obtener latitud del usuario (renombrado desde getUserLatitude)
    fun getUserLatitud(): Double {
        val latStr = prefs.getString("user_latitude", "0.0")
        return latStr?.toDoubleOrNull() ?: 0.0
    }

    // Obtener longitud del usuario (renombrado desde getUserLongitude)
    fun getUserLongitud(): Double {
        val lngStr = prefs.getString("user_longitude", "0.0")
        return lngStr?.toDoubleOrNull() ?: 0.0
    }

    // Obtener dirección del usuario
    fun getUserDireccion(): String? {
        return prefs.getString("user_address", null)
    }
    // Obtener dirección guardada
    fun getUserAddress(): String? {
        return prefs.getString("user_address", null)
    }


    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}