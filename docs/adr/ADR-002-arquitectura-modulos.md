# ADR-002: Estrategia de Arquitectura — Módulo Único vs Multi-Módulo

## Estado
Aceptado — 2026-05-20

## Contexto
El proyecto **TaskFlow** es desarrollado por 1 integrante en 6 semanas. La aplicación tiene 4 funcionalidades principales: gestión de tareas, perfil de usuario, notificaciones y sincronización offline. El desarrollador tiene experiencia básica con arquitectura multi-módulo (vista en cursos previos, no implementada en proyectos reales).

Se debe decidir si el proyecto tendrá un único módulo `:app` o se dividirá en múltiples módulos Gradle independientes antes de escribir cualquier código de negocio, ya que refactorizar esta decisión después es costoso.

## Alternativas consideradas

### 1. Módulo único con separación de capas (MVVM + Clean Architecture)
```
:app/
  ├── ui/          (Composables, ViewModels)
  ├── domain/      (Entidades, UseCases, interfaces)
  └── data/        (Repositorios, Room, Retrofit)
```
- ✅ Configuración simple: un solo `build.gradle.kts`
- ✅ Sin overhead de configuración de módulos Gradle
- ✅ Menor curva de aprendizaje; ideal para equipos pequeños o proyectos cortos
- ✅ Navegación sin necesidad de APIs públicas entre módulos
- ❌ Tiempos de build crecen linealmente; al modificar cualquier archivo se recompila todo
- ❌ No hay enforcement de dependencias entre capas (domain puede accidentalmente importar data)
- ❌ Menor escalabilidad si el proyecto crece

### 2. Multi-módulo (`:app` + `:feature:*` + `:core:*`)
```
:app              → punto de entrada, navegación
:feature:tasks    → pantallas de tareas
:feature:profile  → perfil de usuario
:core:domain      → entidades, casos de uso, interfaces
:core:data        → Room, Retrofit, repositorios
:core:ui          → Design System, componentes Compose
```
- ✅ Build incremental: solo recompila módulos afectados por cambios
- ✅ Separation of concerns enforceada por el compilador (`:feature` no puede importar `:data` directamente)
- ✅ Cada módulo es testeable de forma independiente
- ✅ Práctica estándar en proyectos Android profesionales (Now in Android, architecture-samples)
- ❌ Mayor complejidad de configuración inicial (~3-4 horas de setup adicional)
- ❌ Requiere definir APIs públicas entre módulos con cuidado
- ❌ Para equipos de 1 persona el build paralelo no aprovecha múltiples núcleos efectivamente

## Decisión
Se adopta la arquitectura **multi-módulo** con la siguiente estructura:

```
:app              → NavHost, DI setup, MainActivity
:feature:tasks    → TaskListScreen, CreateTaskScreen, TaskDetailScreen
:feature:profile  → ProfileScreen, SettingsScreen
:core:domain      → Task, User (entidades), TaskRepository (interface), UseCases
:core:data        → TaskRepositoryImpl, TaskDao (Room), TaskApiService (Retrofit)
:core:ui          → AppTheme, colores, tipografía, Button, Card, TextField
```

**Grafo de dependencias:**
```
:app → :feature:tasks, :feature:profile
:feature:* → :core:domain, :core:ui
:core:data → :core:domain
:core:domain → (ninguno — capa más interna)
```

**Justificación específica:** Aunque el beneficio de build incremental es menor con 1 desarrollador, se elige multi-módulo porque: (1) es un requisito implícito del proyecto académico para demostrar comprensión de arquitectura, (2) la separación enforceada por el compilador previene errores de acoplamiento accidental durante el desarrollo rápido, y (3) el setup inicial de ~3-4 horas es una inversión única con beneficio educativo directo.

## Consecuencias

**Positivas:**
- Imposible importar Room/Retrofit desde capas de UI (el compilador lo rechaza)
- Los UseCases de `:core:domain` son testeables sin Android framework
- Cambios en `:core:ui` (Design System) no recompilan módulos de negocio

**Trade-offs aceptados conscientemente:**
- La configuración inicial de Gradle es más compleja: cada módulo necesita su propio `build.gradle.kts`, y el `settings.gradle.kts` debe declarar todos los módulos explícitamente
- La navegación entre features requiere pasar por `:app` o definir contratos de navegación explícitos, añadiendo indirección
- Para un proyecto de 1 persona en 6 semanas, parte del tiempo se invierte en configuración de infraestructura en lugar de features de usuario; este costo se acepta conscientemente como aprendizaje
