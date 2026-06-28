# Music JL / VibeTune (Android Client)

<div align="center">
  <h3>El compañero invisible para la descarga e integración de MP3 desde YouTube</h3>
</div>

---

## 🎨 Arquitectura del Proyecto

Este cliente de Android está organizado siguiendo principios de arquitectura limpia, estructurando las funcionalidades por capas y características:

```plaintext
com.example/
│
├── MainActivity.kt                # Punto de entrada. Intercepta el Intent.ACTION_SEND de YouTube.
├── VibeTuneCore.kt                # Clase Application. Inicializa Room, WorkManager y notificaciones.
├── VibeTuneViewModel.kt           # Conecta la UI con los casos de uso (gestiona el estado).
│
├── ui/                            # 🎨 Capa de Presentación (Jetpack Compose)
│   ├── theme/                     # Diseño de colores, temas tipográficos y estilos globales.
│   ├── components/                # Componentes Compose reutilizables (AlbumArt, Vistas de estado).
│   └── screens/                   # Pantallas completas (Main Dashboard y Overlay de Compartir).
│
├── data/                          # 💾 Capa de Datos (Local y Remota)
│   ├── local/                     # Configuración de SQLite/Room, DAOs y entidades.
│   └── remote/                    # Clientes de API, WebSockets (Supabase) y modelos JSON.
│
├── domain/                        # 🧠 Capa de Reglas de Negocio
│   ├── models/                    # Objetos y entidades de dominio puro (TrackInfo).
│   └── repository/                # Interfaces del repositorio de acceso a datos.
│
├── worker/                        # ⚙️ Capa de Tareas en Segundo Plano
│   ├── DownloadWorker.kt          # Tareas de descarga en background desde Cloudflare R2 con WorkManager.
│   └── NotificationHelper.kt      # Generación de Rich Notifications para mostrar progreso.
│
└── utils/                         # 🛠️ Herramientas y Extensiones
    ├── IntentParser.kt            # Filtro y parser de URLs para contenido compartido.
    └── MediaStoreHelper.kt        # Guardado y consulta de canciones descargadas en Android MediaStore.
```

---

## 📦 Dependencias Clave (FOSS)

El proyecto utiliza las siguientes librerías declaradas en `gradle/libs.versions.toml`:
- **Room Database**: Almacenamiento local SQLite para el historial de descargas y metadatos de las canciones.
- **WorkManager**: Descargas seguras en segundo plano y de larga duración, integrando reintentos automáticos.
- **Media3 (ExoPlayer & Session)**: Motor de reproducción de audio nativo e integración con los controles multimedia de Android.

---

## 🚀 Cómo Empezar Localmente

### Requisitos Previos
- **Android Studio** (Koala o más reciente recomendado)
- **Android SDK** API 24 o superior

### Pasos de Ejecución
1. Clona o abre el directorio del proyecto en Android Studio.
2. Crea un archivo `.env` en la raíz del proyecto y define tu clave de API si es requerido (ver `.env.example`).
3. Sincroniza Gradle para descargar las nuevas dependencias declaradas.
4. Conecta un dispositivo físico o ejecuta un emulador Android (API >= 26 para el soporte completo de canales de notificación).
5. Compila y ejecuta el módulo `:app`.
