package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.models.Avaliacao
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class AdminMaisAvaliacoesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idLivro: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_admin_mais_avaliacoes)

        idLivro = intent.getStringExtra("ID_LIVRO") ?: ""

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

        carregarAvaliacoes()
        setupNavBar()
    }

    private fun carregarAvaliacoes(ordem: Query.Direction = Query.Direction.DESCENDING) {
        if (idLivro.isEmpty()) return

        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy("data", ordem)
            .get()
            .addOnSuccessListener { documents ->
                val container = findViewById<LinearLayout>(R.id.containerAvaliacoes)
                container.removeAllViews()
                val avaliacoes = documents.mapNotNull { it.toObject(Avaliacao::class.java).copy(id = it.id) }
                
                avaliacoes.forEach { avaliacao ->
                    adicionarItemAvaliacao(avaliacao)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar avaliações", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarItemAvaliacao(avaliacao: Avaliacao) {
        val container = findViewById<LinearLayout>(R.id.containerAvaliacoes)
        val itemView = layoutInflater.inflate(R.layout.item_admin_avaliacao, container, false)
        container.addView(itemView)

        db.collection("usuarios").document(avaliacao.idUsuario).get()
            .addOnSuccessListener { userDoc ->
                val nome = userDoc.getString("nome") ?: "Usuário"
                configurarItem(itemView, nome, avaliacao.titulo, avaliacao.descricao, avaliacao.data, avaliacao.nota)
            }
    }

    private fun configurarItem(view: View, nome: String, titulo: String, comentario: String, data: Timestamp?, nota: Float) {
        val txtNome = view.findViewById<TextView>(R.id.txtNomeUsuario)
        val txtTitulo = view.findViewById<TextView>(R.id.txtTituloAvaliacao)
        val txtComentario = view.findViewById<TextView>(R.id.txtComentario)
        val txtData = view.findViewById<TextView>(R.id.txtData)
        val txtEstrelas = view.findViewById<TextView>(R.id.txtEstrelas)

        val btnMenu = view.findViewById<ImageButton>(R.id.btnMenu)

        txtNome.text = nome
        txtTitulo.text = titulo
        txtComentario.text = comentario
        txtEstrelas.text = converterMediaParaEstrelas(nota)
        
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm:ss a", Locale.ENGLISH)
        val dataFormatada = data?.toDate()?.let { sdf.format(it) } ?: "Data desconhecida"
        txtData.text = dataFormatada

        txtComentario.visibility = View.VISIBLE

        btnMenu.setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menu.add("Deletar")
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
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

    private fun mostrarDialogOrdenacao() {
        val opcoes = arrayOf("Mais recentes", "Mais antigos")

        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> carregarAvaliacoes(Query.Direction.DESCENDING)
                    1 -> carregarAvaliacoes(Query.Direction.ASCENDING)
                }
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