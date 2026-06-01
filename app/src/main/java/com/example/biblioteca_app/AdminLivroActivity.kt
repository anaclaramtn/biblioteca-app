package com.example.biblioteca_app

import android.content.Intent
import android.net.Uri
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
import com.example.biblioteca_app.models.Avaliacao
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.Button
import android.widget.TextView

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class AdminLivroActivity : AppCompatActivity() {

    private var livroPos: Int = -1
    private var livroAtual: Livro? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_livro)

        livroPos = intent.getIntExtra("LIVRO_POS", -1)
        livroAtual = intent.getSerializableExtra("LIVRO") as? Livro
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAcoes()
        setupNavBar()
    }

    override fun onResume() {
        super.onResume()
        preencherDadosLivro()
    }

    private fun preencherDadosLivro() {
        val livro = if (livroAtual != null) {
            livroAtual!!
        } else if (livroPos != -1 && livroPos < AcervoadmActivity.listaLivros.size) {
            AcervoadmActivity.listaLivros[livroPos]
        } else {
            // Fallback para exemplo
            Livro(
                id = "ID_LIVRO_TESTE_123",
                titulo = "Star Wars: A Vingança dos Sith",
                autor = "George Lucas",
                descricao = "Anakin Skywalker se torna Darth Vader...",
                disponivel = true,
                media = 4.9f,
                totalAvaliacoes = 120
            )
        }

        findViewById<TextView>(R.id.txtTitulo).text = livro.titulo
        findViewById<TextView>(R.id.txtAutor).text = livro.autor
        findViewById<TextView>(R.id.txtDescricao).text = livro.descricao
        val img = findViewById<android.widget.ImageView>(R.id.imgCapa)
        when {
            !livro.imagemBase64.isNullOrEmpty() -> {
                try {
                    val decodedBytes = android.util.Base64.decode(livro.imagemBase64, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    img.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    img.setImageResource(R.drawable.capadomquixote)
                }
            }
            livro.imagemRes != null && livro.imagemRes != 0 -> {
                img.setImageResource(livro.imagemRes)
            }
            else -> {
                img.setImageResource(R.drawable.capadomquixote)
            }
        }
        
        val layoutResumo = findViewById<View>(R.id.layoutResumo)
        layoutResumo.findViewById<TextView>(R.id.txtMedia).text = livro.media.toString()
        layoutResumo.findViewById<TextView>(R.id.txtTotalAvaliacoes).text = "(${livro.totalAvaliacoes} avaliações)"
        layoutResumo.findViewById<TextView>(R.id.txtEstrelasMedia).text = converterMediaParaEstrelas(livro.media)

        carregarAvaliacoes(livro.id)
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

    private fun carregarAvaliacoes(idLivro: String) {
        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val avaliacoes = documents.mapNotNull { it.toObject(Avaliacao::class.java).copy(id = it.id) }
                exibirAvaliacoes(avaliacoes)
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.txtSemAvaliacoes).visibility = View.VISIBLE
                findViewById<TextView>(R.id.txtSemAvaliacoes).text = "Erro ao carregar avaliações."
            }
    }

    private fun exibirAvaliacoes(avaliacoes: List<Avaliacao>) {
        val views = listOf(
            findViewById<View>(R.id.avaliacao1),
            findViewById<View>(R.id.avaliacao2),
            findViewById<View>(R.id.avaliacao3)
        )
        
        val txtSemAvaliacoes = findViewById<TextView>(R.id.txtSemAvaliacoes)
        
        if (avaliacoes.isEmpty()) {
            txtSemAvaliacoes.visibility = View.VISIBLE
            views.forEach { it.visibility = View.GONE }
        } else {
            txtSemAvaliacoes.visibility = View.GONE
            avaliacoes.forEachIndexed { index, avaliacao ->
                if (index < views.size) {
                    val itemView = views[index]
                    itemView.visibility = View.VISIBLE
                    
                    db.collection("usuarios").document(avaliacao.idUsuario).get()
                        .addOnSuccessListener { userDoc ->
                            val nome = userDoc.getString("nome") ?: "Usuário"
                            configurarItemAvaliacaoAdmin(
                                itemView,
                                nome,
                                avaliacao,
                                converterMediaParaEstrelas(avaliacao.nota)
                            )
                        }
                }
            }
        }
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
            val livro = livroAtual ?: (if (livroPos != -1) AcervoadmActivity.listaLivros[livroPos] else null)
            livro?.let {
                intent.putExtra("ID_LIVRO", it.id)
                intent.putExtra("MEDIA", it.media)
                intent.putExtra("TOTAL", it.totalAvaliacoes)
            }
            startActivity(intent)
        }
    }

    private fun configurarItemAvaliacaoAdmin(
        view: View,
        nome: String,
        avaliacao: Avaliacao,
        estrelas: String
    ) {
        val txtComentario = view.findViewById<TextView>(R.id.txtComentario)

        view.findViewById<TextView>(R.id.txtNomeUsuario).text = nome
        view.findViewById<TextView>(R.id.txtTituloAvaliacao).text = avaliacao.titulo
        txtComentario.text = avaliacao.descricao

        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm:ss a", Locale.ENGLISH)
        val dataFormatada = avaliacao.data?.toDate()?.let { sdf.format(it) } ?: "Data desconhecida"
        view.findViewById<TextView>(R.id.txtData).text = dataFormatada
        view.findViewById<TextView>(R.id.txtEstrelas).text = estrelas

        val txtCurtidas = view.findViewById<TextView>(R.id.txtCurtidas)
        txtCurtidas.text = avaliacao.curtidas.toString()

        // Lógica de Curtida
        verificarCurtidaAvaliacao(avaliacao.id, view)

        // Garantir que o comentário esteja sempre visível
        txtComentario.visibility = View.VISIBLE

        // Menu de moderação para o administrador
        view.findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menu.add("Deletar")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
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

    private fun verificarCurtidaAvaliacao(idAvaliacao: String, itemView: View) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val btnCurtir = itemView.findViewById<ImageButton>(R.id.btnCurtir)
        val txtCurtidas = itemView.findViewById<TextView>(R.id.txtCurtidas)

        db.collection("avaliacoesCurtidas")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("idAvaliacao", idAvaliacao)
            .get()
            .addOnSuccessListener { documents ->
                var jaCurtido = !documents.isEmpty

                btnCurtir.setImageResource(
                    if (jaCurtido) R.drawable.ic_heart_filled else R.drawable.ic_heart
                )

                btnCurtir.setOnClickListener {
                    btnCurtir.isEnabled = false
                    val refAvaliacao = db.collection("avaliacoes").document(idAvaliacao)

                    if (jaCurtido) {
                        // Remover curtida
                        db.collection("avaliacoesCurtidas")
                            .whereEqualTo("idUsuario", uid)
                            .whereEqualTo("idAvaliacao", idAvaliacao)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val batch = db.batch()
                                for (doc in querySnapshot) batch.delete(doc.reference)
                                batch.commit().addOnSuccessListener {
                                    jaCurtido = false
                                    refAvaliacao.update("curtidas", FieldValue.increment(-1))
                                    btnCurtir.setImageResource(R.drawable.ic_heart)
                                    val novoTotal = (txtCurtidas.text.toString().toIntOrNull() ?: 1) - 1
                                    txtCurtidas.text = novoTotal.toString()
                                    btnCurtir.isEnabled = true
                                }
                            }
                    } else {
                        // Adicionar curtida
                        val data = hashMapOf("idUsuario" to uid, "idAvaliacao" to idAvaliacao)
                        db.collection("avaliacoesCurtidas").add(data).addOnSuccessListener {
                            jaCurtido = true
                            refAvaliacao.update("curtidas", FieldValue.increment(1))
                            btnCurtir.setImageResource(R.drawable.ic_heart_filled)
                            val novoTotal = (txtCurtidas.text.toString().toIntOrNull() ?: 0) + 1
                            txtCurtidas.text = novoTotal.toString()
                            btnCurtir.isEnabled = true
                        }
                    }
                }
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