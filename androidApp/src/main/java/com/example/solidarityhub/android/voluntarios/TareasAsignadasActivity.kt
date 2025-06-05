package com.example.solidarityhub.android.voluntarios

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.solidarityhub.android.databinding.ActivityTareasAsignadasBinding
import com.example.solidarityhub.android.util.SessionManager
import com.example.solidarityhub.android.data.dto.TareaDTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TareasAsignadasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTareasAsignadasBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var tareasAdapter: TareasAsignadasAdapter
    private val tareasList = mutableListOf<TareaAsignada>()
    private var isLoading = false
    private var isManualReload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTareasAsignadasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupEmptyState()
        setupReloadButton()
        
        // Cargar tareas reales desde el endpoint (carga inicial)
        loadTareasAsignadas()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Tareas Asignadas"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        tareasAdapter = TareasAsignadasAdapter(
            tareasList,
            onTareaClick = { tarea -> onTareaClick(tarea) },
            onTareaFinalizada = { cargarTareas() }
        )
        
        binding.recyclerViewTareas.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TareasAsignadasActivity)
            adapter = tareasAdapter
        }
    }

    private fun setupEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerViewTareas.visibility = View.GONE
    }

    private fun setupReloadButton() {
        binding.fabReload.setOnClickListener {
            if (!isLoading) {
                isManualReload = true
                loadTareasAsignadas()
                Toast.makeText(this, "🔄 Recargando tareas...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTareasAsignadas() {
        val dni = sessionManager.getDni()
        if (dni.isNullOrEmpty()) {
            Toast.makeText(this, "Error: DNI del usuario no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar indicador de carga
        isLoading = true
        binding.fabReload.hide()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.tareaApiService.getTareasVoluntario(dni)
                
                withContext(Dispatchers.Main) {
                    isLoading = false
                    binding.fabReload.show()
                    
                    if (response.isSuccessful) {
                        val tareasDTO = response.body()
                        tareasDTO?.let { listaTareasDTO ->
                            val tareasAsignadas = listaTareasDTO.map { dto -> mapTareaDTOToTareaAsignada(dto) }
                            
                            tareasList.clear()
                            tareasList.addAll(tareasAsignadas)
                            
                            if (tareasList.isEmpty()) {
                                binding.layoutEmptyState.visibility = View.VISIBLE
                                binding.recyclerViewTareas.visibility = View.GONE
                            } else {
                                binding.layoutEmptyState.visibility = View.GONE
                                binding.recyclerViewTareas.visibility = View.VISIBLE
                                tareasAdapter.notifyDataSetChanged()
                            }
                            
                            Log.d("TareasAsignadas", "Tareas cargadas exitosamente: ${tareasList.size}")
                            
                            // Mostrar mensaje de éxito solo si es una recarga manual
                            if (isManualReload) {
                                val mensaje = if (tareasList.isEmpty()) {
                                    "✅ No hay tareas nuevas asignadas"
                                } else {
                                    "✅ Tareas actualizadas: ${tareasList.size} encontradas"
                                }
                                Toast.makeText(this@TareasAsignadasActivity, mensaje, Toast.LENGTH_SHORT).show()
                                isManualReload = false
                            }
                        }
                    } else {
                        val errorMsg = "Error al cargar tareas: ${response.code()}"
                        Toast.makeText(this@TareasAsignadasActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        Log.e("TareasAsignadas", errorMsg)
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    binding.fabReload.show()
                    
                    val errorMsg = "❌ Error de conexión: ${e.message}"
                    Toast.makeText(this@TareasAsignadasActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("TareasAsignadas", "Error al cargar tareas", e)
                    
                    if (isManualReload) {
                        isManualReload = false
                    }
                }
            }
        }
    }

    private fun mapTareaDTOToTareaAsignada(dto: TareaDTO): TareaAsignada {
        // Mapear estado del endpoint a nuestro enum
        val estado = when (dto.estado.lowercase()) {
            "en curso" -> EstadoTarea.EN_PROGRESO
            "en_progreso" -> EstadoTarea.EN_PROGRESO
            "finalizada" -> EstadoTarea.COMPLETADA
            "completada" -> EstadoTarea.COMPLETADA
            "cancelada" -> EstadoTarea.CANCELADA
            else -> EstadoTarea.PENDIENTE
        }
        
        // Formatear fecha para mostrar
        val fechaFormateada = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dto.fechaInicio)
            outputFormat.format(date ?: dto.fechaInicio)
        } catch (e: Exception) {
            dto.fechaInicio.substring(0, 10) // Fallback: solo la fecha
        }
        
        return TareaAsignada(
            id = dto.id.toString(),
            titulo = dto.nombre,
            descripcion = dto.descripcion,
            afectadoNombre = dto.afectadoNombre,
            ubicacion = "Tel: ${dto.afectadoTelefono}", // Usar teléfono como ubicación por ahora
            estado = estado,
            fechaAsignacion = fechaFormateada
        )
    }

    private fun onTareaClick(tarea: TareaAsignada) {
        // TODO: Implementar navegación a detalles de la tarea
        Toast.makeText(this, "Tarea seleccionada: ${tarea.titulo}", Toast.LENGTH_SHORT).show()
    }

    private fun cargarTareas() {
        // Implementa la lógica para cargar tareas nuevamente
        loadTareasAsignadas()
    }
}

// Clases de datos para las tareas
data class TareaAsignada(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val afectadoNombre: String,
    val ubicacion: String,
    val estado: EstadoTarea,
    val fechaAsignacion: String
)

enum class EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA,
    CANCELADA
} 