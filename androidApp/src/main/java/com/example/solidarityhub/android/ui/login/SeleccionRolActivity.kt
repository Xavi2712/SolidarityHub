package com.example.solidarityhub.android.ui.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.solidarityhub.android.afectados.RegistrarAfectadosActivity
import com.example.solidarityhub.android.databinding.ActivitySeleccionRolBinding
import com.example.solidarityhub.android.data.voluntarios.RegistrarVoluntariosActivity
import kotlin.math.hypot
import kotlin.math.max

class SeleccionRolActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionRolBinding
    private var expandableBackgroundView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionRolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Crear una capa de animación que estará por encima de todos los elementos
        createExpandableBackgroundView()

        // Obtener el nombre del usuario del intent si lo enviaste
        val nombreUsuario = intent.getStringExtra("nombreUsuario")
        if (!nombreUsuario.isNullOrEmpty()) {
            binding.tvBienvenida.text = "Bienvenido, $nombreUsuario"
        }

        // Configurar click en la sección Voluntario
        binding.cvVoluntario.setOnClickListener {
            animateAndProceed(it, "#2196F3") {
                navigateToRegistrarVoluntarios()
            }
        }

        // Configurar click en la sección Afectado
        binding.cvAfectado.setOnClickListener {
            animateAndProceed(it, "#E91E63") {
                navigateToRegistrarAfectados()
            }
        }
    }

    private fun createExpandableBackgroundView() {
        // Crear una nueva vista que se usará para la animación
        expandableBackgroundView = View(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            elevation = 1000f // Valor alto para asegurar que esté por encima
        }

        // Añadir la vista al contenedor raíz de la actividad
        val rootView = window.decorView.findViewById<View>(android.R.id.content) as FrameLayout
        rootView.addView(expandableBackgroundView)
    }

    private fun animateAndProceed(view: View, colorCode: String, action: () -> Unit) {
        val backgroundView = expandableBackgroundView ?: return

        prepareBackground(backgroundView, colorCode)
        val (centerX, centerY) = view.centerOnScreen()
        val endRadius = calculateMaxRadius(centerX, centerY)

        createCircularRevealAnimation(backgroundView, centerX, centerY, endRadius) {
            action()
        }.start()
    }

    private fun prepareBackground(view: View, colorCode: String) {
        view.setBackgroundColor(Color.parseColor(colorCode))
        view.visibility = View.VISIBLE
    }

    private fun View.centerOnScreen(): Pair<Int, Int> {
        val location = IntArray(2)
        getLocationInWindow(location)
        val centerX = location[0] + width / 2
        val centerY = location[1] + height / 2
        return Pair(centerX, centerY)
    }

    private fun calculateMaxRadius(centerX: Int, centerY: Int): Float {
        val width = binding.root.width
        val height = binding.root.height
        return hypot(
            max(centerX, width - centerX).toDouble(),
            max(centerY, height - centerY).toDouble()
        ).toFloat()
    }

    private fun createCircularRevealAnimation(
        view: View,
        centerX: Int,
        centerY: Int,
        endRadius: Float,
        onEnd: () -> Unit
    ): Animator {
        return ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0f, endRadius).apply {
            duration = 500
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd()
                }
            })
        }
    }

    private fun navigateToRegistrarVoluntarios() {
        val intent = Intent(this, RegistrarVoluntariosActivity::class.java)
        startActivity(intent)
        // No llamamos a finish() para mantener esta actividad en la pila
    }

    private fun navigateToRegistrarAfectados() {
        val intent = Intent(this, RegistrarAfectadosActivity::class.java)
        startActivity(intent)
        // No llamamos a finish() para mantener esta actividad en la pila
    }

    override fun onResume() {
        super.onResume()

        // Ocultar la vista de expansión cuando se regresa a esta actividad
        expandableBackgroundView?.visibility = View.GONE

        // Restaurar la visibilidad de los elementos principales
        binding.cvVoluntario.visibility = View.VISIBLE
        binding.cvAfectado.visibility = View.VISIBLE
        binding.tvBienvenida.visibility = View.VISIBLE
        binding.tvSeleccionRol.visibility = View.VISIBLE
    }
}