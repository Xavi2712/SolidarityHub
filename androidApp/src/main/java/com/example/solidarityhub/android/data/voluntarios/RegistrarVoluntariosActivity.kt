package com.example.solidarityhub.android.data.voluntarios

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.solidarityhub.android.databinding.ActivityRegistrarVoluntariosBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import android.animation.ValueAnimator
import androidx.core.animation.doOnEnd
import com.example.solidarityhub.android.R
import com.example.solidarityhub.android.ui.voluntarios.RegistrarVoluntarioUbicacionActivity
import com.example.solidarityhub.android.util.SessionManager

class RegistrarVoluntariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarVoluntariosBinding
    private lateinit var sessionManager: SessionManager

    // Mapeo de tarjetas a categorías
    private val cardToCategory = mutableMapOf<MaterialCardView, String>()

    // Lista para almacenar categorías seleccionadas (ahora puede tener múltiples)
    private val selectedCategories = mutableSetOf<String>()

    // Días seleccionados por turno
    private val selectedMorningDays = mutableSetOf<String>()
    private val selectedAfternoonDays = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarVoluntariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        setupCategoryCards()
        setupTabLayout()
        setupDayChips()
        setupRegisterButton()
    }

    private fun setupCategoryCards() {
        // Mapear tarjetas a sus categorías
        cardToCategory[binding.cardLimpieza] = "Limpieza"
        cardToCategory[binding.cardMaterialPesado] = "Material Pesado"
        cardToCategory[binding.cardSuministros] = "Suministros"
        cardToCategory[binding.cardSanidad] = "Sanidad"
        cardToCategory[binding.cardHospedaje] = "Hospedaje"
        cardToCategory[binding.cardTransporte] = "Transporte"

        // Configurar click listeners para cada tarjeta
        for ((card, category) in cardToCategory) {
            card.setOnClickListener {
                toggleCardSelection(card, category)
            }
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // Turno Mañana
                        binding.chipGroupMorning.visibility = View.VISIBLE
                        binding.chipGroupAfternoon.visibility = View.GONE
                    }
                    1 -> { // Turno Tarde
                        binding.chipGroupMorning.visibility = View.GONE
                        binding.chipGroupAfternoon.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupDayChips() {
        val morningChips = mapOf(
            binding.chipLunesMorning to "LM",
            binding.chipMartesMorning to "MM",
            binding.chipMiercolesMorning to "XM",
            binding.chipJuevesMorning to "JM",
            binding.chipViernesMorning to "VM",
            binding.chipSabadoMorning to "SM",
            binding.chipDomingoMorning to "DM"
        )

        val afternoonChips = mapOf(
            binding.chipLunesAfternoon to "LT",
            binding.chipMartesAfternoon to "MT",
            binding.chipMiercolesAfternoon to "XT",
            binding.chipJuevesAfternoon to "JT",
            binding.chipViernesAfternoon to "VT",
            binding.chipSabadoAfternoon to "ST",
            binding.chipDomingoAfternoon to "DT"
        )

        morningChips.forEach { (chip, code) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateDaySelection(isChecked, code, selectedMorningDays)
            }
        }

        afternoonChips.forEach { (chip, code) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateDaySelection(isChecked, code, selectedAfternoonDays)
            }
        }
    }



    private fun updateDaySelection(isSelected: Boolean, day: String, dayList: MutableSet<String>) {
        if (isSelected) {
            dayList.add(day)
        } else {
            dayList.remove(day)
        }
        updateDaysSelectionSummary()
    }

    private var MaterialCardView.isChecked: Boolean
        get() = tag as? Boolean ?: false
        set(value) {
            tag = value
        }

    private fun toggleCardSelection(card: MaterialCardView, category: String) {
        // Invertir el estado de selección actual
        val newCheckedState = !card.isChecked

        // Actualizar la colección de categorías antes de cambiar el estado visual
        if (newCheckedState) {
            selectedCategories.add(category)
        } else {
            selectedCategories.remove(category)
        }

        // Guardar el nuevo estado
        card.isChecked = newCheckedState

        // Definir colores inicial y final para la animación
        val fromColor = card.cardBackgroundColor.defaultColor
        val toColor = if (newCheckedState)
            ContextCompat.getColor(this, R.color.blue_primary)
        else
            ContextCompat.getColor(this, R.color.white)

        // Animación de cambio de color de fondo
        animateCardBackgroundColor(card, fromColor, toColor)

        // Animación de cambio de ancho de borde
        val fromWidth = card.strokeWidth.toFloat()
        val toWidth = if (newCheckedState) 3f else 1f
        animateCardStrokeWidth(card, fromWidth, toWidth)
    }

    private fun animateCardBackgroundColor(card: MaterialCardView, fromColor: Int, toColor: Int) {
        val colorAnimation = ValueAnimator.ofArgb(fromColor, toColor)
        colorAnimation.duration = 300 // 300ms para la animación
        colorAnimation.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            card.setCardBackgroundColor(animatedColor)
        }
        colorAnimation.doOnEnd {
            // Asegurar que el color final sea exactamente el deseado
            card.setCardBackgroundColor(toColor)
        }
        colorAnimation.start()
    }

    private fun animateCardStrokeWidth(card: MaterialCardView, fromWidth: Float, toWidth: Float) {
        val strokeAnimation = ValueAnimator.ofFloat(fromWidth, toWidth)
        strokeAnimation.duration = 300 // 300ms para la animación
        strokeAnimation.addUpdateListener { animator ->
            val animatedWidth = (animator.animatedValue as Float).toInt()
            card.strokeWidth = animatedWidth
        }
        strokeAnimation.start()

        // Añadir animación de elevación
        val elevationFrom = if (toWidth > fromWidth) 2f else 8f
        val elevationTo = if (toWidth > fromWidth) 8f else 2f

        val elevationAnimation = ValueAnimator.ofFloat(elevationFrom, elevationTo)
        elevationAnimation.duration = 300
        elevationAnimation.addUpdateListener { animator ->
            val elevation = animator.animatedValue as Float
            card.elevation = elevation
        }
        elevationAnimation.start()
    }

    private fun updateDaysSelectionSummary() {
        val allSelectedDays = selectedMorningDays + selectedAfternoonDays

        if (allSelectedDays.isEmpty()) {
            binding.tvResumenDias.text = "Días seleccionados: Ninguno"
        } else {
            binding.tvResumenDias.text = "Días seleccionados: ${allSelectedDays.joinToString(", ")}"
        }
    }

    private fun setupRegisterButton() {
        binding.btnRegistrarVoluntario.setOnClickListener {
            if (validateSelections()) {
                // En lugar de registrar directamente, ahora guardamos en SessionManager y navegamos
                saveSelectionsAndNavigate()
            }
        }
    }

    private fun validateSelections(): Boolean {
        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona al menos una categoría de ayuda", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedMorningDays.isEmpty() && selectedAfternoonDays.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona al menos un día y turno disponible", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveSelectionsAndNavigate() {
        // Convertir conjuntos a ArrayList para guardar en SessionManager
        val capacidades = ArrayList<String>(selectedCategories)
        val diasDisponibles = ArrayList<String>(selectedMorningDays + selectedAfternoonDays)

        // Guardar en SessionManager
        sessionManager.saveTempCapacidades(capacidades)
        sessionManager.saveTempDiasDisponibles(diasDisponibles)

        Log.d("RegistroVoluntario", "Categorías: $capacidades")
        Log.d("RegistroVoluntario", "Días disponibles: $diasDisponibles")

        // Navegar a la pantalla de selección de ubicación
        val intent = Intent(this, RegistrarVoluntarioUbicacionActivity::class.java)
        startActivity(intent)
    }
}