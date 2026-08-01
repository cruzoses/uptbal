# Plan de Reconstrucción — App Android SACE UPTBAL

> Estado: v1.0 — 2026-07-31
> Destino del proyecto: `E:\JavaSoft\Uptbal`
> Backend: CakePHP 3.10 en `D:\Apache24\htdocs\dace` (repositorio `cruzoses/dace`, commit `e0fc620`)

---

## 1. Objetivo

Reconstruir desde cero la app Android de la **UPTBAL**, porque el proyecto original se
perdió al formatear el disco y nunca se subió a GitHub (solo el PHP del sistema web).
La app está dirigida a **estudiantes y docentes** y debe ofrecer:

**Acceso / cuentas**
1. Iniciar sesión (`username` + `password`)
2. Registro de **estudiante** (cédula + expediente + clave de registro + captcha)
3. Registro de **docente** (cédula + clave de registro + captcha)
4. Recuperar contraseña ("Olvidé mi contraseña" → envío de clave nueva por email)

**Una vez autenticado (portal del estudiante)**
5. Situación académica (ISA / IRA, créditos, % aprobado)
6. Notas de lapso / por curso
7. Inscripciones (programas / carreras / sedes / cohorte)
8. Históricos de notas
9. Noticias
10. Edición de perfil (foto + redes sociales)

La UI debe imitar el aspecto **responsive de la web** (tema AdminLTE): sidebar oscuro,
"boxes", tablas, y colores de notas aprobado/reprobado.

## 2. Decisiones tomadas (confirmadas con el usuario)

| Decisión | Valor |
|---|---|
| Autenticación | `username` + `password` (Auth actual de CakePHP) |
| Registro | Replicar `registroestudiante` / `registrodocente` de la web (incluye **captcha**) |
| Recuperar clave | Replicar `nuevaclave` de la web (email → clave nueva) |
| Stack | Kotlin + XML/ViewBinding, Retrofit + kotlinx.serialization |
| Backend | Extender `ApiController` de `dace` con nuevos endpoints JSON |
| Formato del plan | Este archivo `E:\JavaSoft\Uptbal\PLAN.md` |
| Repositorio | Crear repo git propio para la app (la app no va en el repo de `dace`) |

## 3. Arquitectura

```
┌───────────────┐   HTTPS/HTTP (JSON + X-API-Token)   ┌──────────────────────────┐
│  App Android  │ ───────────────────────────────────► │  CakePHP ApiController   │
│  Kotlin + XML │                                      │  (D:\Apache24\htdocs\dace)│
└───────────────┘                                      └────────────┬─────────────┘
        │                                                           │
        └── Base URL configurable: http://10.0.2.2/dace (emulador)  ▼
                                http://localhost/dace   ┌──────────────────────────┐
                                                       │  MySQL gesaca (localhost) │
                                                       └──────────────────────────┘
```

- **Token**: `POST /dace/api/login` devuelve `api_token` (64 hex). Todos los demás
  endpoints exigen header `X-API-Token`.
- **Cookie de sesión**: los flujos de captcha/registro usan la sesión de CakePHP; la app
  mantiene un **CookieJar persistente** en OkHttp (cookies entre `/captcha` y
  `/registro-*`). El resto de llamadas no depende de cookies.
- **Resolución del estudiante**: la app del estudiante necesita su registro en
  `estudiantes`. La columna `estudiantes.usuario_id` está vacía hoy (COUNT=0); se
  resolverá con fallback por **cédula** (`estudiantes.cedula = usuarios.cedula`).
- **Rol en el inicio de sesión**: al entrar, la app consulta `profile` (incluye `rols`)
  y enruta: rol **ESTUDIANTE** → portal con situación/notas/inscripciones/históricos/
  noticias/perfil; rol **DOCENTE** → vista inicial de bienvenida + perfil (los módulos
  docentes del web quedan fuera del alcance de esta v1).
- **Seguridad**: nunca exponer `password` ni `api_token` en respuestas; el token se
  guarda en DataStore (sesión local).

## 4. Diseño de la API (extender `ApiController`)

Formato de respuesta común: `{ "success": true, "data": ... }` o
`{ "success": false, "error": "..." }` con código HTTP (401/400/405/500).

| Endpoint | Método | Descripción | Estado |
|---|---|---|---|
| `/dace/api/login` | POST | `{username, password}` → `{token, user}` | Existe |
| `/dace/api/logout` | POST | invalida token | Existe |
| `/dace/api/profile` | GET | datos del usuario autenticado | Existe |
| `/dace/api/me/estudiante` | GET | registro `estudiantes` del usuario (por `usuario_id` → fallback cédula) | **Nuevo** |
| `/dace/api/situacion` | GET | situación académica por programa (ISA/IRA, créditos, %, asignaturas) | **Nuevo** |
| `/dace/api/notas-lapso` | GET | notas por curso/lapso: `estudiante_cursos` + `curso_notas` (calificación, recuperación, definitiva) | **Nuevo** |
| `/dace/api/inscripciones` | GET | `estudiante_programas` + carrera, programa, sede, periodo, cohorte, egreso, congelado, culminado | **Nuevo** |
| `/dace/api/historicos` | GET | `historicos` del estudiante (periodo, asignatura, calificación, sección, responsable) | **Nuevo** |
| `/dace/api/noticias` | GET | noticias activas (`activa=1`), orden fecha desc | **Nuevo** |
| `/dace/api/perfil` | PUT | actualizar `twitter`, `instagram`, `facebook` y `foto` (base64, máx ~256 bytes → mejor subir a tamaño pequeño) | **Nuevo** |
| `/dace/api/noticias/{id}` | GET | detalle de una noticia | **Nuevo** |
| `/dace/api/captcha` | GET | genera captcha y devuelve `{captcha_id, image_base64}` (reutiliza `CaptchaComponent`; la imagen en base64 para mostrarla en la app) | **Nuevo** |
| `/dace/api/registro-estudiante` | POST | registro de estudiante (validación cédula/expediente/token + captcha + rol ESTUDIANTE + vínculo `estudiantes.usuario_id`) | **Nuevo** |
| `/dace/api/registro-docente` | POST | registro de docente (validación cédula/token + captcha + rol DOCENTE + vínculo `docentes.usuario_id`) | **Nuevo** |
| `/dace/api/recuperar-clave` | POST | `{email}` → genera clave nueva, la envía por email (misma lógica que `nuevaclave`) | **Nuevo** |

### Detalles críticos por endpoint

- **`/situacion`**: reutilizar la lógica de `DatosController::situacion` (cálculo de
  ISA/IRA, `registrarDesdeMalla`, aprobado con nota mínima del programa o de la malla,
  asignaturas cualitativas A/R). Cuidado con `situacion_estudiantes` (~1.5M filas):
  filtrar SIEMPRE por `estudiante_id` (+`programa_id`). Devolver por programa:
  `programa (codename, nota_minima, creditos)`, `carrera (codigo)`,
  `asignaturas[] (trayecto, codigo, nombre, creditos, nota, seccion, periodo, responsable, aprobada)`,
  `totales (creditos_programa, asignaturas, creditos_aprobados, asignaturas_aprobadas, %aprobado, isa, ira)`.
- **`/notas-lapso`**: base = `estudiante_cursos` (curso del estudiante) con `cursos`
  (asignatura, periodo, sección, docente) y `curso_notas` (evaluaciones con
  ponderación). Incluir `calificacion`, `recuperacion`, `definitiva`, `observacion`.
- **`/historicos`**: 867k filas → filtrar por `estudiante_id`; join `asignaturas` y
  `periodos`; ordenar por periodo desc. Devolver solo si `estudiante` resuelto.
- **`/noticias`**: la tabla está **vacía (0 registros)** → la app debe renderizar
  estado vacío ("No hay noticias publicadas").
- **`/perfil` PUT**: replicar `UsuariosController::perfil` (redes + foto). La web limita
  la foto a 256 bytes; en la app la imagen se reducirá/comprime en el cliente antes de
  enviar (base64). Guardar archivo en `WWW_ROOT/img/fotos/foto{userId}.{ext}` y registrar
  auditoría `MODIFICA PERFIL`.

### Registro y recuperación de contraseña (flujo captcha)

La web exige **captcha** en ambos registros. El `CaptchaComponent` guarda el código en
la **sesión** (cookie `PHPSESSID`). Para la app:

- **`GET /api/captcha`** (sin token): genera el código con `$this->Captcha->generate()`,
  renderiza la imagen con `SecurimageCaptcha` (igual que `CaptchaController::image`) y
  devuelve `{ captcha_id, image_base64 }`. La app mantiene la cookie de sesión (CookieJar
  persistente en OkHttp) para que la validación posterior encuentre el código guardado.
- **`POST /api/registro-estudiante`** (sin token) — campos:
  `cedula, nombres, apellidos, fecha_nacimiento (Y-m-d), sexo, telefonos, email, username,
  password, password_confirmar, expediente, token, captcha_id, captcha_code`.
  Lógica idéntica a `registroestudiante`:
  1. validar captcha (`$this->Captcha->validate($captcha_code, $captcha_id)`),
  2. `password === password_confirmar`,
  3. buscar `estudiantes` por `cedula` con `usuario_id IS null` y `activo=1`; validar
     `expediente` y `token`,
  4. guardar usuario, linkear rol **ESTUDIANTE**, asignar `estudiantes.usuario_id`,
  5. auditoría `REGISTRA`, responder `{ success: true }`.
- **`POST /api/registro-docente`** (sin token) — campos:
  `cedula, nombres, apellidos, fecha_nacimiento (Y-m-d), sexo, telefonos, email, username,
  password, password_confirmar, token, captcha_id, captcha_code`.
  Igual que arriba contra `docentes` (valida solo `cedula` + `token`), rol **DOCENTE**.
- **`POST /api/recuperar-clave`** (sin token) — campo `{ email }`:
  replica `nuevaclave`: busca usuario por email, si existe y está activo genera una clave
  aleatoria, la guarda y la envía por `Email` (template `usuario_nueva_clave`). Respuesta
  siempre amigable (no revelar si el email existe): `{ success: true }` con mensaje
  "Si el correo existe, recibirá una nueva contraseña".

### Registro de auditoría

Todos los endpoints que escriben/consultan deben usar `$this->Auditorias->registrar(...)`
con textos cortos (el componente ya trunca a 65 KB y nunca rompe la página).

## 5. Fases de trabajo

### Fase 0 — Preparación
- [ ] Instalar/verificar Android Studio + JDK 17 + SDK (compileSdk 34, minSdk 24).
- [ ] Crear proyecto en `E:\JavaSoft\Uptbal` (raíz del repo). Nombre de app: **SACE UPTBAL**.
- [ ] `git init` y crear repo; `.gitignore` estándar Android.
- [ ] Probar endpoints actuales con Postman/curl:
  `POST /dace/api/login` (usuario `cruzoses`), `GET /dace/api/profile` con token.

### Fase 1 — Backend: extender `ApiController` (en el repo `dace`)
- [ ] Añadir `me/estudiante`, `situacion`, `notas-lapso`, `inscripciones`, `historicos`, `noticias`, `perfil` (PUT), `captcha`, `registro-estudiante`, `registro-docente`, `recuperar-clave`.
- [ ] Helper privado `_resolverEstudiante($usuarioId, $cedula)` (usuario_id → fallback cédula).
- [ ] Helper privado `_validarCaptcha($code, $id)` para los registros.
- [ ] Crear modelo/controlador de `Noticias` en CakePHP (tabla ya existe, falta el model) si se requiere desde API.
- [ ] Probar cada endpoint con token real y un estudiante de prueba.
- [ ] Commit y push a `cruzoses/dace`.

### Fase 2 — Esqueleto de la app (Kotlin + XML)
- [ ] Gradle (Kotlin DSL): AGP 8.x, Kotlin 1.9+, ViewBinding, Material Components.
- [ ] Dependencias: Retrofit 2.11, kotlinx.serialization, okhttp logging, DataStore Preferences, Coil, Navigation (single activity + fragments), SwipeRefreshLayout.
- [ ] `ApiService` (Retrofit) + `SessionManager` (DataStore: token + usuario + estudiante).
- [ ] `Base URL` configurable vía `BuildConfig` (debug → `10.0.2.2`, release → `localhost`/IP del servidor).
- [ ] Estructura: `LoginActivity` (pantalla única) + `MainActivity` con **NavigationDrawer** (sidebar AdminLTE).

### Fase 3 — Módulos funcionales (fragments)
- [ ] **Login**: campos username/password, validación, guardar token, manejo de errores (401 → mensaje "Usuario o contraseña incorrectos"). Enlaces a: "Registrarse como Estudiante", "Registrarse como Docente" y "¿Olvidó su contraseña?".
- [ ] **Registro Estudiante**: formulario por secciones (Datos Personales, Cuenta, Datos del Registro) con captcha (imagen base64 + botón recargar) y validación cliente; POST `/registro-estudiante`; al éxito → ir a Login.
- [ ] **Registro Docente**: formulario análogo (Datos Personales, Cuenta, Datos del Registro) con captcha; POST `/registro-docente`; al éxito → ir a Login.
- [ ] **Recuperar Contraseña**: campo email + POST `/recuperar-clave`; muestra mensaje de confirmación y botón para volver al Login.
- [ ] **Inicio (Dashboard)**: tarjetas/boxes con datos del estudiante (foto, nombres, cédula, carrera) y accesos rápidos.
- [ ] **Situación Académica**: box por programa con tabla de asignaturas + resumen de índices (igual a `situacion.ctp`).
- [ ] **Notas por Lapso**: lista de cursos con definitiva; expandir para ver evaluaciones (`curso_notas`).
- [ ] **Inscripciones**: tabla de programas (carrera, sede, periodo, cohorte, estado congelado/culminado).
- [ ] **Históricos**: lista/tabla por periodo desc con asignatura y calificación.
- [ ] **Noticias**: lista (tarjeta con fecha/título/contenido) + detalle; estado vacío.
- [ ] **Mi Perfil**: foto (selector + compresión), twitter/instagram/facebook, guardar vía PUT; logout.

### Fase 4 — Estilo AdminLTE (responsive web look)
- [ ] Paleta: sidebar oscuro `#222d32`, fondo `#ecf0f5`, primary `#3c8dbc`, éxito `#00a65a`, warning `#f39c12`, peligro `#dd4b39`.
- [ ] Aprobado: `#0056b3` negrita; Reprobado: `#dc3545` negrita (igual que la web).
- [ ] Tablas tipo `table-bordered table-hover table-condensed` con header y footer de totales.
- [ ] Tipografía sans-serif (fuente del sistema / Source Sans Pro).

### Fase 5 — Pruebas y entrega
- [ ] Probar con emulador (`10.0.2.2`) y con dispositivo real en la misma red.
- [ ] Probar rotación/orientación, sin conexión (mensajes claros), token expirado.
- [ ] Generar APK de depuración y (si aplica) APK firmado.
- [ ] Subir el proyecto a GitHub (repo nuevo, p. ej. `cruzoses/sace-android`).
- [ ] Documentar build + credenciales de prueba en README de la app.

## 6. Riesgos y notas

- **`estudiantes.usuario_id` vacío**: la mayoría de estudiantes no tendrá usuario
  vinculado; la app resuelve por cédula. Si un estudiante no tiene usuario
  (no se registró), la app no puede autenticarlo.
- **Tablas grandes** (`situacion_estudiantes` 1.5M, `historicos` 867k): nunca hacer
  queries sin filtro por `estudiante_id`; verificar índices.
- **`noticias` vacía**: preparar UI de estado vacío.
- **Captcha en móvil**: depende de cookies de sesión; si el servidor bloquea cookies
  (entornos raros), fallback: `GET /api/captcha` devolverá además un `captcha_key` que la
  app reenvía y el backend guardará en una tabla temporal en vez de sesión. Decidir en la
  Fase 1 según cómo responda Apache.
- **Recuperar clave requiere email SMTP**: si `Email` falla, el web ya muestra mensaje de
  éxito genérico; la app igualmente debe tratar el envío como best-effort.
- **Foto** (256 bytes máx en la web): la app comprime a ~200×200 px JPEG ≈ < 1 KB y el
  backend aceptará imágenes pequeñas; documentar el límite.
- **URL base**: en producción se necesitará IP pública/dominio o la IP LAN del servidor.
- **Auditoría**: registrar solo textos cortos; nunca `toArray()` de entidades grandes.

## 7. Entregables finales

1. `E:\JavaSoft\Uptbal` con el proyecto Android completo y compilable (APK).
2. Repo `cruzoses/sace-android` en GitHub.
3. Backend `cruzoses/dace` actualizado con los nuevos endpoints (commit + push).
4. README con instrucciones de build y prueba.
