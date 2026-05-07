package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMaisAvaliacoesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_admin_mais_avaliacoes)

        // HEADER
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val txtTitulo = findViewById<TextView>(R.id.txtTituloHeader)

        txtTitulo.text = "Avaliações"

        btnVoltar.setOnClickListener {
            finish()
        }

        // ORDENAR
        val btnOrdenar = findViewById<LinearLayout>(R.id.btnOrdenar)
        btnOrdenar.setOnClickListener {
            mostrarDialogOrdenacao()
        }

        // ITENS
        configurarItem(findViewById(R.id.avaliacao1), "João Silva", "Incrível!", "Excelente leitura!", "15/05/2023")
        configurarItem(findViewById(R.id.avaliacao2), "Maria Souza", "Muito bom", "Bom livro.", "20/06/2023")
        configurarItem(findViewById(R.id.avaliacao3), "Carlos Alberto", "Cuidado!", "Spoiler pesado...", "02/07/2023")
        configurarItem(findViewById(R.id.avaliacao4), "Ana Oliveira", "Gostei", "Muito bom.", "10/07/2023")

        setupNavBar()
    }

    private fun configurarItem(view: View, nome: String, titulo: String, comentario: String, data: String) {

        val txtNome = view.findViewById<TextView>(R.id.txtNomeUsuario)
        val txtTitulo = view.findViewById<TextView>(R.id.txtTituloAvaliacao)
        val txtComentario = view.findViewById<TextView>(R.id.txtComentario)
        val txtData = view.findViewById<TextView>(R.id.txtData)
        val txtCurtidas = view.findViewById<TextView>(R.id.txtCurtidas)

        val btnCurtir = view.findViewById<ImageButton>(R.id.btnCurtir)
        val btnMenu = view.findViewById<ImageButton>(R.id.btnMenu)
        val btnVerSpoiler = view.findViewById<Button>(R.id.btnVerSpoiler)

        txtNome.text = nome
        txtTitulo.text = titulo
        txtComentario.text = comentario
        txtData.text = data

        // Estado inicial do spoiler (caso queira já deixar oculto algum)
        btnVerSpoiler.visibility = View.GONE

        // CURTIDAS
        var curtidas = (0..50).random()
        var curtido = false

        txtCurtidas.text = curtidas.toString()

        btnCurtir.setOnClickListener {
            curtido = !curtido
            if (curtido) {
                curtidas++
                btnCurtir.setImageResource(R.drawable.ic_heart_filled)
            } else {
                curtidas--
                btnCurtir.setImageResource(R.drawable.ic_heart)
            }
            txtCurtidas.text = curtidas.toString()
        }

        // BOTÃO SPOILER (revelar comentário)
        btnVerSpoiler.setOnClickListener {
            btnVerSpoiler.visibility = View.GONE
            txtComentario.visibility = View.VISIBLE
        }

        // MENU ADMIN (IGUAL AO DA TELA DE LIVRO)
        btnMenu.setOnClickListener { v ->
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
                                (view.parent as? ViewGroup)?.removeView(view)
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

    private fun mostrarDialogOrdenacao() {
        val opcoes = arrayOf("Mais curtidas", "Menos curtidas", "Mais recentes", "Mais antigos")

        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setItems(opcoes) { _, which ->
                Toast.makeText(this, opcoes[which], Toast.LENGTH_SHORT).show()
            }
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