package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Notificacao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class NotificacoesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: GenericAdapter<Notificacao>
    private val lista = mutableListOf<Notificacao>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_notificacoes)

        setupRecycler()
        carregarNotificacoes()
        setupBotaoLimpar()
        setupNavBar()
    }

    private fun setupRecycler() {
        recycler = findViewById(R.id.recyclerNotificacoes)

        adapter = GenericAdapter(
            R.layout.item_notificacao,
            lista
        ) { view, item, position ->

            val titulo = view.findViewById<TextView>(R.id.titulo1)
            val msg = view.findViewById<TextView>(R.id.msg1)
            val data = view.findViewById<TextView>(R.id.data1)
            val indicador = view.findViewById<View>(R.id.indicadorNaoLido)
            val card = view as? androidx.cardview.widget.CardView

            titulo.text = item.titulo
            msg.text = item.mensagem
            data.text = item.data

            // 🔵 bolinha azul
            indicador.visibility = if (item.lida) View.GONE else View.VISIBLE

            // ✅ clicar = marcar como lido (sumir bolinha)
            card?.setOnClickListener {
                if (!item.lida) {
                    item.lida = true
                    indicador.visibility = View.GONE
                    adapter.updateItem(position, item)

                    // Persistir no Firestore (removemos o sufixo _env se existir)
                    val idFirestore = item.id.split("_")[0]
                    db.collection("solicitacoes").document(idFirestore)
                        .update("lidaUsuario", true)
                }
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun formatarData(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val sdfTime = SimpleDateFormat("hh:mm a", Locale.US)
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return "${sdfTime.format(timestamp.toDate()).lowercase()}\n${sdfDate.format(timestamp.toDate())}"
    }

    private fun carregarNotificacoes() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Carregar das Solicitações (Enviadas, Aprovadas, Recusadas, Devoluções)
        db.collection("solicitacoes")
            .whereEqualTo("idUsuario", uid)
            .get()
            .addOnSuccessListener { solicitacoesDocs ->
                val tempLista = mutableListOf<Notificacao>()
                var totalEsperado = solicitacoesDocs.size()

                // 2. Carregar do Histórico (Multas, Lembretes e Devoluções Automáticas)
                db.collection("historico")
                    .whereEqualTo("idUsuario", uid)
                    .whereEqualTo("isDevolvido", false)
                    .get()
                    .addOnSuccessListener { historicoDocs ->
                        totalEsperado += historicoDocs.size()

                        if (totalEsperado == 0) {
                            adapter.updateList(emptyList())
                            return@addOnSuccessListener
                        }

                        var processados = 0

                        fun checkConcluido() {
                            processados++
                            if (processados == totalEsperado) {
                                finalizarCarregamento(tempLista)
                            }
                        }

                        // Processar Solicitações
                        for (doc in solicitacoesDocs) {
                            val status = doc.getString("status")
                            val tipoObjeto = doc.getString("tipoObjeto")
                            val idObjeto = doc.getString("idObjeto")
                            val dataSolicitacao = doc.getTimestamp("dataSolicitacao")
                            val dataResposta = doc.getTimestamp("dataResposta")
                            val lida = doc.getBoolean("lidaUsuario") ?: false
                            val isDevolucao = doc.getBoolean("isDevolucao") ?: false

                            if (tipoObjeto != null && idObjeto != null) {
                                val colecao = when (tipoObjeto) {
                                    "livro" -> "livros"
                                    "jogo" -> "jogos"
                                    "sala" -> "salas"
                                    else -> ""
                                }

                                if (colecao.isNotEmpty()) {
                                    db.collection(colecao).document(idObjeto).get().addOnSuccessListener { objDoc ->
                                        val nomeObjeto = if (tipoObjeto == "livro") objDoc.getString("titulo") else objDoc.getString("nome")

                                        // Notificações de Aluguel
                                        if (!isDevolucao) {
                                            // Enviada
                                            if (dataSolicitacao != null) {
                                                val lidaEnv = if (status == "pendente" || status == "visto") lida else true
                                                tempLista.add(Notificacao(
                                                    id = doc.id + "_env",
                                                    titulo = "Administração - Solicitação",
                                                    mensagem = "Solicitação de aluguel do $tipoObjeto\n\"$nomeObjeto\" foi enviada.",
                                                    data = formatarData(dataSolicitacao),
                                                    lida = lidaEnv,
                                                    timestamp = dataSolicitacao.seconds
                                                ))
                                            }
                                            // Resposta (Aceito/Recusado)
                                            if (dataResposta != null && (status == "aceito" || status == "recusado")) {
                                                val acao = if (status == "aceito") "aprovado" else "recusado"
                                                tempLista.add(Notificacao(
                                                    id = doc.id,
                                                    titulo = "Administração - Solicitação",
                                                    mensagem = "Solicitação de aluguel do $tipoObjeto\n\"$nomeObjeto\" foi $acao.",
                                                    data = formatarData(dataResposta),
                                                    lida = lida,
                                                    timestamp = dataResposta.seconds
                                                ))
                                            }
                                        } else if (tipoObjeto == "livro") {
                                            // Notificações de Devolução (Exclusivas de Livro)
                                            // Enviada
                                            if (dataSolicitacao != null) {
                                                val lidaDevEnv = if (status == "pendente" || status == "visto") lida else true
                                                tempLista.add(Notificacao(
                                                    id = doc.id + "_dev_env",
                                                    titulo = "Administração - Devolução",
                                                    mensagem = "A solicitação de devolução do livro\n\"$nomeObjeto\" foi enviada.",
                                                    data = formatarData(dataSolicitacao),
                                                    lida = lidaDevEnv,
                                                    timestamp = dataSolicitacao.seconds
                                                ))
                                            }
                                            // Confirmada
                                            if (dataResposta != null && status == "aceito") {
                                                tempLista.add(Notificacao(
                                                    id = doc.id,
                                                    titulo = "Administração - Devolução",
                                                    mensagem = "A devolução do livro\n\"$nomeObjeto\" foi confirmada pelo administrador.",
                                                    data = formatarData(dataResposta),
                                                    lida = lida,
                                                    timestamp = dataResposta.seconds
                                                ))
                                            }
                                        }
                                        checkConcluido()
                                    }.addOnFailureListener { checkConcluido() }
                                } else { checkConcluido() }
                            } else { checkConcluido() }
                        }

                        // Processar Histórico (Multas, Lembretes e Devoluções Automáticas)
                        for (hDoc in historicoDocs) {
                            val idObjeto = hDoc.getString("idObjeto")
                            val tipoObjeto = hDoc.getString("tipoObjeto") ?: "livro"
                            val dataPrazo = hDoc.getTimestamp("dataPrazo")
                            val agora = Timestamp.now()

                            if (idObjeto != null && dataPrazo != null) {
                                val colecao = when (tipoObjeto) {
                                    "jogo" -> "jogos"
                                    "sala" -> "salas"
                                    else -> "livros"
                                }

                                db.collection(colecao).document(idObjeto).get().addOnSuccessListener { bDoc ->
                                    if (tipoObjeto == "livro") {
                                        val tituloLivro = bDoc.getString("titulo") ?: "Livro"

                                        val diffMillis = agora.toDate().time - dataPrazo.toDate().time
                                        val diffDias = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                                        if (diffMillis > 0) {
                                            // ATRASADO (Multa)
                                            val multa = diffDias * 0.50
                                            val msgMulta = "O livro \"$tituloLivro\" está atrasado $diffDias dias.\nMulta acumulada : R$ ${String.format(Locale.getDefault(), "%.2f", multa)}"

                                            tempLista.add(Notificacao(
                                                id = hDoc.id + "_multa",
                                                titulo = "Administração - Multa",
                                                mensagem = msgMulta,
                                                data = formatarData(agora),
                                                lida = false, // Multa sempre alerta
                                                timestamp = agora.seconds
                                            ))
                                        } else {
                                            // LEMBRETE (Próximos 3 dias)
                                            val diasRestantes = (-diffDias)
                                            if (diasRestantes in 0..3) {
                                                tempLista.add(Notificacao(
                                                    id = hDoc.id + "_lembrete",
                                                    titulo = "Administração - Lembrete",
                                                    mensagem = "O livro \"$tituloLivro\" deve ser devolvido em $diasRestantes dias.",
                                                    data = formatarData(agora),
                                                    lida = false,
                                                    timestamp = agora.seconds
                                                ))
                                            }
                                        }
                                    } else if (tipoObjeto == "jogo" || tipoObjeto == "sala") {
                                        val nomeObjeto = bDoc.getString("nome") ?: (if (tipoObjeto == "jogo") "Jogo" else "Sala")

                                        if (agora.toDate().after(dataPrazo.toDate())) {
                                            // DEVOLUÇÃO AUTOMÁTICA
                                            tempLista.add(Notificacao(
                                                id = hDoc.id + "_autodev",
                                                titulo = "Administração - Devolução",
                                                mensagem = "A ${tipoObjeto} \"$nomeObjeto\" foi devolvida automaticamente.",
                                                data = formatarData(dataPrazo),
                                                lida = false,
                                                timestamp = dataPrazo.seconds
                                            ))
                                        }
                                    }
                                    checkConcluido()
                                }.addOnFailureListener { checkConcluido() }
                            } else { checkConcluido() }
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar notificações", Toast.LENGTH_SHORT).show()
            }
    }

    private fun finalizarCarregamento(tempLista: List<Notificacao>) {
        val listaOrdenada = tempLista.sortedByDescending { it.timestamp }
        lista.clear()
        lista.addAll(listaOrdenada)
        adapter.updateList(lista)
    }

    private fun setupBotaoLimpar() {
        val btnLimpar = findViewById<TextView>(R.id.btnLimpar)

        btnLimpar.setOnClickListener {
            lista.forEach { notificacao ->
                if (!notificacao.lida) {
                    notificacao.lida = true
                    db.collection("solicitacoes").document(notificacao.id)
                        .update("lidaUsuario", true)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_notif

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
                R.id.nav_notif -> true
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