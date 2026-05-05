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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_livro)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preencherDadosLivro()
        setupAcoes()
        setupComentarios()
        setupNavBar()
    }

    private fun preencherDadosLivro() {
        findViewById<TextView>(R.id.txtTitulo).text = "Star Wars: A Vingança dos Sith"
        findViewById<TextView>(R.id.txtAutor).text = "George Lucas"
        findViewById<TextView>(R.id.txtDescricao).text = "Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império. Anakin Skywalker se torna Darth Vader após ser seduzido pelo lado sombrio da Força. Uma história de queda, tragédia e redenção que marca o fim da República e o surgimento do Império."
        findViewById<android.widget.ImageView>(R.id.imgCapa).setImageResource(R.drawable.capa_star_wars)
        
        // Resumo de avaliações
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
                btnVerMaisDesc.text = getString(R.string.btn_ver_menos)
            } else {
                txtDescricao.maxLines = 4
                btnVerMaisDesc.text = getString(R.string.btn_ver_mais)
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
        view.findViewById<TextView>(R.id.txtNomeUsuario).text = nome
        view.findViewById<TextView>(R.id.txtComentario).text = comentario
        view.findViewById<TextView>(R.id.txtData).text = data
        view.findViewById<TextView>(R.id.txtEstrelas).text = estrelas

        // Menu de moderação para o administrador
        view.findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menu.add("Censurar como Spoiler")
            popup.menu.add("Banir Usuário")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Censurar como Spoiler" -> {

                        Toast.makeText(this, "Comentário ocultado", Toast.LENGTH_SHORT).show()
                        true
                    }
                    "Banir Usuário" -> {
                        AlertDialog.Builder(this)
                            .setTitle("Banir Usuário")
                            .setMessage("Deseja banir $nome?")
                            .setPositiveButton("Banir") { _, _ ->
                                Toast.makeText(this, "Usuário banido", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Cancelar", null)
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
                    Toast.makeText(this, "Abrindo tela de edição...", Toast.LENGTH_SHORT).show()
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