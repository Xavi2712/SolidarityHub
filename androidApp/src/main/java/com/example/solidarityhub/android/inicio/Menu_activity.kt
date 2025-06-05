package com.example.solidarityhub.android.inicio

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.solidarityhub.R
import com.example.solidarityhub.android.databinding.ActivityMenuBinding
import com.example.solidarityhub.android.util.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent
import com.example.solidarityhub.android.Necesidad.AñadirNecesidadActivity
import com.example.solidarityhub.android.afectados.RegistrarAfectadosActivity
import com.example.solidarityhub.android.data.dto.AfectadoADTO
import com.example.solidarityhub.android.data.remote.RetrofitClient
import com.example.solidarityhub.android.ui.login.LoginActivity
import com.example.solidarityhub.android.data.voluntarios.RegistrarVoluntariosActivity
import com.example.solidarityhub.android.voluntarios.TareasAsignadasActivity
import com.example.solidarityhub.android.perfil.PerfilActivity
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ImageView
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.example.solidarityhub.android.util.PolylineUtils
import com.example.solidarityhub.android.data.dto.TareaAfectadoApiDTO
import java.text.SimpleDateFormat
import java.util.Locale

class Menu_activity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var mMap: GoogleMap
    private lateinit var sessionManager: SessionManager
    private val afectadosList = mutableListOf<AfectadoADTO>()
    private var currentRoutePolyline: Polyline? = null // Para guardar la ruta actual

    // API Key de Google Maps desde recursos
    private val googleMapsApiKey: String by lazy {
        try {
            val resourceId = resources.getIdentifier("google_maps_api_key", "string", packageName)
            if (resourceId != 0) {
                resources.getString(resourceId)
            } else {
                "NO_CONFIGURADO"
            }
        } catch (e: Exception) {
            "NO_CONFIGURADO"
        }
    }

    // Función para verificar si el API key está configurado correctamente
    private fun isApiKeyConfigured(): Boolean {
        return googleMapsApiKey != "NO_CONFIGURADO" && 
               googleMapsApiKey != "AQUI_PEGA_TU_API_KEY_REAL" && 
               googleMapsApiKey != "TU_API_KEY_AQUI" && 
               googleMapsApiKey.length > 20 // Un API key real tiene más de 20 caracteres
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Verificar si el usuario tiene un rol asignado
        val userRole = sessionManager.getUserRole()
        if (userRole.isNullOrEmpty()) {
            // Si no tiene rol, redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Configurar el mapa - buscar el fragmento directamente en el layout
        val mapFragment = supportFragmentManager.fragments.find { it is SupportMapFragment } as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Configurar el DrawerLayout
        setupDrawerLayout()

        // Configurar botones del menú
        setupMenuButtons()

        // Configurar datos de usuario en el menú
        setupUserData()

        // Configurar botones según el rol del usuario
        setupRoleSpecificButtons(userRole)

        // Cargar afectados para todos los usuarios (tanto usuarios como afectados pueden ver las ubicaciones)
        cargarAfectados()
    }

    private fun setupRoleSpecificButtons(userRole: String) {
        // Ocultar o mostrar botones según el rol
        when (userRole.lowercase()) {
            "voluntario" -> {
                binding.btnRegistrarNecesidad.visibility = View.GONE
                binding.btnTareasAsignadas.visibility = View.VISIBLE
                binding.btnMensajes.visibility = View.GONE
            }
            "afectado" -> {
                // Los afectados pueden registrar necesidades
                binding.btnRegistrarNecesidad.visibility = View.VISIBLE
                binding.btnRegistrarNecesidad.isEnabled = true
                binding.btnTareasAsignadas.visibility = View.GONE
                binding.btnMensajes.visibility = View.VISIBLE
            }
            else -> {
                // Para cualquier otro rol, ocultar el botón de necesidades y tareas
                binding.btnRegistrarNecesidad.visibility = View.GONE
                binding.btnTareasAsignadas.visibility = View.GONE
                binding.btnMensajes.visibility = View.VISIBLE
            }
        }
    }

    private fun setupDrawerLayout() {
        // Configurar botón hamburguesa para abrir el menú
        binding.btnMenuHamburguesa.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Configurar botón de centrar en ubicación
        binding.fabCenter.setOnClickListener {
            // Limpiar ruta actual si existe
            cancelarRutaActual()
            centerMapOnUserLocation()
        }
        
        // Configurar botón de cancelar ruta
        binding.btnCancelarRuta.setOnClickListener {
            cancelarRutaActual()
            Toast.makeText(this, "Ruta cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMenuButtons() {
        // Configurar botón de perfil
        binding.btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de mensajes
        binding.btnMensajes.setOnClickListener {
            Toast.makeText(this, "Mensajes seleccionados", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de tareas asignadas (para voluntarios)
        binding.btnTareasAsignadas.setOnClickListener {
            val intent = Intent(this, TareasAsignadasActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de registrar necesidad
        binding.btnRegistrarNecesidad.setOnClickListener {
            // Verificar que el usuario sea afectado antes de permitir el registro
            if (sessionManager.getUserRole()?.lowercase() == "afectado") {
                val intent = Intent(this, AñadirNecesidadActivity::class.java)
                startActivity(intent)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                Toast.makeText(this, "Solo los afectados pueden registrar necesidades", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar botón de ajustes
        binding.btnAjustes.setOnClickListener {
            Toast.makeText(this, "Ajustes seleccionados", Toast.LENGTH_SHORT).show()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            // Limpiar datos de sesión
            sessionManager.logout()

            // Redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupUserData() {
        // Mostrar nombre de usuario y email desde SessionManager
        binding.txtUserName.text = sessionManager.getUserName() ?: "Usuario"
        binding.txtUserEmail.text = sessionManager.getUserEmail() ?: "usuario@email.com"
    }

    private fun cargarAfectados() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Usar directamente el afectadoApiService del RetrofitClient
                val response = RetrofitClient.afectadoApiService.getAllAfectados()
                
                if (response.isSuccessful) {
                    val afectadosListFromApi = response.body()
                    afectadosListFromApi?.let { lista ->
                        this@Menu_activity.afectadosList.clear()
                        this@Menu_activity.afectadosList.addAll(lista)
                        
                        withContext(Dispatchers.Main) {
                            mostrarAfectadosEnMapa()
                        }
                        
                        Log.d("Menu_activity", "Afectados cargados exitosamente: ${lista.size}")
                    }
                } else {
                    Log.e("Menu_activity", "Error al cargar afectados: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Menu_activity, "Error al cargar ubicaciones de afectados: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("Menu_activity", "Error al cargar afectados", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Menu_activity, "Error de conexión al cargar ubicaciones de afectados", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarAfectadosEnMapa() {
        // Configurar InfoWindow personalizado
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // Usar el contenido personalizado con el fondo por defecto
            }

            override fun getInfoContents(marker: Marker): View? {
                // Solo personalizar para marcadores de afectados
                val afectado = afectadosList.find { 
                    marker.position.latitude == it.latitud && marker.position.longitude == it.longitud 
                }
                
                return if (afectado != null) {
                    createCustomInfoWindow(afectado)
                } else {
                    null // Usar InfoWindow por defecto para otros marcadores
                }
            }
        })

        // Configurar click en InfoWindow
        mMap.setOnInfoWindowClickListener { marker ->
            val afectado = afectadosList.find { 
                marker.position.latitude == it.latitud && marker.position.longitude == it.longitud 
            }
            
            if (afectado != null) {
                mostrarModalTareas(afectado)
            }
        }

        afectadosList.forEach { afectado ->
            // Usar las coordenadas reales del afectado
            val ubicacion = LatLng(afectado.latitud, afectado.longitud)
            
            mMap.addMarker(
                MarkerOptions()
                    .position(ubicacion)
                    .title("🆘 ${afectado.nombre}")
                    .snippet("Persona afectada que necesita ayuda")
                    .icon(createCustomMarkerIcon())
            )
        }
        
        Log.d("Menu_activity", "Marcadores de afectados añadidos al mapa: ${afectadosList.size}")
    }

    private fun mostrarModalTareas(afectado: AfectadoADTO) {
        Log.d("Menu_activity", "=== MOSTRANDO MODAL DE TAREAS ===")
        Log.d("Menu_activity", "Afectado seleccionado:")
        Log.d("Menu_activity", "  - Nombre: '${afectado.nombre}'")
        Log.d("Menu_activity", "  - DNI: '${afectado.dni}'")
        Log.d("Menu_activity", "  - Teléfono: '${afectado.telefono}'")
        Log.d("Menu_activity", "  - Dirección: '${afectado.direccion}'")
        
        // Crear un Dialog normal en lugar de BottomSheetDialog
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        
        // Crear vista del modal programáticamente pasando la referencia del dialog
        val modalView = createModalView(afectado, dialog)
        
        dialog.setContentView(modalView)
        
        // Configurar el dialog para que aparezca en el centro
        val window = dialog.window
        window?.let {
            it.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% del ancho de pantalla
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.setGravity(android.view.Gravity.CENTER)
            it.setBackgroundDrawableResource(android.R.color.transparent) // Fondo transparente
        }
        
        // Hacer que se pueda cerrar tocando fuera
        dialog.setCanceledOnTouchOutside(true)
        
        dialog.show()
        Log.d("Menu_activity", "Modal mostrado correctamente")
    }
    
    private fun createModalView(afectado: AfectadoADTO, dialog: android.app.Dialog? = null): View {
        val context = this
        
        // Layout principal
        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            
            // Fondo redondeado
            val background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.WHITE)
                cornerRadius = 20f
            }
            setBackground(background)
        }
        
        // Header
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 16)
        }
        
        val emojiText = TextView(context).apply {
            text = "📋"
            textSize = 24f
            setPadding(0, 0, 12, 0)
        }
        
        val titleLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        val titleText = TextView(context).apply {
            text = "Tareas Asignadas"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#1976D2"))
        }
        
        val nameText = TextView(context).apply {
            text = "Para: ${afectado.nombre}"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#666666"))
        }
        
        titleLayout.addView(titleText)
        titleLayout.addView(nameText)
        
        val closeButton = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setPadding(4, 4, 4, 4)
            background = android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#20000000")),
                null,
                null
            )
            layoutParams = LinearLayout.LayoutParams(32, 32)
            setOnClickListener {
                // Cerrar el dialog directamente
                dialog?.dismiss()
            }
        }
        
        headerLayout.addView(emojiText)
        headerLayout.addView(titleLayout)
        headerLayout.addView(closeButton)
        
        // Separador
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                1
            ).apply {
                bottomMargin = 16
            }
            setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
        }
        
        // Loading indicator
        val loadingText = TextView(context).apply {
            text = "⏳ Cargando tareas..."
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.parseColor("#666666"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )
        }
        
        // RecyclerView
        val recyclerView = androidx.recyclerview.widget.RecyclerView(context).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Altura máxima más razonable
                height = 400
            }
            visibility = View.GONE // Oculto hasta que se carguen las tareas
        }
        
        // Estado vacío
        val emptyLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            )
            visibility = View.GONE
        }
        
        val emptyEmoji = TextView(context).apply {
            text = "📭"
            textSize = 48f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        
        val emptyText = TextView(context).apply {
            text = "No hay tareas asignadas"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            gravity = android.view.Gravity.CENTER
        }
        
        emptyLayout.addView(emptyEmoji)
        emptyLayout.addView(emptyText)
        
        // Botón "Ir a ayudarle"
        val btnIrAyudar = android.widget.Button(context).apply {
            text = "🚗 Ir a ayudarle"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            
            // Fondo azul moderno
            val background = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#1976D2"))
                cornerRadius = 25f
            }
            setBackground(background)
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            
            setPadding(16, 12, 16, 12)
            
            setOnClickListener {
                // Cerrar el modal y trazar ruta
                dialog?.dismiss()
                trazarRutaHaciaAfectado(afectado)
            }
        }
        
        // Agregar todo al layout principal
        mainLayout.addView(headerLayout)
        mainLayout.addView(divider)
        mainLayout.addView(loadingText)
        mainLayout.addView(recyclerView)
        mainLayout.addView(emptyLayout)
        mainLayout.addView(btnIrAyudar)
        
        // Cargar tareas reales del API
        cargarTareasAfectado(afectado.dni, recyclerView, loadingText, emptyLayout)
        
        return mainLayout
    }
    
    private fun cargarTareasAfectado(
        dniAfectado: String, 
        recyclerView: androidx.recyclerview.widget.RecyclerView, 
        loadingText: TextView, 
        emptyLayout: LinearLayout
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("Menu_activity", "=== INICIANDO CARGA DE TAREAS ===")
                Log.d("Menu_activity", "DNI del afectado: '$dniAfectado'")
                Log.d("Menu_activity", "URL completa: ${RetrofitClient.BASE_URL}api/Tarea/afectado/$dniAfectado")
                
                val response = RetrofitClient.tareaApiService.getTareasAfectado(dniAfectado)
                
                Log.d("Menu_activity", "Código de respuesta: ${response.code()}")
                Log.d("Menu_activity", "¿Respuesta exitosa?: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val tareasApi = response.body()
                    Log.d("Menu_activity", "Respuesta body: $tareasApi")
                    Log.d("Menu_activity", "Número de tareas recibidas: ${tareasApi?.size ?: 0}")
                    
                    // Log detallado de cada tarea
                    tareasApi?.forEachIndexed { index, tarea ->
                        Log.d("Menu_activity", "Tarea $index: ID=${tarea.id}, Nombre='${tarea.nombre}', Estado='${tarea.estado}', Fecha='${tarea.fechaInicio}' (${if (tarea.fechaInicio == null) "NULL" else "NO NULL"}), Voluntarios=${tarea.voluntarios?.size ?: 0}")
                        tarea.voluntarios?.forEachIndexed { vIndex, voluntario ->
                            Log.d("Menu_activity", "  Voluntario $vIndex: '$voluntario'")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Menu_activity", "Error en respuesta - Código: ${response.code()}")
                    Log.e("Menu_activity", "Error body: $errorBody")
                    Log.e("Menu_activity", "Headers: ${response.headers()}")
                }
                
                withContext(Dispatchers.Main) {
                    loadingText.visibility = View.GONE
                    
                    if (response.isSuccessful) {
                        val tareasApi = response.body() ?: emptyList()
                        Log.d("Menu_activity", "Procesando ${tareasApi.size} tareas en UI thread")
                        
                        if (tareasApi.isNotEmpty()) {
                            // Convertir de API DTO a DTO del adapter
                            val tareasAdapter = convertirTareasApi(tareasApi)
                            Log.d("Menu_activity", "Tareas convertidas: ${tareasAdapter.size}")
                            
                            val adapter = com.example.solidarityhub.android.adapters.TareasAfectadoAdapter(
                                tareasAdapter,
                                onTareaAsignada = {
                                    // Recargar tareas cuando se asigna una
                                    cargarTareasAfectado(dniAfectado, recyclerView, loadingText, emptyLayout)
                                }
                            )
                            recyclerView.adapter = adapter
                            
                            recyclerView.visibility = View.VISIBLE
                            emptyLayout.visibility = View.GONE
                            
                            Log.d("Menu_activity", "RecyclerView configurado y visible")
                        } else {
                            Log.d("Menu_activity", "Lista de tareas vacía, mostrando estado vacío")
                            recyclerView.visibility = View.GONE
                            emptyLayout.visibility = View.VISIBLE
                        }
                    } else {
                        Log.e("Menu_activity", "Error al cargar tareas: ${response.code()}")
                        // Mostrar mensaje de error pero mantener el botón de ruta funcionando
                        recyclerView.visibility = View.GONE
                        emptyLayout.visibility = View.VISIBLE
                        
                        // Cambiar mensaje de error
                        val emptyText = emptyLayout.getChildAt(1) as TextView
                        emptyText.text = "Error al cargar tareas\n(${response.code()})"
                    }
                }
                
            } catch (e: Exception) {
                Log.e("Menu_activity", "Error de conexión al cargar tareas", e)
                Log.e("Menu_activity", "Stack trace: ${e.stackTrace.contentToString()}")
                withContext(Dispatchers.Main) {
                    loadingText.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    
                    // Cambiar mensaje de error
                    val emptyText = emptyLayout.getChildAt(1) as TextView
                    emptyText.text = "Error de conexión\nal cargar tareas\n${e.message}"
                }
            }
        }
    }
    
    private fun convertirTareasApi(tareasApi: List<TareaAfectadoApiDTO>): List<com.example.solidarityhub.android.data.dto.TareaAfectadoDTO> {
        Log.d("Menu_activity", "=== CONVIRTIENDO TAREAS API ===")
        Log.d("Menu_activity", "Número de tareas a convertir: ${tareasApi.size}")
        
        return tareasApi.mapIndexed { index, tareaApi ->
            Log.d("Menu_activity", "Convirtiendo tarea $index:")
            Log.d("Menu_activity", "  - ID: ${tareaApi.id}")
            Log.d("Menu_activity", "  - Nombre: '${tareaApi.nombre}'")
            Log.d("Menu_activity", "  - Descripción: '${tareaApi.descripcion}'")
            Log.d("Menu_activity", "  - Estado original: '${tareaApi.estado}'")
            Log.d("Menu_activity", "  - Fecha original: '${tareaApi.fechaInicio}'")
            Log.d("Menu_activity", "  - Voluntarios: ${tareaApi.voluntarios}")
            
            // Formatear fecha (ahora puede ser null)
            val fechaFormateada = formatearFecha(tareaApi.fechaInicio)
            Log.d("Menu_activity", "  - Fecha formateada: '$fechaFormateada'")
            
            // Mapear estado
            val estadoFormateado = when (tareaApi.estado.lowercase()) {
                "pendiente" -> "Pendiente"
                "en_progreso" -> "En Progreso"
                "completada" -> "Completada"
                else -> tareaApi.estado
            }
            Log.d("Menu_activity", "  - Estado formateado: '$estadoFormateado'")
            
            // Formatear voluntarios
            val voluntarioFormateado = formatearVoluntarios(tareaApi.voluntarios)
            Log.d("Menu_activity", "  - Voluntario formateado: '$voluntarioFormateado'")
            
            val tareaConvertida = com.example.solidarityhub.android.data.dto.TareaAfectadoDTO(
                id = tareaApi.id,
                titulo = tareaApi.nombre,
                descripcion = tareaApi.descripcion,
                fecha = fechaFormateada,
                estado = estadoFormateado,
                voluntarioNombre = voluntarioFormateado,
                voluntarioTelefono = "Sin contacto" // La API no devuelve esta info
            )
            
            Log.d("Menu_activity", "  - Tarea convertida: $tareaConvertida")
            tareaConvertida
        }
    }
    
    private fun formatearVoluntarios(voluntarios: List<String>?): String {
        Log.d("Menu_activity", "Formateando voluntarios: $voluntarios")
        
        // Manejar null
        if (voluntarios == null) {
            Log.d("Menu_activity", "Lista de voluntarios es null")
            return "Sin asignar"
        }
        
        // Filtrar voluntarios vacíos o nulos
        val voluntariosFiltrados = voluntarios.filter { it.isNotBlank() }
        
        return when {
            voluntariosFiltrados.isEmpty() -> {
                Log.d("Menu_activity", "No hay voluntarios asignados")
                "Sin asignar"
            }
            voluntariosFiltrados.size == 1 -> {
                Log.d("Menu_activity", "Un voluntario: ${voluntariosFiltrados[0]}")
                voluntariosFiltrados[0]
            }
            else -> {
                val primerVoluntario = voluntariosFiltrados[0]
                val restantes = voluntariosFiltrados.size - 1
                val resultado = "$primerVoluntario + $restantes"
                Log.d("Menu_activity", "Múltiples voluntarios: $resultado")
                resultado
            }
        }
    }
    
    private fun formatearFecha(fechaIso: String?): String {
        Log.d("Menu_activity", "Formateando fecha: '$fechaIso'")
        
        // Manejar null
        if (fechaIso == null) {
            Log.d("Menu_activity", "Fecha es null, usando fecha por defecto")
            return "Sin fecha"
        }
        
        // Manejar string vacío
        if (fechaIso.isBlank()) {
            Log.d("Menu_activity", "Fecha está vacía, usando fecha por defecto")
            return "Sin fecha"
        }
        
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fecha = formatoEntrada.parse(fechaIso)
            val resultado = formatoSalida.format(fecha ?: return "Fecha inválida")
            Log.d("Menu_activity", "Fecha formateada exitosamente: '$resultado'")
            resultado
        } catch (e: Exception) {
            Log.w("Menu_activity", "Error formateando fecha '$fechaIso': ${e.message}")
            // Si hay error parseando, devolver fecha simplificada
            val resultado = try {
                fechaIso.split("T")[0] // Al menos quitar la parte de hora
            } catch (e2: Exception) {
                "Fecha inválida"
            }
            Log.d("Menu_activity", "Usando fecha simplificada: '$resultado'")
            resultado
        }
    }

    private fun createCustomInfoWindow(afectado: AfectadoADTO): View {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            
            // Crear fondo con bordes redondeados
            val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.WHITE)
                cornerRadius = 24f
                setStroke(4, android.graphics.Color.parseColor("#FFCDD2"))
            }
            background = backgroundDrawable
            
            // Agregar elevación para sombra
            elevation = 8f
            
            // Asegurar tamaño mínimo
            minimumWidth = 300
            minimumHeight = 150
        }
        
        // Header con emoji
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 12)
        }
        
        val emojiText = TextView(context).apply {
            text = "🆘"
            textSize = 20f
            setPadding(0, 0, 16, 0)
        }
        
        val titleText = TextView(context).apply {
            text = "Persona Afectada"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#D32F2F"))
        }
        
        headerLayout.addView(emojiText)
        headerLayout.addView(titleText)
        layout.addView(headerLayout)
        
        // Datos del afectado
        val nombreText = TextView(context).apply {
            text = "👤 ${afectado.nombre}"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            setPadding(0, 4, 0, 4)
        }
        
        val telefonoText = TextView(context).apply {
            text = "📱 ${afectado.telefono}"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#B71C1C"))
            setPadding(0, 0, 0, 4)
        }
        
        val direccionText = TextView(context).apply {
            text = "📍 ${afectado.direccion}"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            setPadding(0, 0, 0, 8)
            maxLines = 2
        }
        
        val estadoText = TextView(context).apply {
            text = "🚨 Necesita Ayuda"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#FF5252"))
            
            // Fondo redondeado para el estado
            val estadoBackground = android.graphics.drawable.GradientDrawable().apply {
                setColor(android.graphics.Color.parseColor("#FFEBEE"))
                cornerRadius = 12f
            }
            background = estadoBackground
            setPadding(12, 8, 12, 8)
        }
        
        layout.addView(nombreText)
        layout.addView(telefonoText)
        layout.addView(direccionText)
        layout.addView(estadoText)
        
        return layout
    }

    private fun createCustomMarkerIcon(): com.google.android.gms.maps.model.BitmapDescriptor {
        try {
            // Crear un bitmap personalizado completamente programático
            val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Fondo rojo circular
            val paint = Paint().apply {
                color = android.graphics.Color.parseColor("#F44336")
                isAntiAlias = true
            }
            canvas.drawCircle(40f, 40f, 35f, paint)
            
            // Borde blanco
            val borderPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                strokeWidth = 4f
                style = Paint.Style.STROKE
            }
            canvas.drawCircle(40f, 40f, 35f, borderPaint)
            
            // Símbolo de exclamación (!)
            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
                textSize = 45f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            // Línea vertical del !
            canvas.drawText("!", 40f, 50f, textPaint)
            
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        } catch (e: Exception) {
            // Fallback: usar marcador rojo por defecto si hay algún problema
            Log.e("Menu_activity", "Error creando marcador personalizado: ${e.message}")
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Cargar y centrar en la ubicación del usuario
        centerMapOnUserLocation()
    }

    private fun centerMapOnUserLocation() {
        val latitud = sessionManager.getUserLatitud()
        val longitud = sessionManager.getUserLongitud()
        val direccion = sessionManager.getUserDireccion()

        Log.d("MAP_LOCATION", "Datos de ubicación: Lat=$latitud, Lng=$longitud, Dir=$direccion")

        if (latitud != 0.0 && longitud != 0.0) {
            val ubicacion = LatLng(latitud, longitud)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f)) // Zoom más cercano
            
            // Añadir marcador en la ubicación
            mMap.addMarker(MarkerOptions()
                .position(ubicacion)
                .title(direccion ?: "Mi ubicación")
                .snippet("Usuario actual")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            
            Log.d("MAP_LOCATION", "Mapa centrado en la ubicación del usuario: $latitud, $longitud")
        } else {
            // Si no hay ubicación guardada, centrar en España
            val espana = LatLng(40.4168, -3.7038)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 6f))
            Log.d("MAP_LOCATION", "No hay ubicación del usuario, centrando en España")
        }
    }

    private fun trazarRutaHaciaAfectado(afectado: AfectadoADTO) {
        val userLat = sessionManager.getUserLatitud()
        val userLng = sessionManager.getUserLongitud()
        
        if (userLat == 0.0 || userLng == 0.0) {
            Toast.makeText(this, "No se pudo obtener tu ubicación actual", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Verificar API Key
        if (isApiKeyConfigured()) {
            Log.d("Menu_activity", "API Key de Google Maps configurado correctamente")
            Toast.makeText(this, "Configurando ruta por carreteras", Toast.LENGTH_SHORT).show()
            trazarRutaPorCarreteras(userLat, userLng, afectado)
        } else {
            Log.w("Menu_activity", "API Key de Google Maps no configurado, usando ruta simple")
            Toast.makeText(this, "Configurando ruta simple (sin API Key)", Toast.LENGTH_SHORT).show()
            crearRutaSimple(userLat, userLng, afectado)
        }
    }
    
    private fun trazarRutaPorCarreteras(userLat: Double, userLng: Double, afectado: AfectadoADTO) {
        // Limpiar ruta anterior si existe
        currentRoutePolyline?.remove()
        
        // Mostrar loading
        Toast.makeText(this, "Calculando ruta por carreteras...", Toast.LENGTH_SHORT).show()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val origin = "$userLat,$userLng"
                val destination = "${afectado.latitud},${afectado.longitud}"
                
                Log.d("Menu_activity", "Solicitando ruta: $origin -> $destination")
                
                val response = RetrofitClient.directionsApiService.getDirections(
                    origin = origin,
                    destination = destination,
                    apiKey = googleMapsApiKey
                )
                
                Log.d("Menu_activity", "Respuesta API: ${response.code()}")
                
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
                    Log.d("Menu_activity", "Status: ${directionsResponse?.status}")
                    Log.d("Menu_activity", "Rutas encontradas: ${directionsResponse?.routes?.size}")
                    
                    if (directionsResponse?.routes?.isNotEmpty() == true) {
                        val route = directionsResponse.routes[0]
                        val polylinePoints = route.overview_polyline.points
                        val decodedPoints = PolylineUtils.decodePolyline(polylinePoints)
                        
                        Log.d("Menu_activity", "Puntos decodificados: ${decodedPoints.size}")
                        
                        withContext(Dispatchers.Main) {
                            // Dibujar ruta en el mapa
                            val polylineOptions = PolylineOptions()
                                .addAll(decodedPoints)
                                .color(android.graphics.Color.parseColor("#1976D2"))
                                .width(12f)
                                .pattern(listOf())
                            
                            currentRoutePolyline = mMap.addPolyline(polylineOptions)
                            
                            // Mostrar botón de cancelar ruta
                            mostrarBotonCancelarRuta()
                            
                            // Ajustar cámara para mostrar toda la ruta
                            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                            decodedPoints.forEach { boundsBuilder.include(it) }
                            val bounds = boundsBuilder.build()
                            val padding = 100 // padding en píxeles
                            
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            )
                            
                            // Mostrar información de la ruta
                            val leg = route.legs[0]
                            Toast.makeText(
                                this@Menu_activity, 
                                "✅ Ruta por carreteras hacia ${afectado.nombre}\n📏 ${leg.distance.text} • ⏱️ ${leg.duration.text}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Log.w("Menu_activity", "No se encontraron rutas")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Menu_activity, "No se encontró ruta, usando línea directa", Toast.LENGTH_SHORT).show()
                            crearRutaSimple(userLat, userLng, afectado)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Menu_activity", "Error API ${response.code()}: $errorBody")
                    withContext(Dispatchers.Main) {
                        if (response.code() == 403) {
                            Toast.makeText(this@Menu_activity, "⚠️ API Key inválido o sin permisos\nRevisa Google Cloud Console", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@Menu_activity, "Error en API (${response.code()}), usando ruta simple", Toast.LENGTH_SHORT).show()
                        }
                        crearRutaSimple(userLat, userLng, afectado)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("Menu_activity", "Error al calcular ruta", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Menu_activity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    crearRutaSimple(userLat, userLng, afectado)
                }
            }
        }
    }
    
    private fun crearRutaSimple(userLat: Double, userLng: Double, afectado: AfectadoADTO) {
        // Crear una ruta más inteligente sin API
        val origin = LatLng(userLat, userLng)
        val destination = LatLng(afectado.latitud, afectado.longitud)
        
        // Calcular puntos intermedios para simular carreteras
        val puntosRuta = calcularRutaInteligente(origin, destination)
        
        val polylineOptions = PolylineOptions()
            .addAll(puntosRuta)
            .color(android.graphics.Color.parseColor("#FF5722"))
            .width(10f)
            .pattern(listOf(
                com.google.android.gms.maps.model.Dash(30f),
                com.google.android.gms.maps.model.Gap(15f)
            ))
        
        currentRoutePolyline = mMap.addPolyline(polylineOptions)
        
        // Mostrar botón de cancelar ruta
        mostrarBotonCancelarRuta()
        
        // Ajustar cámara
        val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
        puntosRuta.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        val padding = 100
        
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, padding)
        )
        
        // Calcular distancia aproximada
        val distancia = calcularDistanciaKm(origin, destination)
        
        Toast.makeText(
            this, 
            "🗺️ Ruta aproximada hacia ${afectado.nombre}\n📏 ~${String.format("%.1f", distancia)} km (estimado)\n💡 Para rutas exactas: configura Google API Key",
            Toast.LENGTH_LONG
        ).show()
    }
    
    private fun calcularRutaInteligente(origin: LatLng, destination: LatLng): List<LatLng> {
        val puntos = mutableListOf<LatLng>()
        puntos.add(origin)
        
        // Agregar puntos intermedios para simular curvas de carretera
        val deltaLat = destination.latitude - origin.latitude
        val deltaLng = destination.longitude - origin.longitude
        
        // Crear 3-5 puntos intermedios con pequeñas variaciones
        for (i in 1..4) {
            val progreso = i / 5.0
            val variacion = 0.001 * kotlin.math.sin(progreso * kotlin.math.PI * 2) // Pequeñas curvas
            
            val puntoIntermedio = LatLng(
                origin.latitude + (deltaLat * progreso) + variacion,
                origin.longitude + (deltaLng * progreso) + (variacion * 0.5)
            )
            puntos.add(puntoIntermedio)
        }
        
        puntos.add(destination)
        return puntos
    }
    
    private fun calcularDistanciaKm(punto1: LatLng, punto2: LatLng): Double {
        val radioTierra = 6371.0 // Radio de la Tierra en km
        
        val dLat = Math.toRadians(punto2.latitude - punto1.latitude)
        val dLon = Math.toRadians(punto2.longitude - punto1.longitude)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(punto1.latitude)) * 
                kotlin.math.cos(Math.toRadians(punto2.latitude)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return radioTierra * c
    }

    private fun cancelarRutaActual() {
        currentRoutePolyline?.remove()
        currentRoutePolyline = null
        ocultarBotonCancelarRuta()
    }
    
    private fun mostrarBotonCancelarRuta() {
        binding.btnCancelarRuta.visibility = View.VISIBLE
    }
    
    private fun ocultarBotonCancelarRuta() {
        binding.btnCancelarRuta.visibility = View.GONE
    }
}