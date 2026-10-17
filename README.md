# PulsoCheck

Aplicación de seguimiento personal para Google Pixel Watch (Wear OS) y un teléfono Android compatible.

## Alcance inicial

- Leer frecuencia cardiaca mediante Health Services cuando el dispositivo, los permisos y las capacidades lo permitan.
- Guardar lecturas, episodios y respuestas localmente en cada dispositivo.
- Pedir al usuario que registre cómo se siente desde el reloj.
- Sincronizar registros al teléfono y mostrar un historial de cinco días.

PulsoCheck es un prototipo de registro personal. No diagnostica enfermedades ni recomienda tratamientos. Los límites de prueba son parámetros de software, no criterios clínicos.

## Tecnologías propuestas

- Kotlin
- Android Studio, con módulos `wear` y `mobile`
- Compose for Wear OS y Jetpack Compose
- Health Services
- Wearable Data Layer
- Room (SQLite)

## Cómo trabajaremos

El proyecto se construirá en pasos pequeños. Antes de añadir una función, revisaremos qué problema resuelve, dónde vive en la arquitectura y cómo probarla. El código nuevo llevará comentarios que expliquen las decisiones y conceptos importantes. Cada hito se revisará y se registrará en Git con un mensaje que describa el cambio.

## Estado

- [x] Alcance de la propuesta revisado
- [x] Repositorio local aislado preparado
- [x] Proyecto Android Studio creado y compilado
- [ ] Modelo de datos y almacenamiento local
- [ ] Flujo de respuesta en Wear OS
- [ ] Sincronización Wearable Data Layer
- [ ] Historial móvil de cinco días
- [ ] Validación en reloj y teléfono

## Documentación

- [Plan de trabajo](docs/plan-de-trabajo.md)
- [Complemento de la propuesta: arquitectura, patrones y funciones](docs/complemento-propuesta.md)
- [Complemento de la propuesta en PDF](docs/Complemento_Propuesta_PulsoCheck.pdf)
