package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.launch

class NoticiasFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Noticias")
        cargar()
        setRecargar { cargar() }
    }

    private fun cargar() {
        lifecycleScope.launch {
            setCargando()
            try {
                val res = ApiClient.service.noticias()
                if (res.success) {
                    val noticias = res.data.orEmpty()
                    if (noticias.isEmpty()) {
                        setContenido("No hay noticias publicadas.")
                    } else {
                        setContenido(
                            noticias.joinToString("\n\n---\n\n") { n ->
                                buildString {
                                    append(n.titulo ?: "Sin título")
                                    append("\nFecha: ").append(n.fecha ?: "-")
                                    append("  Autor: ").append(n.autor ?: "-")
                                    append("\n").append(n.contenido ?: "")
                                }
                            }
                        )
                    }
                } else {
                    setContenido(res.error ?: "No se pudieron cargar las noticias.")
                }
            } catch (t: Throwable) {
                mensajeError(t)
                setContenido("No se pudieron cargar las noticias.")
            } finally {
                finCargando()
            }
        }
    }
}
