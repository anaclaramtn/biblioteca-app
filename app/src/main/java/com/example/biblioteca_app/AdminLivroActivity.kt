package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.Button
import android.widget.TextView

class AdminLivroActivity : AppCompatActivity() {

    private var livroPos: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_livro)

        livroPos = intent.getIntExtra("LIVRO_POS", -1)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAcoes()
        setupComentarios()
        setupNavBar()
    }

    override fun onResume() {
        super.onResume()
        preencherDadosLivro()
    }

    private fun preencherDadosLivro() {
        if (livroPos != -1 && livroPos < AcervoadmActivity.listaLivros.size) {
            val livro = AcervoadmActivity.listaLivros[livroPos]
            findViewById<TextView>(R.id.txtTitulo).text = livro.titulo
            findViewById<TextView>(R.id.txtAutor).text = livro.autor
            findViewById<TextView>(R.id.txtDescricao).text = livro.descricao
            findViewById<android.widget.ImageView>(R.id.imgCapa).setImageResource(livro.imagemRes)
        } else {
            // Fallback para exemplo se não vier posição
            findViewById<TextView>(R.id.txtTitulo).text = "Star Wars: A Vingança dos Sith"
            findViewById<TextView>(R.id.txtAutor).text = "George Lucas"
            findViewById<TextView>(R.id.txtDescricao).text = "Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força..."
            findViewById<android.widget.ImageView>(R.id.imgCapa).setImageResource(R.drawable.capa_star_wars)
        }
        
        // Resumo de avaliações (pode ser mockado ou vir do objeto se houver)
        val layoutResumo = findViewById<View>(R.id.layoutResumo)
        layoutResumo.findViewById<TextView>(R.id.txtMedia).text = "4.9"
        layoutResumo.findViewById<TextView>(R.id.txtTotalAvaliacoes).text = "(120 avaliações)"
        layoutResumo.findViewById<TextView>(R.id.txtEstrelasMedia).text = "⭐⭐⭐⭐⭐"
    }

    private fun setupAcoes() {
        // Botão Voltar
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        // Botão de Opções (Três Pontos) no Header
        val btnMenuOpcoes = findViewById<ImageButton>(R.id.btnMenuOpcoes)
        btnMenuOpcoes.setOnClickListener { view ->
            mostrarPopupMenu(view)
        }

        // Expandir Sinopse
        val txtDescricao = findViewById<TextView>(R.id.txtDescricao)
        val btnVerMaisDesc = findViewById<TextView>(R.id.btnVerMais)
        var expandido = false
        btnVerMaisDesc.setOnClickListener {
            expandido = !expandido
            if (expandido) {
                txtDescricao.maxLines = Int.MAX_VALUE
                btnVerMaisDesc.text = "Ver menos"
            } else {
                txtDescricao.maxLines = 4
                btnVerMaisDesc.text = "Ver mais"
            }
        }

        // Botão Ver mais avaliações -> AdminMaisAvaliacoesActivity
        findViewById<Button>(R.id.btnVerAvaliacoes).setOnClickListener {
            val intent = Intent(this, AdminMaisAvaliacoesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupComentarios() {
        // Mesmos comentários da LivroActivity
        configurarItemAvaliacaoAdmin(
            findViewById(R.id.avaliacao1),
            "João Silva",
            "Excelente leitura, recomendo a todos!",
            "15/05/2023",
            "⭐⭐⭐⭐⭐"
        )

        configurarItemAvaliacaoAdmin(
            findViewById(R.id.avaliacao2),
            "Maria Souza",
            "O livro é bom, mas o final poderia ser melhor.",
            "20/06/2023",
            "⭐⭐⭐⭐☆"
        )
    }

    private fun configurarItemAvaliacaoAdmin(
        view: View,
        nome: String,
        comentario: String,
        data: String,
        estrelas: String
    ) {
        val txtComentario = view.findViewById<TextView>(R.id.txtComentario)
        val btnVerSpoiler = view.findViewById<Button>(R.id.btnVerSpoiler)

        view.findViewById<TextView>(R.id.txtNomeUsuario).text = nome
        txtComentario.text = comentario
        view.findViewById<TextView>(R.id.txtData).text = data
        view.findViewById<TextView>(R.id.txtEstrelas).text = estrelas

        // Lógica do botão de Spoiler (revelar texto)
        btnVerSpoiler.setOnClickListener {
            btnVerSpoiler.visibility = View.GONE
            txtComentario.visibility = View.VISIBLE
        }

        // Menu de moderação para o administrador
        view.findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menu.add("Censurar")
            popup.menu.add("Deletar")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Censurar" -> {
                        txtComentario.visibility = View.GONE
                        btnVerSpoiler.visibility = View.VISIBLE
                        Toast.makeText(this, "Comentário censurado", Toast.LENGTH_SHORT).show()
                        true
                    }
                    "Deletar" -> {
                        AlertDialog.Builder(this)
                            .setTitle("Confirmação")
                            .setMessage("Tem certeza que deseja excluir o comentário?")
                            .setPositiveButton("Sim") { _, _ ->
                                (view.parent as? android.view.ViewGroup)?.removeView(view)
                                Toast.makeText(this, "Comentário deletado", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Não", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun mostrarPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Editar")
        popup.menu.add("Deletar")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Editar" -> {
                    val intent = Intent(this, AdminEdicaoActivity::class.java)
                    intent.putExtra("LIVRO_POS", livroPos)
                    startActivity(intent)
                    true
                }
                "Deletar" -> {
                    confirmarExclusao()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja deletar este livro? Esta ação não pode ser desfeita.")
            .setPositiveButton("Deletar") { _, _ ->
                Toast.makeText(this, "Livro deletado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        bottomNav.selectedItemId = R.id.nav_acervo

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_acervo -> {
                    startActivity(Intent(this, AcervoadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_usuarios -> {
                    startActivity(Intent(this, UsuarioadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_notif -> {
                    startActivity(Intent(this, AdminNotificacoesActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_menu -> {
                    startActivity(Intent(this, AdminMenuActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}