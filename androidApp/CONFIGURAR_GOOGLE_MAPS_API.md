# 🗺️ Configuración del Google Maps API Key

## ¿Por qué necesitas esto?
Para que la app muestre rutas reales por carreteras en lugar de líneas rectas.

## 💳 **¿Google Cloud es de pago?**

**¡NO!** Google Maps tiene un **nivel GRATUITO** muy generoso:

### 🆓 **GRATIS cada mes:**
- ✅ **10,000 rutas** (Directions API)
- ✅ **28,000 cargas de mapa**
- ✅ **100,000 búsquedas de direcciones**

### 📱 **¿Cuánto usa tu app?**
- **1 ruta trazada** = 1 llamada API
- **Conclusión**: Tendrías que trazar **10,000 rutas al mes** para pagar algo
- **Realidad**: Difícil que uses más de 50-100 rutas al mes

### 💰 **Si superases el límite:**
- **Costo**: $0.005 por ruta extra (medio centavo)
- **Para llegar a $1**: Necesitarías 200 rutas extra

**🎯 Pide tarjeta solo para verificar identidad, como Netflix o Spotify**

---

## 🛣️ **ALTERNATIVA: Rutas Offline Mejoradas**

Si **NO quieres poner tarjeta**, ya he mejorado las rutas sin API:

### ✅ **Lo que tienes ahora (SIN API KEY):**
- 🗺️ Rutas con **curvas simuladas** (no líneas rectas)
- 📏 **Distancia calculada** en kilómetros
- 🎨 Rutas **naranjas punteadas** más bonitas
- ⚡ **Funciona sin internet** para el cálculo

### 📱 **Mensaje que verás:**
`"🗺️ Ruta aproximada hacia [Nombre] 📏 ~X.X km (estimado)"`

---

## 📋 Pasos a seguir:

### 1. **Ir a Google Cloud Console**
- Abre: https://console.cloud.google.com/
- Inicia sesión con tu cuenta de Google

### 2. **Crear un Proyecto Nuevo**
- Haz clic en el dropdown "Select a project" (arriba)
- Selecciona "New Project" 
- Nombre: `SolidarityHub` (o el que prefieras)
- Haz clic en "Create"

### 3. **Habilitar las APIs necesarias**
- Ve a **"APIs & Services"** → **"Library"**
- Busca y habilita ESTAS APIs:
  - ✅ **"Directions API"** (para rutas por carreteras)
  - ✅ **"Maps JavaScript API"** (para el mapa básico)
  - ✅ **"Places API"** (opcional, para búsquedas)

### 4. **Crear tu API Key**
- Ve a **"APIs & Services"** → **"Credentials"**
- Haz clic en **"+ Create Credentials"** → **"API key"**
- **¡COPIA la clave que aparece!** (algo como: `AIzaSyBvl4_xxxxxxxxxxxxxxxxxxxxxxx`)

### 5. **⚠️ IMPORTANTE: Configurar Facturación**
- Ve a **"Billing"** en el menú lateral
- Haz clic en **"Link a billing account"**
- Añade una tarjeta de crédito/débito

**🎁 NO TE PREOCUPES:** 
- Tienes **10,000 llamadas GRATIS** por mes
- La app solo hace 1-2 llamadas por ruta
- ¡Muy difícil que pagues algo!

### 6. **Restringir tu API Key (Recomendado)**
- Haz clic en tu API key para editarla
- **Application restrictions:**
  - Selecciona "HTTP referrers (websites)"
  - Añade: `https://10.0.2.2/*` y `http://localhost/*`
- **API restrictions:**
  - Selecciona "Restrict key"
  - Marca: ✅ Directions API, ✅ Maps JavaScript API

### 7. **Configurar en tu App**
1. Abre: `src/main/res/values/strings.xml`
2. Encuentra esta línea:
   ```xml
   <string name="google_maps_api_key">AQUI_PEGA_TU_API_KEY_REAL</string>
   ```
3. Reemplaza `AQUI_PEGA_TU_API_KEY_REAL` con tu clave real

### 8. **¡Probar!**
- Ejecuta la app
- Haz clic en un marcador de afectado
- Pulsa "🚗 Ir a ayudarle"
- **¡Deberías ver rutas azules por carreteras!**

---

## 🆘 Solución de Problemas

### Si sigue apareciendo "línea directa":
1. **Verifica que el API key esté bien copiado** (sin espacios extra)
2. **Espera 5-10 minutos** (Google tarda en activar la clave)
3. **Revisa que las APIs estén habilitadas** (Directions API es la clave)
4. **Confirma que la facturación esté configurada**

### Si ves error 403:
- **API Key incorrecto** o **APIs no habilitadas**
- Revisa el paso 3 y 4

### Si no funciona después de 10 minutos:
- Regenera el API key en Google Cloud Console
- Copia el nuevo y actualiza `strings.xml`

---

## 💰 Información de Costos

- **GRATIS:** 10,000 llamadas/mes
- **Después:** $0.005 USD por llamada extra
- **Tu app:** ~1-2 llamadas por ruta trazada
- **Realidad:** Muy difícil que pagues algo a menos que uses la app intensivamente

---

## ✅ Confirmación de que Funciona

Cuando veas este mensaje: **"✅ Ruta por carreteras hacia [Nombre] 📏 X km • ⏱️ Y min"**

¡**PERFECTO!** Ya tienes rutas reales por carreteras 🎉 