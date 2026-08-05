package com.uptbal.sace.ui.registro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.R
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.databinding.ActivityRegistroRapidoBinding
import com.uptbal.sace.ui.main.MainActivity
import com.uptbal.sace.util.ImageUtil
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Calendar

class RegistroRapidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroRapidoBinding
    private lateinit var session: SessionManager

    private var captcha1Id: String? = null
    private var captcha2Id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroRapidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.registro_rapido_titulo)
        session = SessionManager(this)

        binding.inputFechaNac.setOnClickListener { mostrarSelectorFecha() }
        binding.btnRecargarCaptcha1.setOnClickListener { cargarCaptcha1() }
        binding.btnRecargarCaptcha2.setOnClickListener { cargarCaptcha2() }
        binding.btnSolicitarToken.setOnClickListener { solicitarToken() }
        binding.btnRegistrar.setOnClickListener { registrar() }
        binding.btnFormularioCompleto.setOnClickListener { abrirFormularioCompleto() }

        cargarCaptcha1()
    }

    private fun mostrarSelectorFecha() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, anio, mes, dia ->
                binding.inputFechaNac.setText(
                    String.format("%04d-%02d-%02d", anio, mes + 1, dia)
                )
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun cargarCaptcha1() {
        binding.imgCaptcha1.setImageDrawable(null)
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.captcha()
                if (res.success && res.captcha_id != null) {
                    captcha1Id = res.captcha_id
                    ImageUtil.decodeBase64(res.image_base64)?.let {
                        binding.imgCaptcha1.setImageBitmap(it)
                    }
                } else {
                    toast(res.error ?: "No se pudo generar el captcha")
                }
            } catch (t: Throwable) {
                toast("Sin conexión con el servidor. Verifique su red.")
            }
        }
    }

    private fun cargarCaptcha2() {
        binding.imgCaptcha2.setImageDrawable(null)
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.captcha()
                if (res.success && res.captcha_id != null) {
                    captcha2Id = res.captcha_id
                    ImageUtil.decodeBase64(res.image_base64)?.let {
                        binding.imgCaptcha2.setImageBitmap(it)
                    }
                } else {
                    toast(res.error ?: "No se pudo generar el captcha")
                }
            } catch (t: Throwable) {
                toast("Sin conexión con el servidor. Verifique su red.")
            }
        }
    }

    private fun solicitarToken() {
        val cedula = binding.inputCedula.text?.toString()?.trim().orEmpty()
        val fecha = binding.inputFechaNac.text?.toString()?.trim().orEmpty()
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val captchaCode = binding.inputCaptcha1.text?.toString()?.trim().orEmpty()

        if (cedula.isEmpty() || fecha.isEmpty() || email.isEmpty()) {
            toast("Debe completar la cédula, fecha de nacimiento y correo electrónico.")
            return
        }
        val captcha = captcha1Id
        if (captcha == null || captchaCode.isEmpty()) {
            toast("Escriba el resultado del captcha.")
            return
        }

        binding.btnSolicitarToken.isEnabled = false
        binding.progress1.visibility = View.VISIBLE

        val body = mapOf(
            "cedula" to cedula,
            "fecha_nacimiento" to fecha,
            "email" to email,
            "captcha_id" to captcha,
            "captcha_code" to captchaCode
        )

        lifecycleScope.launch {
            try {
                val res = ApiClient.service.solicitarToken(body)
                toast(res.message ?: res.error ?: getString(R.string.registro_rapido_enviado))
                if (!res.expediente.isNullOrBlank() && !res.token.isNullOrBlank()) {
                    binding.inputExpediente.setText(res.expediente)
                    binding.inputToken.setText(res.token)
                }
                mostrarPaso2()
            } catch (e: HttpException) {
                toast("Error del servidor (${e.code()})")
            } catch (e: IOException) {
                toast("Sin conexión con el servidor. Verifique su red.")
            } catch (e: Exception) {
                toast(e.message ?: "Error inesperado")
            } finally {
                binding.btnSolicitarToken.isEnabled = true
                binding.progress1.visibility = View.GONE
            }
        }
    }

    private fun mostrarPaso2() {
        binding.paso1.visibility = View.GONE
        binding.paso2.visibility = View.VISIBLE
        cargarCaptcha2()
        binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun registrar() {
        val cedula = binding.inputCedula.text?.toString()?.trim().orEmpty()
        val fecha = binding.inputFechaNac.text?.toString()?.trim().orEmpty()
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val expediente = binding.inputExpediente.text?.toString()?.trim().orEmpty()
        val token = binding.inputToken.text?.toString()?.trim().orEmpty()
        val username = binding.inputUsername.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val passwordConfirmar = binding.inputPasswordConfirmar.text?.toString().orEmpty()
        val captchaCode = binding.inputCaptcha2.text?.toString()?.trim().orEmpty()

        if (cedula.isEmpty() || fecha.isEmpty() || email.isEmpty() ||
            expediente.isEmpty() || token.isEmpty() || username.isEmpty()
        ) {
            toast("Debe completar todos los campos.")
            return
        }
        if (password.length < 6) {
            toast("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (password != passwordConfirmar) {
            toast("Las contraseñas no coinciden.")
            return
        }
        val captcha = captcha2Id
        if (captcha == null || captchaCode.isEmpty()) {
            toast("Escriba el resultado del captcha.")
            return
        }

        binding.btnRegistrar.isEnabled = false
        binding.progress2.visibility = View.VISIBLE

        val body = mapOf(
            "cedula" to cedula,
            "fecha_nacimiento" to fecha,
            "email" to email,
            "expediente" to expediente,
            "token" to token,
            "username" to username,
            "password" to password,
            "password_confirmar" to passwordConfirmar,
            "captcha_id" to captcha,
            "captcha_code" to captchaCode
        )

        lifecycleScope.launch {
            try {
                val res = ApiClient.service.autoRegistroEstudiante(body)
                if (res.success && res.token != null && res.user != null) {
                    ApiClient.apiToken = res.token
                    session.saveSession(res.token, res.user)
                    toast(getString(R.string.registro_rapido_auto_ok))
                    startActivity(Intent(this@RegistroRapidoActivity, MainActivity::class.java))
                    finish()
                } else {
                    toast(res.error ?: "No se pudo completar el registro.")
                    cargarCaptcha2()
                }
            } catch (e: HttpException) {
                toast("Error del servidor (${e.code()})")
                cargarCaptcha2()
            } catch (e: IOException) {
                toast("Sin conexión con el servidor. Verifique su red.")
            } catch (e: Exception) {
                toast(e.message ?: "Error inesperado")
            } finally {
                binding.btnRegistrar.isEnabled = true
                binding.progress2.visibility = View.GONE
            }
        }
    }

    private fun abrirFormularioCompleto() {
        startActivity(
            Intent(this, RegistroActivity::class.java)
                .putExtra(RegistroActivity.EXTRA_MODO, RegistroActivity.MODO_ESTUDIANTE)
        )
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
