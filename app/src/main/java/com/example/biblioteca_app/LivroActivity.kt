package com.example.biblioteca_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.R
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
        configurarBotoes(livro)
    }

    private fun criarLivro(): Livro {
        return Livro(
            titulo = "Star Wars: A Vingança dos Sith",
            autor = "George Lucas",
            descricao = "Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império.Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império. Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império",
            imagemRes = R.drawable.capa_star_wars,
            disponivel = true,
            media = 4.9f,
            totalAvaliacoes = 120
        )
    }

    private fun preencherTela(livro: Livro) {
        //isso eh so um debug pra ver se este caralho estava rodando, depois vou remover
        Toast.makeText(this, "preencherTela chamado: ${livro.titulo}", Toast.LENGTH_LONG).show()
        binding.txtTitulo.text = livro.titulo

        binding.txtTitulo.text = livro.titulo
        binding.txtAutor.text = livro.autor
        binding.txtDescricao.text = livro.descricao
        binding.imgCapa.setImageResource(livro.imagemRes)

        binding.txtStatus.text =
            if (livro.disponivel) "Disponível" else "Indisponível"

        binding.txtMedia.text = livro.media.toString()
        binding.txtTotalAvaliacoes.text =
            "${livro.totalAvaliacoes} avaliações"

        // caso sem avaliações
        if (livro.totalAvaliacoes == 0) {
            binding.txtSemAvaliacoes.visibility = android.view.View.VISIBLE
        }
    }

    private fun configurarBotoes(livro: Livro) {

        // VOLTAR
        binding.btnVoltar.setOnClickListener {
            finish()
        }

        // CURTIR
        binding.btnCurtir.setOnClickListener {
            curtido = !curtido

            if (curtido) {
                binding.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
            } else {
                binding.btnCurtir.setImageResource(R.drawable.ic_heart)
            }
        }

        // VER MAIS / MENOS
        binding.btnVerMais.setOnClickListener {
            expandido = !expandido

            if (expandido) {
                binding.txtDescricao.maxLines = Int.MAX_VALUE
                binding.btnVerMais.text = "Ver menos"
            } else {
                binding.txtDescricao.maxLines = 4
                binding.btnVerMais.text = "Ver mais"
            }
        }

        // ALUGAR
        binding.btnAlugar.setOnClickListener {

            if (!solicitacaoEnviada) {
                solicitacaoEnviada = true

                binding.btnAlugar.text = "Solicitação enviada"
                binding.btnAlugar.isEnabled = false

                Toast.makeText(
                    this,
                    "Sua solicitação foi enviada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // VER MAIS AVALIAÇÕES (placeholder)
        binding.btnVerAvaliacoes.setOnClickListener {
            Toast.makeText(this, "Ir para tela de avaliações", Toast.LENGTH_SHORT).show()
        }

        // AVALIAR (placeholder)
        binding.btnAvaliar.setOnClickListener {
            Toast.makeText(this, "Ir para tela de nova avaliação", Toast.LENGTH_SHORT).show()
        }
    }
}