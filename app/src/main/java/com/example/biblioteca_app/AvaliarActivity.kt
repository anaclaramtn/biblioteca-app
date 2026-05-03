package com.example.biblioteca_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.DialogConfirmacaoCancelarBinding
import com.example.biblioteca_app.databinding.TelaAvaliarBinding

class AvaliarActivity : AppCompatActivity() {

    private lateinit var binding: TelaAvaliarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaAvaliarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarHeader()
        configurarBotoes()
    }

    private fun configurarHeader() {
        val header = binding.header
        header.txtTitulo.text = "Nova Avaliação"
        header.btnBack.visibility = android.view.View.VISIBLE
        header.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun configurarBotoes() {
        binding.btnConfirmar.setOnClickListener {
            val nota = binding.ratingBarAvaliacao.rating
            val titulo = binding.edtTituloAvaliacao.text.toString()
            val comentario = binding.edtComentarioAvaliacao.text.toString()

            if (nota == 0f) {
                Toast.makeText(this, "Dê uma nota", Toast.LENGTH_SHORT).show()
            } else if (titulo.isEmpty() || comentario.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                // Futuramente salvar no banco
                Toast.makeText(this, "Avaliação enviada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnCancelar.setOnClickListener {
            mostrarConfirmacaoCancelar()
        }
    }

    private fun mostrarConfirmacaoCancelar() {
        val dialogBinding = DialogConfirmacaoCancelarBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnNao.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSim.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}