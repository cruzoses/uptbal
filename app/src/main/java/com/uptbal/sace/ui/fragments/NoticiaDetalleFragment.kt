package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.databinding.FragmentNoticiaDetalleBinding
import kotlinx.coroutines.launch

class NoticiaDetalleFragment : Fragment() {

    private val args: NoticiaDetalleFragmentArgs by navArgs()

    private var _binding: FragmentNoticiaDetalleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticiaDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtContenido.text = "Cargando…"
        cargar(args.noticiaId)
    }

    private fun cargar(id: Int) {
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.noticia(id)
                if (res.success && res.data != null) {
                    val n = res.data
                    binding.txtTitulo.text = n.titulo ?: "Sin título"
                    binding.txtMeta.text = "Fecha: ${n.fecha ?: "-"}  |  Autor: ${n.autor ?: "-"}"
                    binding.txtContenido.text = n.contenido ?: ""
                } else {
                    binding.txtTitulo.text = "Noticia"
                    binding.txtMeta.text = ""
                    binding.txtContenido.text = res.error ?: "Noticia no encontrada."
                }
            } catch (t: Throwable) {
                binding.txtTitulo.text = "Noticia"
                binding.txtMeta.text = ""
                binding.txtContenido.text = when (t) {
                    is retrofit2.HttpException -> if (t.code() == 401) "Su sesión expiró." else "Error del servidor (${t.code()})"
                    is java.io.IOException -> "Sin conexión con el servidor."
                    else -> t.message ?: "Error inesperado"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
