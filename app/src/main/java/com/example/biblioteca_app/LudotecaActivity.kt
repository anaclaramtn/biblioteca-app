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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Jogo
import com.example.biblioteca_app.models.Sala
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LudotecaActivity : AppCompatActivity() {

    private lateinit var rvJogos: RecyclerView
    private lateinit var rvSalas: RecyclerView
    private val listaJogos = mutableListOf<Jogo>()
    private val listaSalas = mutableListOf<Sala>()
    private lateinit var adapterJogos: GenericAdapter<Jogo>
    private lateinit var adapterSalas: GenericAdapter<Sala>
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_ludoteca)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val tabJogos = findViewById<TextView>(R.id.tabJogos)
        val tabSalas = findViewById<TextView>(R.id.tabSalas)
        rvJogos = findViewById(R.id.rvJogos)
        rvSalas = findViewById(R.id.rvSalas)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupRecyclerViews()
        carregarDadosFirestore()

        tabJogos.setOnClickListener {
            tabJogos.setBackgroundColor(Color.parseColor("#E3F2FD"))
            tabJogos.setTextColor(Color.parseColor("#002D5E"))
            tabSalas.setBackgroundColor(Color.WHITE)
            tabSalas.setTextColor(Color.BLACK)
            rvJogos.visibility = View.VISIBLE
            rvSalas.visibility = View.GONE
        }

        tabSalas.setOnClickListener {
            tabSalas.setBackgroundColor(Color.parseColor("#E3F2FD"))
            tabSalas.setTextColor(Color.parseColor("#002D5E"))
            tabJogos.setBackgroundColor(Color.WHITE)
            tabJogos.setTextColor(Color.BLACK)
            rvJogos.visibility = View.GONE
            rvSalas.visibility = View.VISIBLE
        }
        setupNavBar()
    }

    private fun setupRecyclerViews() {
        adapterJogos = GenericAdapter(R.layout.item_jogo_ludoteca, listaJogos) { view, item, _ ->
            view.findViewById<TextView>(R.id.txtNomeJogo).text = item.nome
            val imgJogo = view.findViewById<ImageView>(R.id.imgJogo)
            val btnAlugar = view.findViewById<Button>(R.id.btnAlugar)

            verificarStatus(item.id, "jogo", btnAlugar)

            when {
                !item.imagemBase64.isNullOrEmpty() -> {
                    try {
                        val decodedBytes = android.util.Base64.decode(item.imagemBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        imgJogo.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imgJogo.setImageResource(R.drawable.uno)
                    }
                }
                item.imagemRes != 0 -> {
                    imgJogo.setImageResource(item.imagemRes)
                }
                else -> {
                    imgJogo.setImageResource(R.drawable.uno)
                }
            }

            btnAlugar.setOnClickListener {
                solicitarAluguel(item.id, "jogo", btnAlugar)
            }
        }
        rvJogos.layoutManager = LinearLayoutManager(this)
        rvJogos.adapter = adapterJogos

        adapterSalas = GenericAdapter(R.layout.item_sala_ludoteca, listaSalas) { view, item, _ ->
            view.findViewById<TextView>(R.id.txtNomeSala).text = item.nome
            view.findViewById<TextView>(R.id.txtStatusSala).text = "Disponível\nCapacidade: ${item.capacidade} pessoas"
            val btnReservar = view.findViewById<Button>(R.id.btnReservar)

            verificarStatus(item.id, "sala", btnReservar)

            btnReservar.setOnClickListener {
                solicitarAluguel(item.id, "sala", btnReservar)
            }
        }
        rvSalas.layoutManager = LinearLayoutManager(this)
        rvSalas.adapter = adapterSalas
    }

    private fun verificarStatus(idObjeto: String, tipo: String, botao: Button) {
        val uid = userId ?: return

        // 1. Verifica se o item está ocupado por QUALQUER usuário (incluindo o atual)
        db.collection("historico")
            .whereEqualTo("idObjeto", idObjeto)
            .whereEqualTo("isDevolvido", false)
            .get()
            .addOnSuccessListener { snapshots ->
                var activeRentalDoc: com.google.firebase.firestore.DocumentSnapshot? = null
                val agora = java.util.Date()

                for (doc in snapshots.documents) {
                    val dataSaida = doc.getTimestamp("dataSaida")?.toDate()
                    val statusResolvido = doc.getString("status") == "resolvido"
                    val isDevolvidoPorTempo = if (dataSaida != null) agora.after(dataSaida) else false

                    if (!isDevolvidoPorTempo && !statusResolvido) {
                        activeRentalDoc = doc
                        break
                    }
                }

                if (activeRentalDoc != null) {
                    val renterId = activeRentalDoc.getString("idUsuario")
                    if (renterId == uid) {
                        // É o próprio usuário: mostra o prazo
                        val dataPrazo = activeRentalDoc.getTimestamp("dataPrazo")?.toDate()
                        if (dataPrazo != null) {
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            botao.text = "Prazo: ${sdf.format(dataPrazo)}"
                        } else {
                            botao.text = "Emprestado"
                        }
                    } else {
                        // É outro usuário: mostra indisponível
                        botao.text = "Indisponível"
                    }
                    botao.isEnabled = false
                    botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
                } else {
                    // 2. Se não estiver ocupado, verifica solicitações pendentes do usuário atual
                    db.collection("solicitacoes")
                        .whereEqualTo("idUsuario", uid)
                        .whereEqualTo("idObjeto", idObjeto)
                        .whereEqualTo("status", "pendente")
                        .get()
                        .addOnSuccessListener { pendSnap ->
                            if (!pendSnap.isEmpty) {
                                botao.text = "Solicitação enviada"
                                botao.isEnabled = false
                                botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
                            } else {
                                resetBotao(botao, tipo)
                            }
                        }
                }
            }
    }

    private fun resetBotao(botao: Button, tipo: String) {
        botao.text = if (tipo == "jogo") "Alugar" else "Reservar"
        botao.isEnabled = true
        botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#002D5E"))
        botao.setTextColor(Color.WHITE)
    }

    private fun solicitarAluguel(idObjeto: String, tipo: String, botao: Button) {
        val uid = userId ?: run {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Verificar se o usuário já possui algo do MESMO TIPO alugado ativamente
        db.collection("historico")
            .whereEqualTo("idUsuario", uid)
            .whereEqualTo("tipoObjeto", tipo)
            .whereEqualTo("isDevolvido", false)
            .get()
            .addOnSuccessListener { snapshots ->
                val ocupado = snapshots.documents.any { hDoc ->
                    if (tipo == "livro") {
                        true
                    } else {
                        // Para jogo/sala, verifica se ainda está no prazo de 2h (dataSaida)
                        val dataSaida = hDoc.getTimestamp("dataSaida")?.toDate()
                        dataSaida != null && dataSaida.after(java.util.Date())
                    }
                }

                if (ocupado) {
                    val msg = if (tipo == "jogo") "Você já possui um jogo alugado!" else "Você já possui uma sala reservada!"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 2. Verificar se já existe uma solicitação pendente para este item específico
                db.collection("solicitacoes")
                    .whereEqualTo("idUsuario", uid)
                    .whereEqualTo("idObjeto", idObjeto)
                    .whereEqualTo("status", "pendente")
                    .get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            Toast.makeText(this, "Você já possui uma solicitação pendente para este item.", Toast.LENGTH_SHORT).show()
                            botao.text = "Solicitação enviada"
                            botao.isEnabled = false
                            botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
                            return@addOnSuccessListener
                        }

                        // Se não houver pendente, procede com a solicitação
                        val solicitacao = hashMapOf(
                            "idUsuario" to uid,
                            "idObjeto" to idObjeto,
                            "tipoObjeto" to tipo,
                            "status" to "pendente",
                            "dataSolicitacao" to com.google.firebase.Timestamp.now(),
                            "isDevolucao" to false,
                            "dataResposta" to null
                        )

                        db.collection("solicitacoes").add(solicitacao)
                            .addOnSuccessListener {
                                botao.text = "Solicitação enviada"
                                botao.isEnabled = false
                                botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
                                Toast.makeText(this, "Solicitação enviada com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao enviar solicitação", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
    }

    private fun carregarDadosFirestore() {
        // Carregar Jogos
        db.collection("jogos").get().addOnSuccessListener { documentos ->
            listaJogos.clear()
            for (doc in documentos) {
                val jogo = Jogo(
                    id = doc.id,
                    nome = doc.getString("nome") ?: "",
                    imagemRes = (doc.getLong("imagemRes") ?: R.drawable.uno.toLong()).toInt(),
                    imagemBase64 = doc.getString("imagemBase64")
                )
                listaJogos.add(jogo)
            }
            adapterJogos.updateList(listaJogos)
        }

        // Carregar Salas
        db.collection("salas").get().addOnSuccessListener { documentos ->
            listaSalas.clear()
            for (doc in documentos) {
                val sala = Sala(
                    id = doc.id,
                    nome = doc.getString("nome") ?: "",
                    capacidade = (doc.getLong("capacidade") ?: 0).toInt()
                )
                listaSalas.add(sala)
            }
            adapterSalas.updateList(listaSalas)
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
