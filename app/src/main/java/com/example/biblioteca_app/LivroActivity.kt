package com.example.biblioteca_app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.*
import com.example.biblioteca_app.models.Avaliacao
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class LivroActivity : AppCompatActivity() {

    private lateinit var binding: TelaLivroBinding
    private val db = FirebaseFirestore.getInstance()

    private var expandido = false
    private var solicitacaoEnviada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val livro = intent.getSerializableExtra("LIVRO") as? Livro ?: return

        preencherTela(livro)
        carregarResumoAvaliacoes(livro.id)
        carregarAvaliacoes(livro.id)
        configurarBotoes(livro)
        setupNavBar()
        setupAlugar(livro)
    }

    // =========================
    // LIVRO
    // =========================
    private fun preencherTela(livro: Livro) {
        binding.txtTitulo.text = livro.titulo
        binding.txtAutor.text = livro.autor
        binding.txtDescricao.text = livro.descricao

        binding.txtStatus.text =
            if (livro.disponivel) "Disponível" else "Indisponível"

        binding.layoutResumo.txtMedia.text = "0.0"
        binding.layoutResumo.txtTotalAvaliacoes.text = "(0 avaliações)"
        binding.layoutResumo.txtEstrelasMedia.text = "☆☆☆☆☆"

        when {
            !livro.imagemBase64.isNullOrEmpty() -> {
                val bytes = Base64.decode(livro.imagemBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imgCapa.setImageBitmap(bmp)
            }
            livro.imagemRes != null -> {
                binding.imgCapa.setImageResource(livro.imagemRes!!)
            }
            else -> {
                binding.imgCapa.setImageResource(R.drawable.capadomquixote)
            }
        }
    }

    // =========================
    // RESUMO
    // =========================
    private fun carregarResumoAvaliacoes(idLivro: String) {
        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .get()
            .addOnSuccessListener { docs ->
                val notas = docs.mapNotNull { it.getDouble("nota")?.toFloat() }
                val total = notas.size
                val media = if (total > 0) notas.average().toFloat() else 0f

                binding.layoutResumo.txtMedia.text = String.format("%.1f", media)
                binding.layoutResumo.txtTotalAvaliacoes.text = "($total avaliações)"
                binding.layoutResumo.txtEstrelasMedia.text = converterEstrelas(media)

                binding.txtSemAvaliacoes.visibility =
                    if (total == 0) View.VISIBLE else View.GONE
            }
    }

    private fun converterEstrelas(media: Float): String {
        return when {
            media > 4.5 -> "⭐⭐⭐⭐⭐"
            media > 4 && media <= 4.5 -> "⭐⭐⭐⭐☆"
            media > 3 && media <= 3.9 -> "⭐⭐⭐☆☆"
            media > 2 && media <= 2.9 -> "⭐⭐☆☆☆"
            media > 1 && media <= 1.9 -> "⭐☆☆☆☆"
            media > 0 && media <= 0.9 -> "☆☆☆☆☆"
            else -> "☆☆☆☆☆"
        }
    }

    // =========================
    // TOP 3 AVALIAÇÕES (CORRIGIDO)
    // =========================
    private fun carregarAvaliacoes(idLivro: String) {
        // Consultando e ordenando por Curtidas (Principal) e Data (Secundário)
        db.collection("avaliacoes")
            .whereEqualTo("idLivro", idLivro)
            .orderBy("curtidas", Query.Direction.DESCENDING)
            .orderBy("data", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { docs ->
                val bindings = listOf(
                    binding.avaliacao1,
                    binding.avaliacao2,
                    binding.avaliacao3
                )

                bindings.forEach { it.root.visibility = View.GONE }

                docs.forEachIndexed { index, doc ->
                    if (index >= bindings.size) return@forEachIndexed

                    val avaliacao = doc.toObject(Avaliacao::class.java)
                    val avaliacaoId = doc.id

                    db.collection("usuarios")
                        .document(avaliacao.idUsuario)
                        .get()
                        .addOnSuccessListener { user ->
                            val nome = user.getString("nome") ?: "Usuário"
                            val item = bindings[index]

                            item.root.visibility = View.VISIBLE
                            item.txtNomeUsuario.text = nome
                            item.txtTituloAvaliacao.text = avaliacao.titulo
                            item.txtComentario.text = avaliacao.descricao
                            item.txtCurtidas.text = avaliacao.curtidas.toString()

                            // Tratamento e exibição da Data
                            val timestamp = doc.getTimestamp("data")
                            if (timestamp != null) {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                item.txtData.text = sdf.format(timestamp.toDate())
                                item.txtData.visibility = View.VISIBLE
                            } else {
                                item.txtData.visibility = View.GONE
                            }

                            // =========================
                            // CURTIR
                            // =========================
                            var localLike = false
                            item.btnCurtir.setOnClickListener {
                                val ref = db.collection("avaliacoes").document(avaliacaoId)

                                if (!localLike) {
                                    ref.update("curtidas", FieldValue.increment(1))
                                    avaliacao.curtidas++
                                    item.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
                                    localLike = true
                                } else {
                                    ref.update("curtidas", FieldValue.increment(-1))
                                    avaliacao.curtidas--
                                    item.btnCurtir.setImageResource(R.drawable.ic_heart)
                                    localLike = false
                                }
                                item.txtCurtidas.text = avaliacao.curtidas.toString()
                            }

                            // =========================
                            // DENUNCIAR
                            // =========================
                            item.btnDenunciar.setOnClickListener {
                                mostrarDialogDenuncia(avaliacaoId)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar avaliações: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // =========================
    // ALUGAR (CORRIGIDO)
    // =========================
    private fun setupAlugar(livro: Livro) {
        binding.btnAlugar.setOnClickListener {
            if (solicitacaoEnviada) return@setOnClickListener

            val data = hashMapOf(
                "idLivro" to livro.id,
                "status" to "pendente",
                "data" to Timestamp.now()
            )

            // Substituído "??????" por uma coleção padrão "solicitacoes_aluguel"
            db.collection("solicitacoes_aluguel")
                .add(data)
                .addOnSuccessListener {
                    solicitacaoEnviada = true
                    binding.btnAlugar.text = "Solicitação enviada"
                    binding.btnAlugar.isEnabled = false

                    Toast.makeText(this, "Solicitação enviada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao solicitar aluguel", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // =========================
    // DENÚNCIA
    // =========================
    private fun mostrarDialogDenuncia(idAvaliacao: String) {
        val dialogBinding = DialogDenunciaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnEnviar.setOnClickListener {
            val motivo = dialogBinding.edtOutro.text.toString()

            val denuncia = hashMapOf(
                "idAvaliacao" to idAvaliacao,
                "motivo" to motivo,
                "data" to Timestamp.now()
            )

            db.collection("denuncias")
                .add(denuncia)
                .addOnSuccessListener {
                    Toast.makeText(this, "Denúncia enviada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
        }

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // =========================
    // BOTÕES
    // =========================
    private fun configurarBotoes(livro: Livro) {
        binding.btnVoltar.setOnClickListener { finish() }

        binding.btnVerMais.setOnClickListener {
            expandido = !expandido
            binding.txtDescricao.maxLines =
                if (expandido) Int.MAX_VALUE else 4
        }

        binding.btnVerAvaliacoes.setOnClickListener {
            val intent = Intent(this, MaisAvaliacoesActivity::class.java)
            intent.putExtra("ID_LIVRO", livro.id)
            startActivity(intent)
        }

        binding.btnAvaliar.setOnClickListener {
            val intent = Intent(this, AvaliarActivity::class.java)
            intent.putExtra("ID_LIVRO", livro.id)
            startActivity(intent)
        }
    }

    // =========================
    // NAVBAR
    // =========================
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_busca

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TelaHomeActivity::class.java))
                R.id.nav_busca -> startActivity(Intent(this, BuscaActivity::class.java))
                R.id.nav_notif -> startActivity(Intent(this, NotificacoesActivity::class.java))
                R.id.nav_menu -> startActivity(Intent(this, MenuActivity::class.java))
                else -> return@setOnItemSelectedListener false
            }
            finish()
            true
        }
    }
}