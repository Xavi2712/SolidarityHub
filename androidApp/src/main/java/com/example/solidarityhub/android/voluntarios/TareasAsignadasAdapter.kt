package com.example.solidarityhub.android.voluntarios

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.solidarityhub.R
import com.example.solidarityhub.android.databinding.ItemTareaAsignadaBinding
import com.example.solidarityhub.android.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.AlertDialog

class TareasAsignadasAdapter(
    private val tareas: List<TareaAsignada>,
    private val onTareaClick: (TareaAsignada) -> Unit,
    private val onTareaFinalizada: () -> Unit
) : RecyclerView.Adapter<TareasAsignadasAdapter.TareaViewHolder>() {

    inner class TareaViewHolder(private val binding: ItemTareaAsignadaBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(tarea: TareaAsignada) {
            binding.apply {
                tvTituloTarea.text = tarea.titulo
                tvDescripcionTarea.text = tarea.descripcion
                tvAfectadoNombre.text = "Solicitado por: ${tarea.afectadoNombre}"
                tvUbicacion.text = tarea.ubicacion
                tvFechaAsignacion.text = "Fecha: ${tarea.fechaAsignacion}"
                
                // Configurar estado
                tvEstado.text = when (tarea.estado) {
                    EstadoTarea.PENDIENTE -> "Pendiente"
                    EstadoTarea.EN_PROGRESO -> "En curso"
                    EstadoTarea.COMPLETADA -> "Completada"
                    EstadoTarea.CANCELADA -> "Cancelada"
                }
                
                // Configurar color del estado usando colores directos
                val estadoColor = when (tarea.estado) {
                    EstadoTarea.PENDIENTE -> Color.parseColor("#FF9800") // Naranja
                    EstadoTarea.EN_PROGRESO -> Color.parseColor("#2196F3") // Azul
                    EstadoTarea.COMPLETADA -> Color.parseColor("#4CAF50") // Verde
                    EstadoTarea.CANCELADA -> Color.parseColor("#F44336") // Rojo
                }
                tvEstado.setTextColor(estadoColor)

                // Configurar click en el item
                root.setOnClickListener {
                    if (tarea.estado != EstadoTarea.COMPLETADA) {
                        mostrarDialogoFinalizarTarea(tarea)
                    } else {
                        onTareaClick(tarea)
                    }
                }
            }
        }

        private fun mostrarDialogoFinalizarTarea(tarea: TareaAsignada) {
            val context = binding.root.context
            AlertDialog.Builder(context)
                .setTitle("Finalizar Tarea")
                .setMessage("¿Estás seguro de que quieres marcar esta tarea como completada?")
                .setPositiveButton("Sí, finalizar") { _, _ ->
                    finalizarTarea(tarea.id.toInt())
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        private fun finalizarTarea(tareaId: Int) {
            val context = binding.root.context
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.tareaApiService.finalizarTarea(tareaId)
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "✅ Tarea finalizada con éxito", Toast.LENGTH_SHORT).show()
                            onTareaFinalizada() // Notificar para recargar la lista
                        } else {
                            val errorMessage = when (response.code()) {
                                404 -> "Tarea no encontrada"
                                else -> "Error al finalizar tarea (${response.code()})"
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
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ItemTareaAsignadaBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return TareaViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.bind(tareas[position])
    }
    
    override fun getItemCount(): Int = tareas.size
} 