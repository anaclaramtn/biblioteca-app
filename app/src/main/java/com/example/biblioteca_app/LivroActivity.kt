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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class LivroActivity : AppCompatActivity() {

    private lateinit var binding: TelaLivroBinding
    private val db = FirebaseFirestore.getInstance()

    private var expandido = false
    private var livroJaCurtido = false
    private var idHistoricoAtual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val livro = intent.getSerializableExtra("LIVRO") as? Livro ?: return

        preencherTela(livro)
        carregarResumoAvaliacoes(livro.id)
        carregarAvaliacoes(livro.id)
        verificarCurtida(livro.id)
        configurarBotoes(livro)
        setupNavBar()
        setupAlugar(livro)
        verificarStatusLivro(livro.id)
    }

    // =========================
    // LIVRO
    // =========================
    private fun preencherTela(livro: Livro) {
        binding.txtTitulo.text = livro.titulo
        binding.txtAutor.text = livro.autor
        binding.txtDescricao.text = livro.descricao

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
            media >= 4.5 -> "⭐⭐⭐⭐⭐"
            media >= 3.5 -> "⭐⭐⭐⭐☆"
            media >= 2.5 -> "⭐⭐⭐☆☆"
            media >= 1.5 -> "⭐⭐☆☆☆"
            media >= 0.5 -> "⭐☆☆☆☆"
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

                    val avaliacao = doc.toObject(Avaliacao::class.java).copy(id = doc.id)
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
                            item.txtEstrelas.text = converterEstrelas(avaliacao.nota)

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
                            // CURTIR E DENUNCIAR
                            // =========================
                            verificarCurtidaAvaliacao(avaliacaoId, item)

                            item.btnDenunciar.setOnClickListener {
                                mostrarDialogDenuncia(avaliacao, binding.txtTitulo.text.toString())
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar avaliações: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun verificarCurtidaAvaliacao(idAvaliacao: String, item: ItemAvaliacaoBinding) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("avaliacoesCurtidas")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("idAvaliacao", idAvaliacao)
            .get()
            .addOnSuccessListener { documents ->
                var curtidaDocId: String? = if (!documents.isEmpty) documents.documents[0].id else null
                var jaCurtido = curtidaDocId != null

                item.btnCurtir.setImageResource(
                    if (jaCurtido) R.drawable.ic_heart_filled else R.drawable.ic_heart
                )

                item.btnCurtir.setOnClickListener {
                    item.btnCurtir.isEnabled = false
                    val refAvaliacao = db.collection("avaliacoes").document(idAvaliacao)

                    if (jaCurtido) {
                        // Remover curtida
                        db.collection("avaliacoesCurtidas").document(curtidaDocId!!)
                            .delete()
                            .addOnSuccessListener {
                                jaCurtido = false
                                curtidaDocId = null
                                refAvaliacao.update("curtidas", FieldValue.increment(-1))
                                item.btnCurtir.setImageResource(R.drawable.ic_heart)
                                val novoTotal = (item.txtCurtidas.text.toString().toIntOrNull() ?: 1) - 1
                                item.txtCurtidas.text = novoTotal.toString()
                                item.btnCurtir.isEnabled = true
                            }
                            .addOnFailureListener {
                                item.btnCurtir.isEnabled = true
                            }
                    } else {
                        // Adicionar curtida
                        val data = hashMapOf("idUsuario" to uid, "idAvaliacao" to idAvaliacao)
                        db.collection("avaliacoesCurtidas").add(data)
                            .addOnSuccessListener { docRef ->
                                jaCurtido = true
                                curtidaDocId = docRef.id
                                refAvaliacao.update("curtidas", FieldValue.increment(1))
                                item.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
                                val novoTotal = (item.txtCurtidas.text.toString().toIntOrNull() ?: 0) + 1
                                item.txtCurtidas.text = novoTotal.toString()
                                item.btnCurtir.isEnabled = true
                            }
                            .addOnFailureListener {
                                item.btnCurtir.isEnabled = true
                            }
                    }
                }
            }
    }

    // =========================
    // ALUGAR E DEVOLVER
    // =========================
    private fun verificarStatusLivro(idLivro: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("historico")
            .whereEqualTo("idObjeto", idLivro)
            .whereEqualTo("tipoObjeto", "livro")
            .whereEqualTo("dataSaida", null)
            .get()
            .addOnSuccessListener { snapshots ->
                val alugadoPorAlguem = !snapshots.isEmpty
                val meuAluguel = snapshots.documents.find { it.getString("idUsuario") == uid }

                if (alugadoPorAlguem) {
                    binding.txtStatus.text = "Indisponível"
                    if (meuAluguel != null) {
                        idHistoricoAtual = meuAluguel.id
                        binding.btnAlugar.text = "Devolver"
                        binding.btnAlugar.isEnabled = true
                        binding.btnAlugar.alpha = 1.0f
                    } else {
                        idHistoricoAtual = null
                        binding.btnAlugar.text = "Indisponível"
                        binding.btnAlugar.isEnabled = false
                        binding.btnAlugar.alpha = 0.5f
                    }
                } else {
                    binding.txtStatus.text = "Disponível"
                    idHistoricoAtual = null

                    db.collection("solicitacoes")
                        .whereEqualTo("idUsuario", uid)
                        .whereEqualTo("idObjeto", idLivro)
                        .whereEqualTo("status", "pendente")
                        .get()
                        .addOnSuccessListener { pendSnap ->
                            if (!pendSnap.isEmpty) {
                                binding.btnAlugar.text = "Solicitação enviada"
                                binding.btnAlugar.isEnabled = false
                                binding.btnAlugar.alpha = 0.5f
                            } else {
                                binding.btnAlugar.text = "Alugar"
                                binding.btnAlugar.isEnabled = true
                                binding.btnAlugar.alpha = 1.0f
                            }
                        }
                }
            }
    }

    private fun setupAlugar(livro: Livro) {
        binding.btnAlugar.setOnClickListener {
            val texto = binding.btnAlugar.text.toString()
            if (texto == "Alugar") {
                realizarAluguel(livro)
            } else if (texto == "Devolver") {
                idHistoricoAtual?.let { realizarDevolucao(livro.id, it) }
            }
        }
    }

    private fun realizarAluguel(livro: Livro) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Verificar se o usuário já possui QUALQUER aluguel ativo (Livro, Sala ou Jogo)
        db.collection("historico")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("isDevolvido", false)
            .get()
            .addOnSuccessListener { snapshots ->
                val ocupado = snapshots.documents.any { hDoc ->
                    if (hDoc.getString("tipoObjeto") == "livro") {
                        true // Livro não devolvido
                    } else {
                        // Sala ou Jogo ainda no prazo de 2h
                        val dataSaida = hDoc.getTimestamp("dataSaida")
                        dataSaida != null && dataSaida.toDate().after(java.util.Date())
                    }
                }

                if (ocupado) {
                    Toast.makeText(this, "Você já possui um aluguel ativo e não pode solicitar outro!", Toast.LENGTH_LONG).show()
                } else {
                    val solicitacao = hashMapOf(
                        "idUsuario" to uid,
                        "idObjeto" to livro.id,
                        "tipoObjeto" to "livro",
                        "status" to "pendente",
                        "dataSolicitacao" to Timestamp.now(),
                        "isDevolucao" to false,
                        "dataResposta" to null
                    )

                    db.collection("solicitacoes").add(solicitacao)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Solicitação enviada", Toast.LENGTH_SHORT).show()
                            verificarStatusLivro(livro.id)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao solicitar aluguel", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun realizarDevolucao(idLivro: String, historicoId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val agora = Timestamp.now()

        // Atualizar histórico com dataSaida (obs: "data que o usuário solicita a devolução")
        db.collection("historico").document(historicoId)
            .update("dataSaida", agora)
            .addOnSuccessListener {
                val solicitacao = hashMapOf(
                    "idUsuario" to uid,
                    "idObjeto" to idLivro,
                    "tipoObjeto" to "livro",
                    "status" to "pendente",
                    "dataSolicitacao" to agora,
                    "isDevolucao" to true,
                    "dataResposta" to null
                )

                db.collection("solicitacoes").add(solicitacao)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Solicitação de devolução enviada!", Toast.LENGTH_SHORT).show()
                        verificarStatusLivro(idLivro)
                    }
            }
    }

    // =========================
    // DENÚNCIA
    // =========================
    private fun mostrarDialogDenuncia(avaliacao: Avaliacao, tituloLivro: String) {
        val dialogBinding = DialogDenunciaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelar.setOnClickListener {
            mostrarDialogConfirmacaoCancelar(dialog)
        }

        dialogBinding.btnEnviar.setOnClickListener {
            val selected = dialogBinding.radioGroup.checkedRadioButtonId

            if (selected == -1) {
                Toast.makeText(this, "Selecione um motivo", Toast.LENGTH_SHORT).show()
            } else {
                val motivo = when (selected) {
                    R.id.rbInadequado -> getString(R.string.motivo_inadequado)
                    R.id.rbIncorreto -> getString(R.string.motivo_incorreto)
                    R.id.rbSpam -> getString(R.string.motivo_spam)
                    R.id.rbOutro -> {
                        val textoOutro = dialogBinding.edtOutro.text.toString().trim()
                        if (textoOutro.isNotEmpty()) textoOutro else getString(R.string.motivo_outro)
                    }
                    else -> ""
                }

                val uidDenunciante = FirebaseAuth.getInstance().currentUser?.uid
                val denuncia = hashMapOf(
                    "idAvaliacao" to avaliacao.id,
                    "idLivro" to avaliacao.idLivro,
                    "tituloLivro" to tituloLivro,
                    "idAutorComentario" to avaliacao.idUsuario,
                    "idUsuarioDenunciante" to uidDenunciante,
                    "comentario" to avaliacao.descricao,
                    "motivo" to motivo,
                    "status" to "pendente",
                    "data" to Timestamp.now()
                )

                db.collection("denuncias")
                    .add(denuncia)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        mostrarDialogSucesso()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
        dialog.show()
    }

    private fun mostrarDialogSucesso() {
        val sucessoBinding = DialogSucessoDenunciaBinding.inflate(layoutInflater)
        val sucessoDialog = AlertDialog.Builder(this)
            .setView(sucessoBinding.root)
            .create()

        sucessoDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        sucessoBinding.btnOk.setOnClickListener {
            sucessoDialog.dismiss()
        }

        sucessoDialog.show()
    }

    private fun mostrarDialogConfirmacaoCancelar(dialogDenuncia: AlertDialog) {
        val confirmBinding = DialogConfirmacaoCancelarBinding.inflate(layoutInflater)
        val confirmDialog = AlertDialog.Builder(this)
            .setView(confirmBinding.root)
            .create()

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmBinding.btnNao.setOnClickListener {
            confirmDialog.dismiss()
        }

        confirmBinding.btnSim.setOnClickListener {
            confirmDialog.dismiss()
            dialogDenuncia.dismiss()
        }

        confirmDialog.show()
    }

    private fun verificarCurtida(idLivro: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.btnCurtir.isEnabled = false
        db.collection("livrosCurtidos")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("idLivro", idLivro)
            .get()
            .addOnSuccessListener { documents ->
                livroJaCurtido = !documents.isEmpty
                atualizarIconeCurtir()
                binding.btnCurtir.isEnabled = true
            }
            .addOnFailureListener {
                binding.btnCurtir.isEnabled = true
            }
    }

    private fun atualizarIconeCurtir() {
        if (livroJaCurtido) {
            binding.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
        } else {
            binding.btnCurtir.setImageResource(R.drawable.ic_heart)
        }
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
            intent.putExtra("TITULO_LIVRO", livro.titulo)
            startActivity(intent)
        }

        binding.btnAvaliar.setOnClickListener {
            val intent = Intent(this, AvaliarActivity::class.java)
            intent.putExtra("ID_LIVRO", livro.id)
            startActivity(intent)
        }

        binding.btnCurtir.setOnClickListener {
            toggleCurtir(livro.id)
        }
    }

    private fun toggleCurtir(idLivro: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Você precisa estar logado para curtir", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnCurtir.isEnabled = false
        if (livroJaCurtido) {
            // Remover curtida
            db.collection("livrosCurtidos")
                .whereEqualTo("idUsuario", uid)
                .whereEqualTo("idLivro", idLivro)
                .get()
                .addOnSuccessListener { documents ->
                    val batch = db.batch()
                    for (doc in documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().addOnSuccessListener {
                        livroJaCurtido = false
                        atualizarIconeCurtir()
                        binding.btnCurtir.isEnabled = true
                        Toast.makeText(this, "Removido dos curtidos", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        binding.btnCurtir.isEnabled = true
                        Toast.makeText(this, "Erro ao remover curtida", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    binding.btnCurtir.isEnabled = true
                }
        } else {
            // Adicionar curtida
            val curtida = hashMapOf(
                "idUsuario" to uid,
                "idLivro" to idLivro
            )

            db.collection("livrosCurtidos")
                .add(curtida)
                .addOnSuccessListener {
                    livroJaCurtido = true
                    atualizarIconeCurtir()
                    binding.btnCurtir.isEnabled = true
                    Toast.makeText(this, "Livro curtido!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    binding.btnCurtir.isEnabled = true
                    Toast.makeText(this, "Erro ao curtir livro", Toast.LENGTH_SHORT).show()
                }
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
