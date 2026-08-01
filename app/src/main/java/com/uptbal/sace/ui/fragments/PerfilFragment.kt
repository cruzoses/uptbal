package com.uptbal.sace.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.PerfilUpdateRequest
import com.uptbal.sace.databinding.FragmentPerfilBinding
import com.uptbal.sace.ui.login.LoginActivity
import com.uptbal.sace.util.ImageUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private var fotoBase64: String? = null

    private val seleccionarFoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val bytes = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val bitmap = ImageUtil.decodificar(bytes)
                    binding.imgFoto.setImageBitmap(bitmap)
                    fotoBase64 = ImageUtil.comprimirYEncodear(bitmap)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val session = SessionManager(ctx)

        lifecycleScope.launch {
            val user = session.user.first()
            if (user != null) {
                binding.txtNombres.text = listOf(user.nombres, user.apellidos)
                    .filterNotNull()
                    .joinToString(" ")
                    .ifBlank { user.username ?: "" }
                binding.txtUsername.text = "@${user.username ?: "-"}"
                binding.inputTwitter.setText(user.twitter ?: "")
                binding.inputInstagram.setText(user.instagram ?: "")
                binding.inputFacebook.setText(user.facebook ?: "")
                user.foto?.let { ImageUtil.decodeBase64(it) }?.let { binding.imgFoto.setImageBitmap(it) }
            }
        }

        binding.btnSeleccionarFoto.setOnClickListener {
            seleccionarFoto.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            guardar()
        }

        binding.btnSalir.setOnClickListener {
            lifecycleScope.launch {
                runCatching { ApiClient.service.logout() }
                ApiClient.apiToken = null
                session.clearSession()
                startActivity(Intent(ctx, LoginActivity::class.java))
                activity?.finishAffinity()
            }
        }
    }

    private fun guardar() {
        val session = SessionManager(requireContext())
        binding.btnGuardar.isEnabled = false
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.actualizarPerfil(
                    PerfilUpdateRequest(
                        twitter = binding.inputTwitter.text?.toString()?.trim()?.ifEmpty { null },
                        instagram = binding.inputInstagram.text?.toString()?.trim()?.ifEmpty { null },
                        facebook = binding.inputFacebook.text?.toString()?.trim()?.ifEmpty { null },
                        foto = fotoBase64
                    )
                )
                if (res.success) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Perfil actualizado correctamente.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    fotoBase64 = null
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        res.error ?: "No se pudo actualizar el perfil.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (t: Throwable) {
                android.widget.Toast.makeText(
                    requireContext(),
                    t.message ?: "Error al guardar el perfil",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnGuardar.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
