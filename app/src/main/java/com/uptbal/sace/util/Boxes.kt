package com.uptbal.sace.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.uptbal.sace.R

object Boxes {

    fun dp(context: Context, v: Int): Int = (v * context.resources.displayMetrics.density).toInt()

    private fun border(context: Context, color: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            setStroke(dp(context, 1), Color.rgb(0xDD, 0xDD, 0xDD))
        }

    fun tarjeta(context: Context, titulo: String? = null, contenido: (LinearLayout) -> Unit): MaterialCardView {
        val card = MaterialCardView(context)
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, dp(context, 4), 0, dp(context, 4))
        card.layoutParams = lp
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
        card.radius = dp(context, 6).toFloat()
        card.cardElevation = dp(context, 3).toFloat()
        card.useCompatPadding = true
        card.setContentPadding(
            dp(context, 12),
            dp(context, 8),
            dp(context, 12),
            dp(context, 8)
        )

        val cont = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        if (titulo != null) {
            cont.addView(
                TextView(context).apply {
                    text = titulo
                    setTextColor(ContextCompat.getColor(context, R.color.adminlte_sidebar))
                    setTypeface(typeface, Typeface.BOLD)
                    textSize = 15f
                    setPadding(0, 0, 0, dp(context, 6))
                }
            )
        }
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        cont.addView(body)
        contenido(body)
        card.addView(cont)
        return card
    }

    fun filaTabla(
        context: Context,
        celdas: List<String>,
        header: Boolean = false,
        colores: Map<Int, Int> = emptyMap()
    ): View {
        val fila = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            background = border(context, if (header) Color.rgb(0xF5, 0xF5, 0xF5) else Color.WHITE)
            setPadding(dp(context, 4), dp(context, 6), dp(context, 4), dp(context, 6))
        }
        celdas.forEachIndexed { i, texto ->
            if (i > 0) {
                fila.addView(
                    View(context).apply {
                        setBackgroundColor(Color.rgb(0xDD, 0xDD, 0xDD))
                        layoutParams = LinearLayout.LayoutParams(
                            dp(context, 1),
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                )
            }
            fila.addView(
                TextView(context).apply {
                    text = texto
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    textSize = if (header) 13f else 12f
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(context, 4), 0, dp(context, 4), 0)
                    if (header) {
                        setTextColor(Color.rgb(0x55, 0x55, 0x55))
                        setTypeface(typeface, Typeface.BOLD)
                    } else {
                        val c = colores[i]
                        setTextColor(
                            if (c != null) c else Color.rgb(0x33, 0x33, 0x33)
                        )
                        if (c != null) setTypeface(typeface, Typeface.BOLD)
                    }
                }
            )
        }
        return fila
    }

    fun filaSimple(context: Context, etiqueta: String, valor: String): View =
        TextView(context).apply {
            text = "$etiqueta: $valor"
            textSize = 14f
            setTextColor(Color.rgb(0x44, 0x44, 0x44))
            setPadding(0, dp(context, 2), 0, dp(context, 2))
        }

    fun linea(context: Context): View = View(context).apply {
        setBackgroundColor(Color.rgb(0xE8, 0xE8, 0xE8))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(context, 1)
        ).apply {
            topMargin = dp(context, 6)
            bottomMargin = dp(context, 6)
        }
    }

    fun texto(context: Context, contenido: String, titulo: Boolean = false): TextView =
        TextView(context).apply {
            text = contenido
            if (titulo) {
                setTextColor(ContextCompat.getColor(context, R.color.adminlte_sidebar))
                setTypeface(typeface, Typeface.BOLD)
                textSize = 16f
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.texto_gris))
                textSize = 14f
            }
            setPadding(0, dp(context, 2), 0, dp(context, 2))
        }

    fun colorNota(context: Context, calificacion: String?, aprobada: Boolean): Int {
        if (calificacion.isNullOrBlank()) return Color.rgb(0x33, 0x33, 0x33)
        return if (aprobada) {
            ContextCompat.getColor(context, R.color.aprobado)
        } else {
            ContextCompat.getColor(context, R.color.reprobado)
        }
    }
}
