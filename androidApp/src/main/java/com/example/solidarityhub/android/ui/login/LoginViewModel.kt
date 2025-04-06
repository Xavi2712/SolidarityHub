package com.example.solidarityhub.android.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.solidarityhub.android.data.LoginRepository
import com.example.solidarityhub.android.data.Result

import com.example.solidarityhub.android.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {



        private val _capacidadesSeleccionadas = MutableLiveData<Map<Int, Boolean>>().apply {
            value = mapOf(
                R.id.Limpieza to false,
                R.id.Transporte to false,
                R.id.Reparaciones to false,
                R.id.Busqueda to false,
                R.id.Alojamiento to false
            )
        }
        val capacidadesSeleccionadas: LiveData<Map<Int, Boolean>> = _capacidadesSeleccionadas


        fun cambioSeleccion(chipId: Int) {
            val current = _capacidadesSeleccionadas.value ?: emptyMap()
            _capacidadesSeleccionadas.value = current.toMutableMap().apply {
                this[chipId] = !(this[chipId] ?: false)
            }
        }


    fun getCapacidadesIds(): List<Int> {
        return _capacidadesSeleccionadas.value?.filter { it.value }?.keys?.toList() ?: emptyList()
    }
    }



