package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var containerHistorico: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_historico)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerHistorico = findViewById(R.id.containerHistorico)

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener { finish() }

        setupHistorico()
        setupNavBar()
    }

    private fun setupHistorico() {
        val user = auth.currentUser ?: return
        
        // Limpar container antes de carregar (caso de refresh ou onResume)
        containerHistorico.removeAllViews()

        db.collection("historico")
            .whereEqualTo("idUsuario", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val tvVazio = TextView(this)
                    tvVazio.text = "Nenhum empréstimo no histórico."
                    tvVazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    tvVazio.setPadding(0, 50, 0, 0)
                    containerHistorico.addView(tvVazio)
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val tipo = doc.getString("tipoObjeto") ?: ""
                    val idObjeto = doc.getString("idObjeto") ?: ""
                    val dataEntrada = doc.getTimestamp("dataEntrada")
                    val dataSaida = doc.getTimestamp("dataSaida")
                    val dataPrazo = doc.getTimestamp("dataPrazo")
                    val isDevolvido = doc.getBoolean("isDevolvido") ?: false

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val entradaStr = dataEntrada?.toDate()?.let { sdf.format(it) } ?: "-"
                    val saidaStr = dataSaida?.toDate()?.let { sdf.format(it) } ?: "Em aberto"

                    val datasFormatadas = "Aluguel: $entradaStr\nDevolução: $saidaStr"

                    // Cálculo da multa
                    var multaValue = 0.0
                    val agora = Date()
                    
                    if (isDevolvido) {
                        // Se já devolveu, verifica se foi após o prazo
                        if (dataSaida != null && dataPrazo != null) {
                            if (dataSaida.toDate().after(dataPrazo.toDate())) {
                                val diff = dataSaida.toDate().time - dataPrazo.toDate().time
                                val dias = (diff / (1000 * 60 * 60 * 24)).toInt()
                                multaValue = dias * 0.50
                            }
                        }
                    } else {
                        // Se não devolveu, verifica se já passou do prazo
                        if (dataPrazo != null && agora.after(dataPrazo.toDate())) {
                            val diff = agora.time - dataPrazo.toDate().time
                            val dias = (diff / (1000 * 60 * 60 * 24)).toInt()
                            multaValue = dias * 0.50
                        }
                    }

                    val multaStr = String.format(Locale.getDefault(), "Valor da multa: R$ %.2f", multaValue)

                    // Criar a view do item
                    val itemView = LayoutInflater.from(this).inflate(R.layout.item_emprestimo_historico, containerHistorico, false)
                    
                    // Buscar informações do objeto (Livro, Jogo ou Sala)
                    val colecao = when (tipo) {
                        "livro" -> "livros"
                        "jogo" -> "jogos"
                        "sala" -> "salas"
                        else -> null
                    }

                    if (colecao != null) {
                        db.collection(colecao).document(idObjeto).get().addOnSuccessListener { objDoc ->
                            val titulo = objDoc.getString("titulo") ?: objDoc.getString("nome") ?: "Item"
                            val imgBase64 = objDoc.getString("imagemBase64")
                            
                            itemView.findViewById<TextView>(R.id.txtTitulo).text = titulo
                            val imgView = itemView.findViewById<ImageView>(R.id.imgItem)
                            
                            if (!imgBase64.isNullOrEmpty()) {
                                try {
                                    val decodedBytes = android.util.Base64.decode(imgBase64, android.util.Base64.DEFAULT)
                                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    imgView.setImageBitmap(bitmap)
                                } catch (e: Exception) {
                                    imgView.setImageResource(R.drawable.logo)
                                }
                            } else {
                                // Fallback images
                                val resId = when(tipo) {
                                    "livro" -> R.drawable.capadomquixote
                                    "jogo" -> R.drawable.uno
                                    else -> R.drawable.logo
                                }
                                imgView.setImageResource(resId)
                            }

                            // Clique para abrir detalhes (se for livro)
                            if (tipo == "livro") {
                                itemView.setOnClickListener {
                                    val livro = objDoc.toObject(Livro::class.java)?.copy(id = objDoc.id)
                                    if (livro != null) {
                                        val intent = Intent(this, LivroActivity::class.java)
                                        intent.putExtra("LIVRO", livro)
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    }

                    itemView.findViewById<TextView>(R.id.txtDatas).text = datasFormatadas
                    itemView.findViewById<TextView>(R.id.txtMulta).text = multaStr
                    
                    containerHistorico.addView(itemView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar histórico: ${e.message}", Toast.LENGTH_SHORT).show()
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
