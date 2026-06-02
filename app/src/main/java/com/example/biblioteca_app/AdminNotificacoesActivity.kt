package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AdminNotificacoesActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val itensNotificacao = mutableListOf<Pair<View, TipoNotificacao>>()
    private lateinit var botoesFiltro: List<Button>
    private val db = FirebaseFirestore.getInstance()

    // Enum para definir qual molde usar e para filtragem
    enum class TipoNotificacao {
        LIVROS, JOGOS, SALAS, DEVOLUCOES, DENUNCIAS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_notificacoes)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        container = findViewById(R.id.containerSolicitacoes)

        setupFiltros()
        carregarNotificacoes()
        setupNavBar()
    }

    private fun carregarNotificacoes() {
        carregarDenuncias()
        carregarSolicitacoes()
    }

    private fun carregarDenuncias() {
        db.collection("denuncias")
            .whereIn("status", listOf("pendente", "visto"))
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val idDoc = doc.id
                    val status = doc.getString("status") ?: "pendente"
                    val tituloLivro = doc.getString("tituloLivro") ?: ""
                    val motivo = doc.getString("motivo") ?: ""
                    val comentario = doc.getString("comentario") ?: ""
                    val idLivro = doc.get("idLivro")?.toString() ?: ""
                    val idAvaliacao = doc.getString("idAvaliacao") ?: ""

                    // Informações formatadas para o admin
                    val nome = "Denúncia"
                    val email = "Motivo: $motivo"
                    val descricao = "Livro: $tituloLivro\n\"$comentario\""

                    adicionarItemNotificacao(
                        TipoNotificacao.DENUNCIAS,
                        nome,
                        email,
                        descricao,
                        extraInfo = idAvaliacao,
                        idDocumento = idDoc,
                        idRelacionado = idLivro,
                        colecao = "denuncias",
                        status = status
                    )
                }
            }
    }

    private fun carregarSolicitacoes() {
        db.collection("solicitacoes")
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val uid = doc.getString("idUsuario")
                    val idObjeto = doc.getString("idObjeto")
                    val tipoObjeto = doc.getString("tipoObjeto")
                    val idDoc = doc.id
                    val isDevolucao = doc.getBoolean("isDevolucao") ?: false

                    if (uid != null && idObjeto != null && tipoObjeto != null) {
                        db.collection("usuarios").document(uid).get().addOnSuccessListener { userDoc ->
                            val nome = userDoc.getString("nome") ?: "Usuário"
                            val email = userDoc.getString("email") ?: ""

                            val colecaoObjeto = when (tipoObjeto) {
                                "livro" -> "livros"
                                "jogo" -> "jogos"
                                "sala" -> "salas"
                                else -> ""
                            }

                            if (colecaoObjeto.isNotEmpty()) {
                                db.collection(colecaoObjeto).document(idObjeto).get().addOnSuccessListener { objDoc ->
                                    val nomeObjeto = if (tipoObjeto == "livro") objDoc.getString("titulo") else objDoc.getString("nome")
                                    val nomeFinal = nomeObjeto ?: "Item"

                                    val descricao = if (isDevolucao) {
                                        "Requerimento de devolução de ${tipoObjeto.replaceFirstChar { it.uppercase() }}\n'$nomeFinal'"
                                    } else {
                                        when (tipoObjeto) {
                                            "sala" -> "Requerimento de Sala: $nomeFinal"
                                            "jogo" -> "Requerimento de aluguel de Jogo\n\"$nomeFinal\""
                                            else -> "Requerimento de aluguel de Livro\n'$nomeFinal'"
                                        }
                                    }

                                    val tipoNotif = when (tipoObjeto) {
                                        "livro" -> TipoNotificacao.LIVROS
                                        "jogo" -> TipoNotificacao.JOGOS
                                        "sala" -> TipoNotificacao.SALAS
                                        else -> TipoNotificacao.LIVROS
                                    }

                                    adicionarItemNotificacao(
                                        tipoNotif,
                                        nome,
                                        email,
                                        descricao,
                                        idDocumento = idDoc,
                                        colecao = "solicitacoes",
                                        idRelacionado = idObjeto,
                                        idUsuario = uid,
                                        tipoObjeto = tipoObjeto
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun carregarSolicitacoesAluguel() {
        db.collection("solicitacoes_aluguel")
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val uid = doc.getString("idUsuario")
                    val idLivro = doc.get("idLivro")?.toString()
                    val idDoc = doc.id

                    if (uid != null && idLivro != null) {
                        db.collection("livros").document(idLivro).get().addOnSuccessListener { livroDoc ->
                            val tituloLivro = livroDoc.getString("titulo") ?: "Livro"

                            db.collection("usuarios").document(uid).get().addOnSuccessListener { userDoc ->
                                val nome = userDoc.getString("nome") ?: "Usuário"
                                val email = userDoc.getString("email") ?: ""
                                val descricao = "Requerimento de aluguel do Livro\n'$tituloLivro'"

                                adicionarItemNotificacao(
                                    TipoNotificacao.LIVROS,
                                    nome,
                                    email,
                                    descricao,
                                    idDocumento = idDoc,
                                    colecao = "solicitacoes_aluguel"
                                )
                            }
                        }
                    }
                }
            }
    }


    private fun setupFiltros() {
        val btnTodas = findViewById<Button>(R.id.btnFiltroTodas)
        val btnLivros = findViewById<Button>(R.id.btnFiltroLivros)
        val btnJogos = findViewById<Button>(R.id.btnFiltroJogos)
        val btnSalas = findViewById<Button>(R.id.btnFiltroSalas)
        val btnDevolucoes = findViewById<Button>(R.id.btnFiltroDevolucoes)
        val btnDenuncias = findViewById<Button>(R.id.btnFiltroDenuncias)

        botoesFiltro = listOf(btnTodas, btnLivros, btnJogos, btnSalas, btnDevolucoes, btnDenuncias)

        btnTodas.setOnClickListener { aplicarFiltro(null, btnTodas) }
        btnLivros.setOnClickListener { aplicarFiltro(TipoNotificacao.LIVROS, btnLivros) }
        btnJogos.setOnClickListener { aplicarFiltro(TipoNotificacao.JOGOS, btnJogos) }
        btnSalas.setOnClickListener { aplicarFiltro(TipoNotificacao.SALAS, btnSalas) }
        btnDevolucoes.setOnClickListener { aplicarFiltro(TipoNotificacao.DEVOLUCOES, btnDevolucoes) }
        btnDenuncias.setOnClickListener { aplicarFiltro(TipoNotificacao.DENUNCIAS, btnDenuncias) }
    }

    private fun aplicarFiltro(tipo: TipoNotificacao?, botaoSelecionado: Button) {
        // Filtrar visualmente os itens
        itensNotificacao.forEach { (view, t) ->
            if (tipo == null || t == tipo) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        // Atualizar estilo dos botões
        botoesFiltro.forEach { btn ->
            if (btn == botaoSelecionado) {
                btn.setBackgroundColor(android.graphics.Color.parseColor("#D1E8F3"))
            } else {
                btn.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }

    /**
     * Função Mestre: Adiciona qualquer tipo de notificação ao container
     */
    private fun adicionarItemNotificacao(
        tipo: TipoNotificacao,
        nome: String,
        email: String,
        descricao: String,
        extraInfo: String? = null,
        idRelacionado: String? = null,
        idDocumento: String? = null,
        colecao: String? = null,
        status: String = "pendente",
        idUsuario: String? = null,
        tipoObjeto: String? = null
    ) {
        val layoutRes = when (tipo) {
            TipoNotificacao.LIVROS, TipoNotificacao.JOGOS, TipoNotificacao.SALAS -> R.layout.item_solicitacao_aluguel
            TipoNotificacao.DEVOLUCOES -> R.layout.item_devolucao_admin
            TipoNotificacao.DENUNCIAS -> R.layout.item_denuncia_admin
        }

        val itemView = LayoutInflater.from(this).inflate(layoutRes, container, false)

        // Preencher campos comuns
        itemView.findViewById<TextView>(R.id.tvNomeUsuario).text = nome
        itemView.findViewById<TextView>(R.id.tvEmailUsuario).text = email
        itemView.findViewById<TextView>(R.id.tvDescricao).text = descricao

        // Configurar ações específicas
        when (tipo) {
            TipoNotificacao.LIVROS, TipoNotificacao.JOGOS, TipoNotificacao.SALAS -> {
                // ✅ Ao clicar na solicitação, a bolinha azul desaparece localmente
                itemView.setOnClickListener {
                    itemView.findViewById<View>(R.id.indicadorStatus)?.visibility = View.GONE
                }

                itemView.findViewById<Button>(R.id.btnAprovar).setOnClickListener {
                    confirmarAcao("Aprovar solicitação de $nome?") {
                        if (idDocumento != null && colecao != null) {
                            val dataResposta = Timestamp.now()
                            db.collection(colecao).document(idDocumento).update(
                                "status", "aceito",
                                "dataResposta", dataResposta
                            ).addOnSuccessListener {
                                if (colecao == "solicitacoes" && idUsuario != null && tipoObjeto != null) {
                                    val calendar = Calendar.getInstance()
                                    calendar.time = dataResposta.toDate()

                                    if (tipoObjeto == "sala" || tipoObjeto == "jogo") {
                                        calendar.add(Calendar.HOUR, 2)
                                    } else if (tipoObjeto == "livro") {
                                        calendar.add(Calendar.DAY_OF_YEAR, 30)
                                    }

                                    val dataPrazo = Timestamp(calendar.time)
                                    val dataSaida = if (tipoObjeto == "sala" || tipoObjeto == "jogo") dataPrazo else null

                                    val historico = hashMapOf(
                                        "dataEntrada" to dataResposta,
                                        "dataPrazo" to dataPrazo,
                                        "dataSaida" to dataSaida,
                                        "idObjeto" to idRelacionado,
                                        "idUsuario" to idUsuario,
                                        "isDevolvido" to false,
                                        "status" to "pendente",
                                        "tipoObjeto" to tipoObjeto
                                    )

                                    db.collection("historico").add(historico)
                                }
                                Toast.makeText(this, "Aprovado!", Toast.LENGTH_SHORT).show()
                                removerItem(itemView)
                            }.addOnFailureListener {
                                Toast.makeText(this, "Erro ao atualizar solicitação", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            removerItem(itemView)
                        }
                    }
                }
                itemView.findViewById<Button>(R.id.btnRecusar).setOnClickListener {
                    confirmarAcao("Recusar solicitação de $nome?") {
                        if (idDocumento != null && colecao != null) {
                            val dataResposta = Timestamp.now()
                            db.collection(colecao).document(idDocumento).update(
                                "status", "recusado",
                                "dataResposta", dataResposta
                            ).addOnSuccessListener {
                                Toast.makeText(this, "Recusado!", Toast.LENGTH_SHORT).show()
                                removerItem(itemView)
                            }.addOnFailureListener {
                                Toast.makeText(this, "Erro ao recusar solicitação", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            removerItem(itemView)
                        }
                    }
                }
            }
            TipoNotificacao.DEVOLUCOES -> {
                // ✅ Ao clicar na notificação, a bolinha azul desaparece localmente
                itemView.setOnClickListener {
                    itemView.findViewById<View>(R.id.indicadorStatus)?.visibility = View.GONE
                }

                itemView.findViewById<TextView>(R.id.tvDatas).text = extraInfo
                itemView.findViewById<Button>(R.id.btnConfirmarDevolucao).setOnClickListener {
                    confirmarAcao("Confirmar devolução de $nome?") {
                        Toast.makeText(this, "Devolução confirmada!", Toast.LENGTH_SHORT).show()
                        removerItem(itemView)
                    }
                }
            }
            TipoNotificacao.DENUNCIAS -> {
                val indicador = itemView.findViewById<View>(R.id.indicadorStatus)
                var currentStatus = status

                // Se já foi visto, esconde a bolinha azul permanentemente
                if (currentStatus == "visto") {
                    indicador?.visibility = View.GONE
                }

                // Ao clicar na notificação (item inteiro), o status fica 'visto' e a bolinha some
                itemView.setOnClickListener {
                    if (idDocumento != null && currentStatus == "pendente") {
                        db.collection("denuncias").document(idDocumento)
                            .update("status", "visto")
                            .addOnSuccessListener {
                                indicador?.visibility = View.GONE
                                currentStatus = "visto"
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao atualizar status", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                itemView.findViewById<Button>(R.id.btnVerificar).setOnClickListener {
                    if (idDocumento != null) {
                        // Ao clicar em verificar, o status já passa a ser 'resolvido'
                        db.collection("denuncias").document(idDocumento)
                            .update("status", "resolvido")
                            .addOnSuccessListener {
                                val intent = Intent(this, AdminMaisAvaliacoesActivity::class.java)
                                intent.putExtra("ID_LIVRO", idRelacionado)
                                intent.putExtra("ID_AVALIACAO_DENUNCIADA", extraInfo)
                                startActivity(intent)
                                removerItem(itemView)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao resolver denúncia", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
        container.addView(itemView)
        itensNotificacao.add(Pair(itemView, tipo))
    }

    private fun removerItem(view: View) {
        container.removeView(view)
        itensNotificacao.removeAll { it.first == view }
    }

    private fun confirmarAcao(mensagem: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Confirmação")
            .setMessage(mensagem)
            .setPositiveButton("Sim") { _, _ -> onConfirm() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_notif

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