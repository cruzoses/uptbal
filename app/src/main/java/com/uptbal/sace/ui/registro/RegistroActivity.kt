package com.uptbal.sace.ui.registro

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.R
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.databinding.ActivityRegistroBinding
import com.uptbal.sace.util.ImageUtil
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    private val modo: String
        get() = intent.getStringExtra(EXTRA_MODO) ?: MODO_ESTUDIANTE

    private var captchaId: String? = null

    companion object {
        const val EXTRA_MODO = "modo"
        const val MODO_ESTUDIANTE = "estudiante"
        const val MODO_DOCENTE = "docente"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = if (modo == MODO_DOCENTE) {
            getString(R.string.registro_titulo_docente)
        } else {
            getString(R.string.registro_titulo_estudiante)
        }

        if (modo == MODO_DOCENTE) {
            binding.fieldExpediente.visibility = View.GONE
        }

        binding.spinnerSexo.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf(getString(R.string.registro_sexo_f), getString(R.string.registro_sexo_m))
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.inputFechaNac.setOnClickListener { mostrarSelectorFecha() }
        binding.btnRecargarCaptcha.setOnClickListener { cargarCaptcha() }
        binding.btnRegistrar.setOnClickListener { registrar() }
        binding.btnSolicitarClave.setOnClickListener {
            startActivity(Intent(this, RegistroRapidoActivity::class.java))
            finish()
        }

        cargarCaptcha()
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

    private fun cargarCaptcha() {
        binding.imgCaptcha.setImageDrawable(null)
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.captcha()
                if (res.success && res.captcha_id != null) {
                    captchaId = res.captcha_id
                    ImageUtil.decodeBase64(res.image_base64)?.let {
                        binding.imgCaptcha.setImageBitmap(it)
                    }
                } else {
                    toast(res.error ?: "No se pudo generar el captcha")
                }
            } catch (t: Throwable) {
                toast("Sin conexión con el servidor. Verifique su red.")
            }
        }
    }

    private fun registrar() {
        val datos = mutableMapOf<String, String>()
        datos["nombres"] = binding.inputNombres.text?.toString()?.trim().orEmpty()
        datos["apellidos"] = binding.inputApellidos.text?.toString()?.trim().orEmpty()
        datos["cedula"] = binding.inputCedula.text?.toString()?.trim().orEmpty()
        datos["fecha_nacimiento"] = binding.inputFechaNac.text?.toString()?.trim().orEmpty()
        datos["sexo"] = if (binding.spinnerSexo.selectedItemPosition == 0) "F" else "M"
        datos["telefonos"] = binding.inputTelefonos.text?.toString()?.trim().orEmpty()
        datos["email"] = binding.inputEmail.text?.toString()?.trim().orEmpty()
        datos["username"] = binding.inputUsername.text?.toString()?.trim().orEmpty()
        datos["password"] = binding.inputPassword.text?.toString().orEmpty()
        datos["password_confirmar"] = binding.inputPasswordConfirmar.text?.toString().orEmpty()
        datos["token"] = binding.inputToken.text?.toString()?.trim().orEmpty()
        datos["captcha_code"] = binding.inputCaptcha.text?.toString()?.trim().orEmpty()

        if (modo == MODO_ESTUDIANTE) {
            datos["expediente"] = binding.inputExpediente.text?.toString()?.trim().orEmpty()
        }

        val error = validar(datos)
        if (error != null) {
            toast(error)
            return
        }

        val captcha = captchaId
        if (captcha == null) {
            toast("Recargue el captcha e intente de nuevo.")
            return
        }
        datos["captcha_id"] = captcha

        binding.btnRegistrar.isEnabled = false
        binding.progressRegistro.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val res = if (modo == MODO_DOCENTE) {
                    ApiClient.service.registroDocente(datos)
                } else {
                    ApiClient.service.registroEstudiante(datos)
                }
                if (res.success) {
                    toast(getString(R.string.registro_ok))
                    finish()
                } else {
                    toast(res.error ?: "No se pudo completar el registro.")
                    cargarCaptcha()
                }
            } catch (e: HttpException) {
                toast("Error del servidor (${e.code()})")
                cargarCaptcha()
            } catch (e: IOException) {
                toast("Sin conexión con el servidor. Verifique su red.")
            } catch (e: Exception) {
                toast(e.message ?: "Error inesperado")
            } finally {
                binding.btnRegistrar.isEnabled = true
                binding.progressRegistro.visibility = View.GONE
            }
        }
    }

    private fun validar(datos: Map<String, String>): String? {
        if (datos["nombres"].isNullOrBlank() || datos["apellidos"].isNullOrBlank()) {
            return "Debe ingresar nombres y apellidos."
        }
        if (datos["cedula"].isNullOrBlank()) {
            return "Debe ingresar su cédula."
        }
        if (datos["fecha_nacimiento"].isNullOrBlank()) {
            return "Debe seleccionar su fecha de nacimiento."
        }
        if (datos["email"].isNullOrBlank()) {
            return "Debe ingresar su correo electrónico."
        }
        if (datos["username"].isNullOrBlank()) {
            return "Debe ingresar un nombre de usuario."
        }
        if (datos["password"].isNullOrEmpty() || datos["password"]!!.length < 6) {
            return "La contraseña debe tener al menos 6 caracteres."
        }
        if (datos["password"] != datos["password_confirmar"]) {
            return "Las contraseñas no coinciden."
        }
        if (datos["token"].isNullOrBlank()) {
            return "Debe ingresar su clave de registro."
        }
        if (datos["captcha_code"].isNullOrBlank()) {
            return "Debe escribir el resultado del captcha."
        }
        if (modo == MODO_ESTUDIANTE && datos["expediente"].isNullOrBlank()) {
            return "Debe ingresar su número de expediente."
        }
        return null
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
