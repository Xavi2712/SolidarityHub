package com.example.solidarityhub.android.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("SolidarityHubPrefs", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val KEY_DNI = "user_dni"
        const val KEY_NOMBRE = "user_name"
        const val KEY_ROL = "user_role"
        const val KEY_TOKEN = "auth_token"
    }

    // Guardar DNI
    fun saveDni(dni: String) {
        editor.putString(KEY_DNI, dni)
        editor.apply()
    }

    // Obtener DNI
    fun getDni(): String? {
        return prefs.getString(KEY_DNI, null)
    }

    // Guardar nombre de usuario
    fun saveUserName(userName: String) {
        editor.putString(KEY_NOMBRE, userName)
        editor.apply()
    }

    // Obtener nombre de usuario
    fun getUserName(): String? {
        return prefs.getString(KEY_NOMBRE, null)
    }

    // Guardar rol
    fun saveRole(role: String) {
        editor.putString(KEY_ROL, role)
        editor.apply()
    }

    // Obtener rol
    fun getRole(): String? {
        return prefs.getString(KEY_ROL, null)
    }

    // Guardar token de autenticación
    fun saveAuthToken(token: String) {
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    // Obtener token
    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // Limpiar todos los datos de sesión
    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}