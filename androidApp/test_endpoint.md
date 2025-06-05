# Test del Endpoint de Tareas

## Endpoint a probar:
```
GET http://localhost:5127/api/Tarea/afectado/{dni}
```

## Ejemplo de prueba:
```bash
# Reemplaza '12345678A' con un DNI real de tu base de datos
curl -X GET "http://localhost:5127/api/Tarea/afectado/12345678A" -H "Content-Type: application/json"
```

## Estructura esperada de respuesta (ACTUALIZADA):
```json
[
  {
    "id": 1,
    "descripcion": "Entrega de medicamentos urgentes",
    "nombre": "Medicamentos para hipertensión",
    "estado": "pendiente",
    "fecha_inicio": "2024-01-20T00:00:00",
    "voluntarios": [
      "María García",
      "Carlos López"
    ]
  },
  {
    "id": 5,
    "descripcion": "Compra de alimentos básicos",
    "nombre": "Compra semanal",
    "estado": "en_progreso",
    "fecha_inicio": "2024-01-21T00:00:00",
    "voluntarios": [
      "Ana Martínez"
    ]
  },
  {
    "id": 8,
    "descripcion": "Acompañamiento médico",
    "nombre": "Cita cardiología",
    "estado": "completada",
    "fecha_inicio": "2024-01-18T00:00:00",
    "voluntarios": []
  }
]
```

## Lógica de voluntarios:
- **Múltiples voluntarios**: "María García + 1" (primer nombre + restantes)
- **Un voluntario**: "Ana Martínez"
- **Sin voluntarios**: "Sin asignar"
- **Lista null/vacía**: "Sin asignar"

## Para depurar en la app:
1. Ejecuta la app
2. Haz clic en un marcador rojo (afectado)
3. Presiona el InfoWindow
4. Mira los logs en Logcat con filtro "Menu_activity"

## Filtros de Logcat recomendados:
- Tag: `Menu_activity`
- Nivel: `Debug` y `Error` 