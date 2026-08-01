package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.launch

class NoticiaDetalleFragment : BaseFragment() {

    private val args: NoticiaDetalleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Noticia")
        cargar(args.noticiaId)
        setRecargar { cargar(args.noticiaId) }
    }

    private fun cargar(id: Int) {
        lifecycleScope.launch {
            setCargando()
            try {
                val res = ApiClient.service.noticia(id)
                if (res.success && res.data != null) {
                    val n = res.data
                    setContenido(
                        "${n.titulo ?: "Sin título"}\n\n" +
                            "Fecha: ${n.fecha ?: "-"}  |  Autor: ${n.autor ?: "-"}\n\n" +
                            (n.contenido ?: "")
                    )
                } else {
                    setContenido(res.error ?: "Noticia no encontrada.")
                }
            } catch (t: Throwable) {
                mensajeError(t)
                setContenido("No se pudo cargar la noticia.")
            } finally {
                finCargando()
            }
        }
    }
}
