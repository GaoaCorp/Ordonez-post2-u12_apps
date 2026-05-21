# ADR-003: Estrategia de Persistencia y Sincronización

## Estado
Aceptado — 2026-05-20

## Contexto
**TaskFlow** es una aplicación offline-first: el usuario debe poder crear, editar y eliminar tareas sin conexión a internet, y los cambios deben sincronizarse automáticamente cuando la conectividad se restaure. Se necesita decidir: (1) qué tecnología usar para persistencia local, (2) cómo manejar la sincronización con el backend, y (3) cómo resolver conflictos cuando la misma tarea fue modificada en el servidor y localmente.

La aplicación maneja entidades simples (Task, User) con relaciones 1:N (un usuario tiene muchas tareas). El volumen de datos esperado es bajo (<1000 tareas por usuario).

## Alternativas consideradas

### Persistencia local

#### Opción A: Room (SQLite wrapper de Jetpack)
- ✅ Integración nativa con Kotlin Coroutines y Flow
- ✅ Verificación de queries SQL en tiempo de compilación
- ✅ Migrations controladas con versioning
- ✅ Soporte oficial de Google, bien mantenido
- ❌ Más verboso que DataStore para datos simples key-value
- ❌ Requiere definir entidades, DAOs y Database manualmente

#### Opción B: DataStore (Preferences o Proto)
- ✅ Ideal para configuraciones y preferencias simples
- ✅ API basada en Flow, moderna y type-safe
- ❌ No es una base de datos relacional; no maneja listas o relaciones bien
- ❌ No es adecuado para el modelo de datos de TaskFlow (listas de tareas con filtros)

#### Opción C: SQLDelight
- ✅ Type-safe, multi-platform
- ❌ Curva de aprendizaje adicional innecesaria para este proyecto
- ❌ Menor cantidad de recursos de aprendizaje en español

### Estrategia de sincronización

#### Opción X: Sync manual (usuario presiona "sincronizar")
- ✅ Simple de implementar
- ❌ Mala UX: el usuario debe recordar sincronizar
- ❌ Riesgo de perder datos si el usuario olvida sincronizar

#### Opción Y: Offline-first con WorkManager + background sync
- ✅ Sincronización automática en background cuando hay red
- ✅ WorkManager garantiza ejecución incluso si la app está cerrada
- ✅ El usuario siempre ve datos locales instantáneamente (sin loading)
- ❌ Más complejo: requiere lógica de cola de operaciones pendientes
- ❌ Resolución de conflictos debe implementarse

#### Opción Z: Sync en tiempo real con WebSockets/Firebase
- ✅ Datos siempre actualizados
- ❌ Requiere Firebase o servidor con WebSockets (fuera del alcance del proyecto)
- ❌ Mayor consumo de batería

## Decisión

**Persistencia:** **Room** con las siguientes entidades:
```kotlin
@Entity data class TaskEntity(
    @PrimaryKey val id: String,  // UUID generado localmente
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus  // SYNCED | PENDING_UPLOAD | PENDING_DELETE
)

enum class SyncStatus { SYNCED, PENDING_UPLOAD, PENDING_DELETE }
```

**Sincronización:** **Offline-first con WorkManager** usando el patrón Repository:
1. Toda operación (crear/editar/eliminar) se aplica primero en Room con `syncStatus = PENDING_UPLOAD`
2. La UI observa Room via Flow (nunca espera la red)
3. WorkManager ejecuta `SyncWorker` cuando hay conexión disponible
4. `SyncWorker` envía operaciones pendientes al backend y actualiza `syncStatus = SYNCED`
5. Conflictos resueltos con política **"last-write-wins"** basada en `updatedAt`

**Justificación:** Room es la única opción viable para el modelo de datos relacional de la app. WorkManager es el componente estándar de Android para trabajo en background persistente; garantiza que la sincronización ocurra incluso si el usuario cierra la app. La política last-write-wins es simple y suficiente para el caso de uso (tareas personales con un solo usuario).

## Consecuencias

**Positivas:**
- La UI nunca muestra spinners de carga al abrir la app (datos siempre disponibles desde Room)
- Las operaciones del usuario son instantáneas desde su perspectiva
- WorkManager reintenta automáticamente si falla la sincronización (red inestable)

**Trade-offs aceptados conscientemente:**
- La política last-write-wins puede causar pérdida de datos en ediciones concurrentes desde múltiples dispositivos; se acepta porque TaskFlow en esta versión es monousuario-monodispositivo
- Mantener `syncStatus` en cada entidad añade complejidad al modelo de datos; este overhead se justifica porque hace el estado de sincronización explícito y observable
- WorkManager tiene latencia de ~15 minutos en modo Doze; se acepta porque las tareas de TaskFlow no son time-critical y la consistencia eventual es suficiente para este dominio
