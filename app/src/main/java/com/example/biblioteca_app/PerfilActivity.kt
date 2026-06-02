package com.example.biblioteca_app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class PerfilActivity : AppCompatActivity() {

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private var idHistoricoAtual: String? = null
    private var idLivroAtual: String? = null
    private var curtidosListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    findViewById<TextView>(R.id.tvNomeUsuario).text = doc.getString("nome") ?: "Usuário"
                    findViewById<TextView>(R.id.tvEmailUsuario).text = doc.getString("email") ?: user.email
                }
        }

        // Configurar botão voltar
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar livros curtidos (carrossel)
        setupLivrosCurtidos()

        // Configurar minhas avaliações
        setupMinhasAvaliacoes()

        // Configurar clique no livro alugado (Título e Capa)
        val cliqueLivroAlugado = View.OnClickListener {
            idLivroAtual?.let { id ->
                db.collection("livros").document(id).get().addOnSuccessListener { doc ->
                    val livro = doc.toObject(com.example.biblioteca_app.models.Livro::class.java)?.copy(id = doc.id)
                    if (livro != null) {
                        val intent = Intent(this, LivroActivity::class.java)
                        intent.putExtra("LIVRO", livro)
                        startActivity(intent)
                    }
                }
            }
        }
        findViewById<TextView>(R.id.tvTituloLivro).setOnClickListener(cliqueLivroAlugado)
        findViewById<ImageView>(R.id.ivCapaLivro).setOnClickListener(cliqueLivroAlugado)

        setupNavBar()
        carregarLivroAlugado()
    }

    private fun carregarLivroAlugado() {
        val uid = auth.currentUser?.uid ?: return
        val cardAluguel = findViewById<androidx.cardview.widget.CardView>(R.id.cvAlugado) ?: return

        db.collection("historico")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("tipoObjeto", "livro")
            .whereEqualTo("dataSaida", null)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.isEmpty) {
                    cardAluguel.visibility = View.GONE
                } else {
                    cardAluguel.visibility = View.VISIBLE
                    val doc = snapshots.documents[0]
                    idHistoricoAtual = doc.id
                    val idLivro = doc.getString("idObjeto") ?: ""
                    idLivroAtual = idLivro
                    val dataPrazo = doc.getTimestamp("dataPrazo")

                    // Calcular Multa
                    if (dataPrazo != null) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        findViewById<TextView>(R.id.tvDevolucaoLivro).text = "Prazo : ${sdf.format(dataPrazo.toDate())}"

                        val agora = com.google.firebase.Timestamp.now().toDate().time
                        val prazo = dataPrazo.toDate().time
                        if (agora > prazo) {
                            val diff = agora - prazo
                            val diasAtraso = (diff / (1000 * 60 * 60 * 24)).toInt()
                            val multa = diasAtraso * 0.50
                            findViewById<TextView>(R.id.tvMulta).text = String.format("Valor da multa: R$ %.2f", multa)
                            findViewById<TextView>(R.id.tvMulta).setTextColor(Color.RED)
                        } else {
                            findViewById<TextView>(R.id.tvMulta).text = "Valor da multa: R$ 0,00"
                            findViewById<TextView>(R.id.tvMulta).setTextColor(Color.BLACK)
                        }
                    }

                    // Carregar detalhes do livro
                    db.collection("livros").document(idLivro).get()
                        .addOnSuccessListener { livroDoc ->
                            findViewById<TextView>(R.id.tvTituloLivro).text = livroDoc.getString("titulo") ?: "Livro"
                            val imgBase64 = livroDoc.getString("imagemBase64")
                            val ivCapa = findViewById<ImageView>(R.id.ivCapaLivro)
                            if (!imgBase64.isNullOrEmpty()) {
                                val bytes = android.util.Base64.decode(imgBase64, android.util.Base64.DEFAULT)
                                val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                ivCapa.setImageBitmap(bmp)
                            } else {
                                ivCapa.setImageResource(R.drawable.capadomquixote)
                            }
                        }

                    // Verificar se já existe solicitação de devolução pendente
                    db.collection("solicitacoes")
                        .whereEqualTo("idUsuario", uid)
                        .whereEqualTo("idObjeto", idLivro)
                        .whereEqualTo("status", "pendente")
                        .whereEqualTo("isDevolucao", true)
                        .get()
                        .addOnSuccessListener { solDocs ->
                            val btn = findViewById<Button>(R.id.btnDevolver)
                            if (!solDocs.isEmpty) {
                                btn.text = "Solicitação Enviada"
                                btn.isEnabled = false
                                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
                                btn.setTextColor(Color.WHITE)
                            } else {
                                btn.text = "DEVOLVER"
                                btn.isEnabled = true
                                btn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                                btn.setTextColor(Color.BLACK)
                            }
                        }
                }
            }
    }

    private fun setupLivrosCurtidos() {
        val user = auth.currentUser ?: return
        val container = findViewById<android.widget.LinearLayout>(R.id.containerCurtidos) ?: return

        // Usando SnapshotListener para atualizar em tempo real quando um livro for descurtido
        curtidosListener?.remove()
        curtidosListener = db.collection("livrosCurtidos")
            .whereEqualTo("idUsuario", user.uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Erro ao carregar curtidas", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                container.removeAllViews()

                if (snapshots == null || snapshots.isEmpty) {
                    val tvSemCurtidos = TextView(this)
                    tvSemCurtidos.text = "Você ainda não curtiu nenhum livro."
                    tvSemCurtidos.setPadding(20, 20, 20, 20)
                    container.addView(tvSemCurtidos)
                } else {
                    for (doc in snapshots) {
                        val idLivro = doc.getString("idLivro") ?: continue
                        
                        db.collection("livros").document(idLivro).get()
                            .addOnSuccessListener { livroDoc ->
                                if (livroDoc.exists()) {
                                    val livro = com.example.biblioteca_app.models.Livro(
                                        id = livroDoc.id,
                                        titulo = livroDoc.getString("titulo") ?: "",
                                        autor = livroDoc.getString("autor") ?: "",
                                        descricao = livroDoc.getString("sinopse") ?: "",
                                        imagemBase64 = livroDoc.getString("imagemBase64")
                                    )
                                    adicionarLivroAoCarrossel(container, livro)
                                }
                            }
                    }
                }
            }
    }

    private fun setupMinhasAvaliacoes() {
        val uid = auth.currentUser?.uid ?: return
        val container = findViewById<android.widget.LinearLayout>(R.id.containerAvaliacoes) ?: return
        val tvMedia = findViewById<TextView>(R.id.tvMediaNota)
        val tvTotal = findViewById<TextView>(R.id.tvQtdAvaliacoes)

        db.collection("avaliacoes")
            .whereEqualTo("idUsuario", uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                container.removeAllViews()
                val notas = mutableListOf<Float>()

                if (snapshots.isEmpty) {
                    tvMedia.text = "0.0"
                    tvTotal.text = "(0 avaliações)"
                    atualizarEstrelasMedia(0f)
                    val tvSem = TextView(this)
                    tvSem.text = "Você ainda não avaliou nenhum livro."
                    tvSem.setPadding(20, 20, 20, 20)
                    container.addView(tvSem)
                } else {
                    tvTotal.text = "(${snapshots.size()} avaliações)"
                    
                    for (doc in snapshots) {
                        val avaliacao = doc.toObject(com.example.biblioteca_app.models.Avaliacao::class.java).copy(id = doc.id)
                        notas.add(avaliacao.nota)
                        
                        // Inflar item de avaliação para o carrossel
                        val itemView = layoutInflater.inflate(R.layout.item_avaliacao, container, false)
                        
                        // Ajustar largura para carrossel (opcional, para não ocupar a tela toda se for horizontal)
                        val params = itemView.layoutParams
                        params.width = (resources.displayMetrics.widthPixels * 0.8).toInt()
                        itemView.layoutParams = params

                        // Preencher campos
                        itemView.findViewById<TextView>(R.id.txtNomeUsuario).text = "Minha Avaliação"
                        
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val dataStr = avaliacao.data?.toDate()?.let { sdf.format(it) } ?: ""
                        itemView.findViewById<TextView>(R.id.txtData).text = dataStr
                        
                        itemView.findViewById<TextView>(R.id.txtEstrelas).text = converterEstrelasParaString(avaliacao.nota)
                        itemView.findViewById<TextView>(R.id.txtTituloAvaliacao).text = avaliacao.titulo
                        itemView.findViewById<TextView>(R.id.txtComentario).text = avaliacao.descricao
                        itemView.findViewById<TextView>(R.id.txtCurtidas).text = avaliacao.curtidas.toString()
                        
                        // Desativar botões que não fazem sentido no próprio perfil (ou ajustar se quiser)
                        itemView.findViewById<View>(R.id.btnDenunciar).visibility = View.GONE

                        // Buscar título do livro para contextualizar
                        db.collection("livros").document(avaliacao.idLivro).get().addOnSuccessListener { livroDoc ->
                            val tituloLivro = livroDoc.getString("titulo") ?: "Livro"
                            // Podemos colocar o título do livro antes do comentário ou no lugar do nome
                            itemView.findViewById<TextView>(R.id.txtNomeUsuario).text = tituloLivro

                            // Clique para levar à tela do livro
                            itemView.setOnClickListener {
                                val livro = livroDoc.toObject(com.example.biblioteca_app.models.Livro::class.java)?.copy(id = livroDoc.id)
                                if (livro != null) {
                                    val intent = Intent(this@PerfilActivity, LivroActivity::class.java)
                                    intent.putExtra("LIVRO", livro)
                                    startActivity(intent)
                                }
                            }
                        }

                        container.addView(itemView)
                    }

                    val media = notas.average().toFloat()
                    tvMedia.text = String.format("%.1f", media)
                    atualizarEstrelasMedia(media)
                }
            }
    }

    private fun converterEstrelasParaString(nota: Float): String {
        val cheias = nota.toInt()
        val vazias = 5 - cheias
        return "⭐".repeat(cheias) + "☆".repeat(vazias)
    }

    private fun atualizarEstrelasMedia(media: Float) {
        val stars = listOf(
            findViewById<ImageView>(R.id.star1),
            findViewById<ImageView>(R.id.star2),
            findViewById<ImageView>(R.id.star3),
            findViewById<ImageView>(R.id.star4),
            findViewById<ImageView>(R.id.star5)
        )

        val mediaInt = media.toInt()
        for (i in 0 until 5) {
            if (i < mediaInt) {
                stars[i].setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                stars[i].setImageResource(android.R.drawable.btn_star_big_off)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        curtidosListener?.remove()
    }

    private fun adicionarLivroAoCarrossel(container: android.widget.LinearLayout, livro: com.example.biblioteca_app.models.Livro) {
        val view = layoutInflater.inflate(R.layout.item_livro, container, false)
        view.findViewById<TextView>(R.id.txtTituloLivro).text = livro.titulo
        view.findViewById<TextView>(R.id.txtAutor).text = livro.autor
        val img = view.findViewById<ImageView>(R.id.imgCapa)

        if (!livro.imagemBase64.isNullOrEmpty()) {
            try {
                val bytes = android.util.Base64.decode(livro.imagemBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                img.setImageBitmap(bitmap)
            } catch (e: Exception) {
                img.setImageResource(R.drawable.capadomquixote)
            }
        } else {
            img.setImageResource(R.drawable.capadomquixote)
        }

        view.setOnClickListener {
            val intent = Intent(this, LivroActivity::class.java)
            intent.putExtra("LIVRO", livro)
            startActivity(intent)
        }
        container.addView(view)
    }

    fun onDevolverClick(view: View) {
        val uid = auth.currentUser?.uid ?: return
        val histId = idHistoricoAtual ?: return
        val livroId = idLivroAtual ?: return
        val agora = com.google.firebase.Timestamp.now()

        // 1. Atualizar histórico com dataSaida (solicitação de devolução)
        db.collection("historico").document(histId)
            .update("dataSaida", agora)
            .addOnSuccessListener {
                // 2. Criar solicitação de devolução
                val solicitacao = hashMapOf(
                    "idUsuario" to uid,
                    "idObjeto" to livroId,
                    "tipoObjeto" to "livro",
                    "status" to "pendente",
                    "dataSolicitacao" to agora,
                    "isDevolucao" to true,
                    "dataResposta" to null
                )

                db.collection("solicitacoes").add(solicitacao)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Solicitação de devolução enviada!", Toast.LENGTH_SHORT).show()
                        carregarLivroAlugado()
                    }
            }
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