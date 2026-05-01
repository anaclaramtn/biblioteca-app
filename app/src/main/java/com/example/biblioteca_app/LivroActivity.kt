package com.example.biblioteca_app

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
import com.example.biblioteca_app.databinding.TelaLivroBinding
import com.example.biblioteca_app.models.Livro

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
        binding.txtMedia.text = livro.media.toString()
        binding.txtTotalAvaliacoes.text = getString(R.string.total_avaliacoes_format, livro.totalAvaliacoes)

        if (livro.totalAvaliacoes == 0) {
            binding.txtSemAvaliacoes.visibility = View.VISIBLE
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

        // Configura o clique de denúncia nas avaliações incluídas
        binding.avaliacao1.btnDenunciar.setOnClickListener {
            mostrarDialogDenuncia()
        }
        binding.avaliacao2.btnDenunciar.setOnClickListener {
            mostrarDialogDenuncia()
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
}
