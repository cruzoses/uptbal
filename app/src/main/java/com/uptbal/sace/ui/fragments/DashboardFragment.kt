package com.uptbal.sace.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.uptbal.sace.R
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.databinding.FragmentDashboardBinding
import com.uptbal.sace.ui.login.LoginActivity
import com.uptbal.sace.util.Boxes
import com.uptbal.sace.util.ImageUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val session = SessionManager(ctx)

        lifecycleScope.launch {
            val user = session.user.first()
            val nombre = listOf(user?.nombres, user?.apellidos).filterNotNull().joinToString(" ")
            binding.txtNombres.text = nombre.ifBlank { user?.username ?: "" }
            binding.txtCedula.text = "Cédula: ${user?.cedula ?: "-"}"
            binding.txtBienvenida.text = "Bienvenido(a), $nombre."
            user?.foto?.let { ImageUtil.decodeBase64(it) }?.let { binding.imgFoto.setImageBitmap(it) }
        }

        val accesos = listOf(
            Triple(R.string.nav_situacion, R.drawable.ic_school, R.id.situacionFragment),
            Triple(R.string.nav_notas, R.drawable.ic_notas, R.id.notasFragment),
            Triple(R.string.nav_inscripciones, R.drawable.ic_inscripciones, R.id.inscripcionesFragment),
            Triple(R.string.nav_historicos, R.drawable.ic_historicos, R.id.historicosFragment),
            Triple(R.string.nav_noticias, R.drawable.ic_noticias, R.id.noticiasFragment),
            Triple(R.string.nav_perfil, R.drawable.ic_perfil, R.id.perfilFragment)
        )

        accesos.forEach { (titulo, icono, destino) ->
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, Boxes.dp(ctx, 4), 0, Boxes.dp(ctx, 4))
            val btn = MaterialButton(ctx).apply {
                text = getString(titulo)
                setIconResource(icono)
                iconGravity = MaterialButton.ICON_GRAVITY_START
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.white))
                setTextColor(ContextCompat.getColor(ctx, R.color.adminlte_sidebar))
                iconTint = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.adminlte_primary))
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.adminlte_primary))
                strokeWidth = Boxes.dp(ctx, 1)
                cornerRadius = Boxes.dp(ctx, 6)
                layoutParams = lp
                setOnClickListener { findNavController().navigate(destino) }
            }
            binding.listaAccesos.addView(btn)
        }

        val btnSalir = MaterialButton(ctx).apply {
            text = getString(R.string.nav_salir)
            setIconResource(R.drawable.ic_salir)
            iconGravity = MaterialButton.ICON_GRAVITY_START
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.adminlte_danger))
            setTextColor(ContextCompat.getColor(ctx, R.color.white))
            iconTint = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, Boxes.dp(ctx, 8), 0, 0) }
            setOnClickListener { cerrarSesion() }
        }
        binding.listaAccesos.addView(btnSalir)

        lifecycleScope.launch {
            val carrera = try {
                val res = ApiClient.service.inscripciones()
                if (res.success) res.data?.firstOrNull()?.carrera?.nombre else null
            } catch (_: Throwable) {
                null
            }
            binding.txtCarrera.text = carrera ?: ""
        }
    }

    private fun cerrarSesion() {
        val session = SessionManager(requireContext())
        lifecycleScope.launch {
            runCatching { ApiClient.service.logout() }
            ApiClient.apiToken = null
            session.clearSession()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
