package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.DialogConfirmacaoCancelarBinding
import com.example.biblioteca_app.databinding.DialogDenunciaBinding
import com.example.biblioteca_app.databinding.DialogSucessoDenunciaBinding
import com.example.biblioteca_app.databinding.ItemAvaliacaoBinding
import com.example.biblioteca_app.databinding.TelaMaisAvaliacoesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MaisAvaliacoesActivity : AppCompatActivity() {

    private lateinit var binding: TelaMaisAvaliacoesBinding

    // Variáveis para representar a média e o total de avaliações
    private var media: Float = 4.9f
    private var totalAvaliacoes: Int = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaMaisAvaliacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera dados passados pela Intent (ou usa valores padrão)
        media = intent.getFloatExtra("MEDIA", 0.0f)
        totalAvaliacoes = intent.getIntExtra("TOTAL", 0)

        preencherResumo()
        configurarBotoes()
        setupNavBar()
    }

    private fun preencherResumo() {
        // Preenche o resumo de avaliações usando o molde padronizado
        binding.layoutResumo.txtMedia.text = media.toString()
        binding.layoutResumo.txtTotalAvaliacoes.text = "($totalAvaliacoes avaliações)"
        binding.layoutResumo.txtEstrelasMedia.text = converterMediaParaEstrelas(media)

        // Atualiza o título do header com o total se desejar, ou mantém fixo
        binding.txtTituloHeader.text = "Avaliações ($totalAvaliacoes)"
    }

    private fun converterMediaParaEstrelas(media: Float): String {
        return when {
            media >= 4.5 -> "⭐⭐⭐⭐⭐"
            media >= 3.5 -> "⭐⭐⭐⭐☆"
            media >= 2.5 -> "⭐⭐⭐☆☆"
            media >= 1.5 -> "⭐⭐☆☆☆"
            media >= 0.5 -> "⭐☆☆☆☆"
            else -> "☆☆☆☆☆"
        }
    }

    private fun configurarBotoes() {
        binding.btnVoltar.setOnClickListener {
            finish()
        }

        // Configura cada item de avaliação com textos específicos
        configurarItemAvaliacao(binding.avaliacao1, "João Silva", "Excelente leitura, recomendo a todos!", "15/05/2023")
        configurarItemAvaliacao(binding.avaliacao2, "Maria Souza", "O livro é bom, mas o final poderia ser melhor.", "20/06/2023")
        configurarItemAvaliacao(binding.avaliacao3, "Carlos Alberto", "SPOILER: Eu não acredito que o protagonista morre no final! Que choque.", "02/07/2023", temSpoiler = true)
        configurarItemAvaliacao(binding.avaliacao4, "Ana Oliveira", "Personagens muito bem construídos.", "10/07/2023")

        binding.btnAvaliar.setOnClickListener {
            val intent = android.content.Intent(this, AvaliarActivity::class.java)
            startActivity(intent)
        }

        binding.btnOrdenar.setOnClickListener {
            mostrarDialogOrdenacao()
        }
    }

    private fun mostrarDialogOrdenacao() {
        val opcoes = arrayOf("Mais curtidas", "Menos curtidas", "Mais recentes", "Mais antigos")

        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setItems(opcoes) { _, which ->
                val opcaoSelecionada = opcoes[which]
                Toast.makeText(this, "Ordenando por: $opcaoSelecionada", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun configurarItemAvaliacao(
        itemBinding: ItemAvaliacaoBinding,
        nome: String,
        comentario: String,
        data: String,
        temSpoiler: Boolean = false
    ) {
        var curtido = false
        var numCurtidas = (0..50).random() // Valor inicial aleatório
        itemBinding.txtNomeUsuario.text = nome
        itemBinding.txtComentario.text = comentario
        itemBinding.txtData.text = data
        itemBinding.txtCurtidas.text = numCurtidas.toString()

        // Lógica de Spoiler
        if (temSpoiler) {
            itemBinding.txtComentario.visibility = View.GONE
            itemBinding.btnVerSpoiler.visibility = View.VISIBLE
            itemBinding.btnVerSpoiler.setOnClickListener {
                itemBinding.txtComentario.visibility = View.VISIBLE
                itemBinding.btnVerSpoiler.visibility = View.GONE
            }
        } else {
            itemBinding.txtComentario.visibility = View.VISIBLE
            itemBinding.btnVerSpoiler.visibility = View.GONE
        }

        // Clique de Denúncia
        itemBinding.btnDenunciar.setOnClickListener { mostrarDialogDenuncia() }

        // Clique de Curtir (Toggle)
        itemBinding.btnCurtir.setOnClickListener {
            curtido = !curtido
            if (curtido) {
                numCurtidas++
                itemBinding.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
                itemBinding.btnCurtir.clearColorFilter()
            } else {
                numCurtidas--
                itemBinding.btnCurtir.setImageResource(R.drawable.ic_heart)
                itemBinding.btnCurtir.clearColorFilter()
            }
            itemBinding.txtCurtidas.text = numCurtidas.toString()
        }
    }

    private fun mostrarDialogDenuncia() {
        val dialogBinding = DialogDenunciaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelar.setOnClickListener {
            mostrarConfirmacaoCancelar(dialog)
        }

        dialogBinding.btnEnviar.setOnClickListener {
            dialog.dismiss()
            mostrarSucessoDenuncia()
        }

        dialog.show()
    }

    private fun mostrarConfirmacaoCancelar(parentDialog: AlertDialog) {
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
            parentDialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarSucessoDenuncia() {
        val dialogBinding = DialogSucessoDenunciaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_menu

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TelaHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_busca -> {
                    startActivity(Intent(this, BuscaActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notif -> {
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}