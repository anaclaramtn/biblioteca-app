package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.biblioteca_app.models.Emprestimo
import com.example.biblioteca_app.models.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DetalhesusuarioadmActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var usuarioId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_detalhesusuarioadm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val header = findViewById<View>(R.id.header)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)
        val titulo = header.findViewById<TextView>(R.id.txtTitulo)

        titulo.text = "Usuários"

        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener {
            finish()
        }

        usuarioId = intent.getStringExtra("USUARIO_ID")

        if (usuarioId == null) {
            Toast.makeText(this, "Erro ao carregar usuário", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarDadosUsuario()
        
        findViewById<TextView>(R.id.txtMultaTotal).setOnClickListener {
            Toast.makeText(this, "Abrir diálogo para editar multa", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
            showDeleteConfirmationDialog()
        }

        setupNavBar()
    }

    private fun carregarDadosUsuario() {
        val uid = usuarioId ?: return

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                if (usuario != null) {
                    findViewById<TextView>(R.id.txtNome).text = usuario.nome
                    findViewById<TextView>(R.id.txtEmail).text = usuario.email
                }
            }

        setupRecyclerViews()
    }

    private fun setupRecyclerViews() {
        val uid = usuarioId ?: return
        val rvAtivos = findViewById<RecyclerView>(R.id.recyclerAtivos)
        val rvHistorico = findViewById<RecyclerView>(R.id.recyclerHistorico)
        
        rvAtivos.layoutManager = LinearLayoutManager(this)
        rvHistorico.layoutManager = LinearLayoutManager(this)

        val listaAtivos = mutableListOf<Emprestimo>()
        val listaHistorico = mutableListOf<Emprestimo>()

        val adapterAtivos = GenericAdapter(R.layout.item_emprestimo_ativo, listaAtivos) { view, item, _ ->
            preencherItemEmprestimo(view, item)
        }
        val adapterHistorico = GenericAdapter(R.layout.item_emprestimo_historico, listaHistorico) { view, item, _ ->
            preencherItemEmprestimo(view, item)
        }

        rvAtivos.adapter = adapterAtivos
        rvHistorico.adapter = adapterHistorico

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        db.collection("historico")
            .whereEqualTo("idUsuario", uid)
            .get()
            .addOnSuccessListener { snapshots ->
                var multaTotalGeral = 0.0

                if (snapshots.isEmpty) {
                    findViewById<TextView>(R.id.txtMultaTotal).text = "Multa Total: R$ 0,00"
                    return@addOnSuccessListener
                }

                for (doc in snapshots) {
                    val tipo = doc.getString("tipoObjeto") ?: ""
                    val idObjeto = doc.getString("idObjeto") ?: ""
                    val dataEmprestimo = doc.getTimestamp("dataEntrada")?.toDate()?.let { sdf.format(it) } ?: "-"
                    val dataDevolucao = doc.getTimestamp("dataSaida")?.toDate()?.let { sdf.format(it) }
                    val dataPrazo = doc.getTimestamp("dataPrazo")
                    
                    val isDevolvido = doc.getBoolean("isDevolvido") ?: false
                    val isAtivo = if (tipo == "livro") {
                        !isDevolvido
                    } else {
                        val saida = doc.getTimestamp("dataSaida")?.toDate()
                        saida != null && saida.after(java.util.Date())
                    }

                    var multaIndividual = 0.0
                    if (isAtivo && dataPrazo != null) {
                        val agora = com.google.firebase.Timestamp.now().toDate().time
                        val prazo = dataPrazo.toDate().time
                        if (agora > prazo) {
                            val diff = agora - prazo
                            val diasAtraso = (diff / (1000 * 60 * 60 * 24)).toInt()
                            multaIndividual = diasAtraso * 0.50
                            multaTotalGeral += multaIndividual
                        }
                    }

                    val emprestimo = Emprestimo(
                        titulo = "Carregando...",
                        dataEmprestimo = dataEmprestimo,
                        dataDevolucao = dataDevolucao,
                        valorMulta = multaIndividual,
                        isAtivo = isAtivo
                    )

                    if (isAtivo) {
                        adapterAtivos.addItem(emprestimo)
                    } else {
                        adapterHistorico.addItem(emprestimo)
                    }

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
                            
                            val adapterAlvo = if (isAtivo) adapterAtivos else adapterHistorico
                            val items = adapterAlvo.getItems()
                            
                            val index = items.indexOfFirst { it.dataEmprestimo == dataEmprestimo && it.titulo == "Carregando..." }
                            if (index != -1) {
                                val novo = items[index].copy(titulo = titulo, imagemBase64 = imgBase64)
                                adapterAlvo.updateItem(index, novo)
                            }
                        }
                    }
                }
                findViewById<TextView>(R.id.txtMultaTotal).text = String.format(Locale.getDefault(), "Multa Total: R$ %.2f", multaTotalGeral)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar histórico: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun preencherItemEmprestimo(view: View, item: Emprestimo) {
        view.findViewById<TextView>(R.id.txtTitulo).text = item.titulo
        val tvDatas = view.findViewById<TextView>(R.id.txtDatas)
        if (item.isAtivo) {
            tvDatas.text = "Aluguel: ${item.dataEmprestimo}"
        } else {
            tvDatas.text = "Aluguel: ${item.dataEmprestimo}\nDevolução: ${item.dataDevolucao}"
        }
        
        view.findViewById<TextView>(R.id.txtMulta).text = String.format(Locale.getDefault(), "Valor da multa: R$ %.2f", item.valorMulta)
        
        val img = view.findViewById<ImageView>(R.id.imgItem)
        if (!item.imagemBase64.isNullOrEmpty()) {
            val decodedBytes = android.util.Base64.decode(item.imagemBase64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            img.setImageBitmap(bitmap)
            img.visibility = View.VISIBLE
        } else if (item.imagemRes != null && item.imagemRes != 0) {
            img.setImageResource(item.imagemRes)
            img.visibility = View.VISIBLE
        } else {
            img.setImageResource(R.drawable.logo)
            img.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente remover este usuário?")
            .setPositiveButton("Sim") { _, _ ->
                usuarioId?.let { uid ->
                    db.collection("usuarios").document(uid).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuário removido", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao remover usuário", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_usuarios

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
                R.id.nav_usuarios -> true
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
