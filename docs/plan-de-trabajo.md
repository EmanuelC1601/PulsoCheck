# Plan de trabajo de PulsoCheck

Este plan convierte la propuesta inicial en entregas pequeñas que se puedan revisar y explicar.

## Estado al 9 de noviembre de 2026

La implementación principal está en el repositorio. Aún no se debe marcar el proyecto como terminado hasta compilar los cambios finales y ejecutar la lista de pruebas en un reloj Wear OS y un teléfono compatibles.

| Hito | Estado |
|---|---|
| Estructura Wear + teléfono y pantallas iniciales | Implementado |
| Room, entidades, DAO y repositorios | Implementado en ambos módulos |
| Detección de episodios y respuesta desde el reloj | Implementado en código; requiere validación física |
| Health Services y permiso de frecuencia cardiaca | Integración implementada con MeasureClient; pruebas físicas pendientes |
| Sincronización de lecturas, episodios y respuestas | Integrada con Data Layer; verificación de desconexión/reconexión pendiente |
| Historial telefónico, resumen diario y borrado local confirmado | Implementado; falta compilar los cambios más recientes |
| Notificaciones de episodio | Integradas en ambos módulos; dependen de permisos y ajustes del dispositivo |
| Complemento de propuesta actualizado | Documento Markdown y PDF actualizados |
| Compilación final y pruebas completas en dispositivos | Pendiente |

## Decisiones base de la propuesta

- Dos módulos Android: `wear` para Wear OS y `mobile` para el teléfono.
- Kotlin y Compose para las interfaces.
- Health Services para lecturas de pulso; Wearable Data Layer para comunicar ambos módulos.
- Room para conservar datos en cada dispositivo, incluso cuando no haya conexión.
- El teléfono presenta un historial de cinco días. No hay cuenta en la nube ni servidor propio en la primera versión.

## Arquitectura propuesta para aprender

Usaremos una arquitectura por capas, empezando simple:

1. **Presentación:** pantallas Compose y estado visible.
2. **Dominio:** reglas entendibles, como decidir cuándo varias lecturas forman un episodio.
3. **Datos:** Room, Health Services y Wearable Data Layer detrás de repositorios.

La interfaz no leerá directamente la base de datos ni implementará reglas de detección. Esa separación hace más fácil explicar, probar y cambiar cada parte.

## Patrones que evaluaremos al implementarlos

- **Repository:** una interfaz común para que las pantallas y reglas consulten o guarden datos sin depender de Room directamente.
- **Observer / flujo reactivo:** observar cambios guardados para refrescar la interfaz sin consultas manuales repetidas.
- **Adapter:** traducir eventos de Health Services y mensajes del Data Layer a modelos propios de PulsoCheck.
- **State:** representar claramente estados de seguimiento, sincronización y respuesta.

Aplicaremos cada patrón cuando resuelva un problema real y explicaremos qué simplifica.

## Hitos

1. Preparar Android Studio, validar SDK y crear la estructura Wear + móvil.
2. Compilar una pantalla inicial en cada dispositivo y entender el flujo de ejecución.
3. Definir entidades y persistir datos de demostración con Room.
4. Implementar y explicar la regla de episodios usando datos simulados.
5. Diseñar la respuesta desde el reloj y guardar la selección.
6. Conectar Health Services y verificar permisos/capacidades en el dispositivo.
7. Sincronizar por bloques con identificadores idempotentes y confirmación.
8. Mostrar historial de cinco días, huecos sin datos y detalle.
9. Revisar notificaciones, reinicio, desconexión y reconexión.
10. Completar la memoria con arquitectura, patrones, funciones, APIs, validación y conclusiones.

## Práctica de Git

- Un cambio pequeño por commit, después de revisar y poder explicar el cambio.
- Mensajes descriptivos, por ejemplo: `docs: documentar alcance de PulsoCheck`.
- No incluir `local.properties`, claves, contraseñas, datos de salud reales ni archivos de compilación.
- El repositorio de GitHub será privado. Cuando esté creado, se podrá compartir con la asesora indicada por el equipo.
- Antes de publicar cada hito, revisar los archivos con `git status` y el cambio con `git diff`.

## Límites de seguridad y privacidad

Solo se recopilarán los permisos mínimos. Los datos se guardarán localmente y se ofrecerá borrarlos. Las lecturas atrasadas quedarán como historial y no se presentarán como una alerta actual. Los valores de prueba no son recomendaciones clínicas.
