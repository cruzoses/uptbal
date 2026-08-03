package com.uptbal.sace.data.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class MessageEnvelope(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(
    val success: Boolean = false,
    val token: String? = null,
    val user: UserDto? = null,
    val error: String? = null
)

@Serializable
data class UserDto(
    val id: Int = 0,
    val cedula: Long = 0,
    val nombres: String? = null,
    val apellidos: String? = null,
    val email: String? = null,
    val username: String? = null,
    val sexo: String? = null,
    val foto: String? = null,
    val twitter: String? = null,
    val instagram: String? = null,
    val facebook: String? = null,
    val roles: List<RoleDto> = emptyList()
)

@Serializable
data class RoleDto(
    val id: Int = 0,
    val nombre: String? = null
)

@Serializable
data class EstudianteDto(
    val id: Int = 0,
    val cedula: Long = 0,
    val nombres: String? = null,
    val apellidos: String? = null,
    val fecha_nacimiento: String? = null,
    val sexo: String? = null,
    val email: String? = null,
    val telefonos: String? = null,
    val expediente: String? = null,
    val activo: Int = 0
)

@Serializable
data class NoticiaDto(
    val id: Int = 0,
    val fecha: String? = null,
    val titulo: String? = null,
    val contenido: String? = null,
    val autor: String? = null
)

@Serializable
data class SituacionPrograma(
    val programa_id: Int = 0,
    val programa: ProgramaDto? = null,
    val carrera: CarreraDto? = null,
    val resumen: ResumenDto? = null,
    val asignaturas: List<AsignaturaSituacionDto> = emptyList()
)

@Serializable
data class ProgramaDto(
    val id: Int = 0,
    val codename: String? = null,
    val nombre: String? = null,
    val nota_minima: Double = 0.0,
    val creditos: Int = 0
)

@Serializable
data class CarreraDto(
    val id: Int = 0,
    val codigo: String? = null,
    val nombre: String? = null
)

@Serializable
data class ResumenDto(
    val creditos_programa: Int = 0,
    val total_asignaturas: Int = 0,
    val creditos_aprobados: Int = 0,
    val asignaturas_aprobadas: Int = 0,
    val porcentaje_aprobado: Double = 0.0,
    val isa: Double = 0.0,
    val ira: Double = 0.0
)

@Serializable
data class AsignaturaSituacionDto(
    val id: Int = 0,
    val trayecto: String? = null,
    val asignatura_id: Int = 0,
    val asignatura_codigo: String? = null,
    val asignatura_nombre: String? = null,
    val creditos: Int = 0,
    val calificacion: String? = null,
    val seccion: String? = null,
    val periodo: String? = null,
    val responsable: String? = null,
    val cualitativa: Int = 0,
    val nota_minima: Double = 0.0,
    val aprobada: Int = 0
)

@Serializable
data class NotaLapsoDto(
    val id: Int = 0,
    val curso_id: Int = 0,
    val seccion: String? = null,
    val horario: String? = null,
    val asignatura: AsignaturaDto? = null,
    val periodo: PeriodoDto? = null,
    val docente: String? = null,
    val calificacion: String? = null,
    val recuperacion: String? = null,
    val definitiva: String? = null,
    val observacion: String? = null,
    val activo: Int = 0,
    val evaluaciones: List<EvaluacionDto> = emptyList()
)

@Serializable
data class AsignaturaDto(
    val id: Int = 0,
    val codigo: String? = null,
    val nombre: String? = null,
    val creditos: Int = 0
)

@Serializable
data class PeriodoDto(
    val id: Int = 0,
    val codigo: String? = null,
    val lapso: Int = 0
)

@Serializable
data class EvaluacionDto(
    val id: Int = 0,
    val descripcion: String? = null,
    val detalle: String? = null,
    val fecha: String? = null,
    val ponderacion: Double = 0.0,
    val escala_nota: Double = 0.0,
    val indicador: String? = null,
    val nota: String? = null
)

@Serializable
data class InscripcionDto(
    val id: Int = 0,
    val carrera_id: Int = 0,
    val carrera: CarreraDto? = null,
    val programa_id: Int = 0,
    val programa: ProgramaDto? = null,
    val sede_id: Int = 0,
    val sede: String? = null,
    val periodo_id: Int = 0,
    val periodo: PeriodoDto? = null,
    val fecha_egreso: String? = null,
    val cohorte: String? = null,
    val isa: Double = 0.0,
    val ira: Double = 0.0,
    val culminado: Int = 0,
    val congelado: Int = 0,
    val activo: Int = 0
)

@Serializable
data class HistoricoDto(
    val id: Int = 0,
    val periodo: PeriodoDto? = null,
    val asignatura: AsignaturaDto? = null,
    val calificacion: String? = null,
    val seccion: String? = null,
    val responsable: String? = null,
    val created: String? = null
)

@Serializable
data class CaptchaResponse(
    val success: Boolean = false,
    val captcha_id: String? = null,
    val question: String? = null,
    val image_base64: String? = null,
    val error: String? = null
)

@Serializable
data class RecuperarClaveRequest(val email: String)

@Serializable
data class PerfilUpdateRequest(
    val twitter: String? = null,
    val instagram: String? = null,
    val facebook: String? = null,
    val foto: String? = null
)
