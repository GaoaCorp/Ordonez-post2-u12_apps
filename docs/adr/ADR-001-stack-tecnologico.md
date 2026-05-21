# ADR-001: Stack Tecnológico

## Estado
Aceptado — 2026-05-20

## Contexto
El proyecto **TaskFlow** es una aplicación Android de gestión de tareas colaborativas desarrollada por 1 integrante en 6 semanas. La aplicación requiere funcionamiento offline-first con sincronización eventual, manejo de listas en tiempo real y una UI moderna y mantenible. El equipo tiene experiencia intermedia con Android nativo y conocimiento básico de Jetpack Compose.

Se necesita decidir el stack tecnológico completo antes de comenzar el desarrollo para evitar refactorizaciones costosas a mitad del proyecto.

## Alternativas consideradas

### 1. Kotlin + Jetpack Compose + arquitectura moderna Android
- ✅ Es el stack oficial recomendado por Google (2024+)
- ✅ Compose elimina el boilerplate de XML y permite UI declarativa
- ✅ Excelente integración con Kotlin Coroutines y Flow
- ✅ Hilt como DI está bien documentado y soportado
- ❌ Curva de aprendizaje de Compose si no hay experiencia previa
- ❌ Compose aún madurando en features avanzadas (LazyGrid, etc.)

### 2. Kotlin + XML Views (arquitectura tradicional)
- ✅ Más estable, mayor cantidad de ejemplos en internet
- ✅ Menor curva de aprendizaje inicial
- ❌ Más boilerplate (adapters, ViewHolder, etc.)
- ❌ Google está descontinuando activamente este enfoque
- ❌ Menor productividad a largo plazo

### 3. Flutter (Dart)
- ✅ Cross-platform (Android e iOS con un solo código)
- ✅ UI consistente en todas las plataformas
- ❌ El proyecto es específicamente para Android nativo (requisito académico)
- ❌ Requiere aprender Dart desde cero
- ❌ Peor integración con APIs nativas de Android

## Decisión
Se adopta **Kotlin + Jetpack Compose + stack moderno Android**:

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Kotlin | 1.9.22 |
| UI | Jetpack Compose + Material3 | BOM 2024.02.00 |
| DI | Hilt | 2.50 |
| Persistencia local | Room | 2.6.1 |
| Red | Retrofit + OkHttp | 2.9.0 |
| Asincronía | Coroutines + Flow | 1.7.3 |
| Ciclo de vida | ViewModel + LiveData/StateFlow | 2.7.0 |
| Build | Gradle 8.x + Version Catalog | 8.2.2 |

**Justificación:** Compose es el futuro del desarrollo Android y este proyecto integrador es la oportunidad ideal para aprenderlo correctamente. La inversión inicial en la curva de aprendizaje se justifica porque el código resultante es más limpio, testeable y mantenible que XML. Hilt simplifica la inyección de dependencias con mínima configuración comparado con Dagger puro.

## Consecuencias

**Positivas:**
- Código UI más conciso y declarativo (~40% menos líneas que XML equivalente)
- Preview en tiempo real de composables en Android Studio
- Estado compartido entre pantallas via ViewModel y StateFlow es natural

**Trade-offs aceptados conscientemente:**
- La compilación inicial de Compose es más lenta (~20% más tiempo en primer build)
- Algunos widgets de Compose Material3 tienen comportamiento ligeramente diferente a Material2; se acepta esta inconsistencia menor durante la migración del ecosistema
- Al ser 1 integrante, no se aprovecha plenamente el build incremental de multi-módulo, pero la estructura se mantiene para cumplir requisitos académicos y como práctica profesional
