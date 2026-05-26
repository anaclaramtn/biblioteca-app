package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.DialogConfirmacaoCancelarBinding
import com.example.biblioteca_app.databinding.DialogDenunciaBinding
import com.example.biblioteca_app.databinding.DialogSucessoDenunciaBinding
import com.example.biblioteca_app.databinding.ItemAvaliacaoBinding
import com.example.biblioteca_app.databinding.TelaLivroBinding
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView

class LivroActivity : AppCompatActivity() {

    private lateinit var binding: TelaLivroBinding

    private var curtido = false
    private var expandido = false
    private var solicitacaoEnviada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val livro = criarLivro()
        preencherTela(livro)
        configurarBotoes()
        setupNavBar()
    }

    private fun criarLivro(): Livro {
        return Livro(
            titulo = "Star Wars: A Vingança dos Sith",
            autor = "George Lucas",
            descricao = "Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império. Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império.",
            imagemRes = R.drawable.capa_star_wars,
            disponivel = true,
            media = 4.9f,
            totalAvaliacoes = 120
        )
    }

    private fun preencherTela(livro: Livro) {
        binding.txtTitulo.text = livro.titulo
        binding.txtAutor.text = livro.autor
        binding.txtDescricao.text = livro.descricao
        binding.imgCapa.setImageResource(livro.imagemRes)

        binding.txtStatus.text = if (livro.disponivel) getString(R.string.status_disponivel) else getString(R.string.status_indisponivel)

        // Preenche o resumo de avaliações (molde padronizado)
        binding.layoutResumo.txtMedia.text = livro.media.toString()
        binding.layoutResumo.txtTotalAvaliacoes.text = "(${livro.totalAvaliacoes} avaliações)"
        binding.layoutResumo.txtEstrelasMedia.text = converterMediaParaEstrelas(livro.media)

        if (livro.totalAvaliacoes == 0) {
            binding.txtSemAvaliacoes.visibility = View.VISIBLE
        }
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

        binding.btnCurtir.setOnClickListener {
            curtido = !curtido
            binding.btnCurtir.setImageResource(
                if (curtido) R.drawable.ic_heart_filled else R.drawable.ic_heart
            )
        }

        binding.btnVerMais.setOnClickListener {
            expandido = !expandido
            if (expandido) {
                binding.txtDescricao.maxLines = Int.MAX_VALUE
                binding.btnVerMais.text = getString(R.string.btn_ver_menos)
            } else {
                binding.txtDescricao.maxLines = 4
                binding.btnVerMais.text = getString(R.string.btn_ver_mais)
            }
        }

        binding.btnAlugar.setOnClickListener {
            if (!solicitacaoEnviada) {
                solicitacaoEnviada = true
                binding.btnAlugar.text = getString(R.string.btn_solicitacao_enviada)
                binding.btnAlugar.isEnabled = false
                Toast.makeText(this, getString(R.string.msg_solicitacao_enviada), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVerAvaliacoes.setOnClickListener {
            val intent = android.content.Intent(this, MaisAvaliacoesActivity::class.java)
            intent.putExtra("MEDIA", 4.9f)
            intent.putExtra("TOTAL", 4)
            startActivity(intent)
        }

        binding.btnAvaliar.setOnClickListener {
            val intent = android.content.Intent(this, AvaliarActivity::class.java)
            startActivity(intent)
        }

        // Configura cada item de avaliação com textos específicos
        configurarItemAvaliacao(
            binding.avaliacao1,
            "João Silva",
            "Incrível!",
            "Excelente leitura, recomendo a todos!",
            "15/05/2023"
        )
        configurarItemAvaliacao(
            binding.avaliacao2,
            "Maria Souza",
            "Muito bom",
            "O livro é bom, mas o final poderia ser melhor.",
            "20/06/2023"
        )

    }

    private fun configurarItemAvaliacao(
        itemBinding: ItemAvaliacaoBinding,
        nome: String,
        titulo: String,
        comentario: String,
        data: String
    ) {
        var curtido = false
        var numCurtidas = (0..20).random() // Valor inicial aleatório
        itemBinding.txtNomeUsuario.text = nome
        itemBinding.txtTituloAvaliacao.text = titulo
        itemBinding.txtComentario.text = comentario
        itemBinding.txtData.text = data
        itemBinding.txtCurtidas.text = numCurtidas.toString()

        // Garantir que o comentário esteja sempre visível
        itemBinding.txtComentario.visibility = View.VISIBLE

        // Clique de Denúncia
        itemBinding.btnDenunciar.setOnClickListener { mostrarDialogDenuncia() }

        // Clique de Curtir (Toggle)
        itemBinding.btnCurtir.setOnClickListener {
            curtido = !curtido
            if (curtido) {
                numCurtidas++
                itemBinding.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
                itemBinding.btnCurtir.clearColorFilter() // Remove o tint se houver
            } else {
                numCurtidas--
                itemBinding.btnCurtir.setImageResource(R.drawable.ic_heart)
                itemBinding.btnCurtir.clearColorFilter() // Garante que as bordas pretas do XML apareçam
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
            val selectedId = dialogBinding.radioGroup.checkedRadioButtonId
            
            if (selectedId == -1) {
                Toast.makeText(this, "Por favor, selecione um motivo para a denúncia", Toast.LENGTH_SHORT).show()
            } else if (selectedId == R.id.rbOutro && dialogBinding.edtOutro.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Por favor, descreva o motivo da denúncia", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                mostrarSucessoDenuncia()
            }
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

        bottomNav.selectedItemId = R.id.nav_busca

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
