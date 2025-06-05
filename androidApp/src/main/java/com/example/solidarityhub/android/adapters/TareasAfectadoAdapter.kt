package com.example.solidarityhub.android.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.solidarityhub.android.data.dto.TareaAfectadoDTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.data.remote.AsignarVoluntarioRequest
import com.example.solidarityhub.android.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TareasAfectadoAdapter(
    private var tareas: List<TareaAfectadoDTO>,
    private val onTareaAsignada: () -> Unit
) : RecyclerView.Adapter<TareasAfectadoAdapter.TareaViewHolder>() {

    private var context: android.content.Context? = null

    class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEstadoIcon: TextView by lazy { itemView.findViewById(android.R.id.text1) }
        val tvTitulo: TextView by lazy { itemView.findViewById(android.R.id.text2) }
        val tvEstado: TextView by lazy { itemView.findViewById(android.R.id.button1) }
        val tvDescripcion: TextView by lazy { itemView.findViewById(android.R.id.summary) }
        val tvFecha: TextView by lazy { itemView.findViewById(android.R.id.message) }
        val tvVoluntario: TextView by lazy { itemView.findViewById(android.R.id.title) }
        val btnMeLaPido: Button by lazy { itemView.findViewById(android.R.id.button2) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        context = parent.context
        val itemView = createItemView(parent)
        return TareaViewHolder(itemView)
    }

    private fun createItemView(parent: ViewGroup): View {
        val context = parent.context
        
        // Crear un CardView programáticamente
        val cardView = androidx.cardview.widget.CardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
            radius = 12f
            cardElevation = 2f
            useCompatPadding = true
        }
        
        // Crear LinearLayout principal
        val mainLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Header layout
        val headerLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        
        // Icono de estado
        val estadoIcon = TextView(context).apply {
            id = android.R.id.text1
            text = "🔄"
            textSize = 18f
            setPadding(0, 0, 8, 0)
        }
        
        // Título
        val titulo = TextView(context).apply {
            id = android.R.id.text2
            text = "Título de la tarea"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#333333"))
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        // Estado
        val estado = TextView(context).apply {
            id = android.R.id.button1
            text = "Estado"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#1976D2"))
            setPadding(6, 6, 6, 6)
        }
        
        headerLayout.addView(estadoIcon)
        headerLayout.addView(titulo)
        headerLayout.addView(estado)
        
        // Descripción
        val descripcion = TextView(context).apply {
            id = android.R.id.summary
            text = "Descripción de la tarea"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            setPadding(0, 8, 0, 8)
            maxLines = 2
        }
        
        // Layout inferior
        val bottomLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }
        
        // Fecha
        val fecha = TextView(context).apply {
            id = android.R.id.message
            text = "📅 Fecha"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#999999"))
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        // Voluntario
        val voluntario = TextView(context).apply {
            id = android.R.id.title
            text = "👤 Voluntario"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#999999"))
        }
        
        bottomLayout.addView(fecha)
        bottomLayout.addView(voluntario)
        
        // Botón "Me la pido"
        val btnMeLaPido = Button(context).apply {
            id = android.R.id.button2
            text = "🤝 Me la pido"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            
            // Fondo verde moderno
            val background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#4CAF50"))
                cornerRadius = 25f
            }
            setBackground(background)
            
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
            
            setPadding(16, 12, 16, 12)
            visibility = View.GONE // Oculto por defecto
        }
        
        // Agregar todo al layout principal
        mainLayout.addView(headerLayout)
        mainLayout.addView(descripcion)
        mainLayout.addView(bottomLayout)
        mainLayout.addView(btnMeLaPido)
        
        cardView.addView(mainLayout)
        return cardView
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareas[position]
        
        holder.tvTitulo.text = tarea.titulo
        holder.tvDescripcion.text = tarea.descripcion
        holder.tvFecha.text = "📅 ${tarea.fecha}"
        holder.tvVoluntario.text = "👤 ${tarea.voluntarioNombre}"
        
        // Configurar estado y colores
        when (tarea.estado.lowercase()) {
            "pendiente" -> {
                holder.tvEstado.text = "Pendiente"
                holder.tvEstadoIcon.text = "⏳"
                holder.tvEstado.setBackgroundColor(Color.parseColor("#FF9800"))
                // Mostrar botón "Me la pido" solo para tareas pendientes
                holder.btnMeLaPido.visibility = View.VISIBLE
                holder.btnMeLaPido.setOnClickListener {
                    asignarTarea(tarea.id)
                }
            }
            "en progreso", "en curso" -> {
                holder.tvEstado.text = "En Progreso"
                holder.tvEstadoIcon.text = "🔄"
                holder.tvEstado.setBackgroundColor(Color.parseColor("#2196F3"))
                holder.btnMeLaPido.visibility = View.GONE
            }
            "completada", "finalizada" -> {
                holder.tvEstado.text = "Completada"
                holder.tvEstadoIcon.text = "✅"
                holder.tvEstado.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.btnMeLaPido.visibility = View.GONE
            }
            "cancelada" -> {
                holder.tvEstado.text = "Cancelada"
                holder.tvEstadoIcon.text = "❌"
                holder.tvEstado.setBackgroundColor(Color.parseColor("#F44336"))
                holder.btnMeLaPido.visibility = View.GONE
            }
            else -> {
                holder.tvEstado.text = tarea.estado
                holder.tvEstadoIcon.text = "📋"
                holder.tvEstado.setBackgroundColor(Color.parseColor("#757575"))
                holder.btnMeLaPido.visibility = View.GONE
            }
        }
    }

    private fun asignarTarea(tareaId: Int) {
        val context = context ?: return
        val sessionManager = SessionManager(context)
        val dniVoluntario = sessionManager.getDni()

        if (dniVoluntario.isNullOrEmpty()) {
            Toast.makeText(context, "Error: No se pudo obtener tu DNI", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.tareaApiService.asignarVoluntario(
                    AsignarVoluntarioRequest(tareaId, dniVoluntario)
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "✅ ¡Tarea asignada con éxito!", Toast.LENGTH_SHORT).show()
                        onTareaAsignada() // Notificar que se asignó la tarea
                    } else {
                        val errorMessage = when (response.code()) {
                            400 -> "La tarea ya está asignada"
                            404 -> "Tarea no encontrada"
                            else -> "Error al asignar tarea (${response.code()})"
                        }
                        Toast.makeText(context, "❌ $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = tareas.size

    fun updateTareas(nuevasTareas: List<TareaAfectadoDTO>) {
        tareas = nuevasTareas
        notifyDataSetChanged()
    }
} 