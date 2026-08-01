package com.uptbal.sace.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ==================== ACCESO / CUENTAS ====================

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/logout")
    suspend fun logout(): MessageEnvelope

    @GET("api/profile")
    suspend fun profile(): LoginResponse

    @POST("api/recuperar-clave")
    suspend fun recuperarClave(@Body body: RecuperarClaveRequest): MessageEnvelope

    @GET("api/captcha")
    suspend fun captcha(): CaptchaResponse

    @POST("api/registro-estudiante")
    suspend fun registroEstudiante(@Body body: Map<String, String>): MessageEnvelope

    @POST("api/registro-docente")
    suspend fun registroDocente(@Body body: Map<String, String>): MessageEnvelope

    // ==================== ESTUDIANTE ====================

    @GET("api/me-estudiante")
    suspend fun meEstudiante(): ApiEnvelope<EstudianteDto>

    @GET("api/situacion")
    suspend fun situacion(): ApiEnvelope<List<SituacionPrograma>>

    @GET("api/notas-lapso")
    suspend fun notasLapso(): ApiEnvelope<List<NotaLapsoDto>>

    @GET("api/inscripciones")
    suspend fun inscripciones(): ApiEnvelope<List<InscripcionDto>>

    @GET("api/historicos")
    suspend fun historicos(): ApiEnvelope<List<HistoricoDto>>

    // ==================== NOTICIAS ====================

    @GET("api/noticias")
    suspend fun noticias(): ApiEnvelope<List<NoticiaDto>>

    @GET("api/noticias/{id}")
    suspend fun noticia(@Path("id") id: Int): ApiEnvelope<NoticiaDto>

    // ==================== PERFIL ====================

    @PUT("api/perfil")
    suspend fun actualizarPerfil(@Body body: PerfilUpdateRequest): MessageEnvelope
}
