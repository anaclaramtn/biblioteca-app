package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.biblioteca_app.databinding.DialogConfirmacaoCancelarBinding
import com.example.biblioteca_app.databinding.DialogDenunciaBinding
import com.example.biblioteca_app.databinding.DialogSucessoDenunciaBinding
import com.example.biblioteca_app.databinding.ItemAvaliacaoBinding
import com.example.biblioteca_app.databinding.TelaMaisAvaliacoesBinding
import com.example.biblioteca_app.models.Avaliacao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class MaisAvaliacoesActivity : AppCompatActivity() {

    private lateinit var binding: TelaMaisAvaliacoesBinding
    private val db = FirebaseFirestore.getInstance()

    private var idLivro: String = ""
    private var media: Float = 0.0f
    private var totalAvaliacoes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaMaisAvaliacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idLivro = intent.getStringExtra("ID_LIVRO") ?: "ID_LIVRO_TESTE_123"
        media = intent.getFloatExtra("MEDIA", 0.0f)
        totalAvaliacoes = intent.getIntExtra("TOTAL", 0)

        preencherResumo()
        configurarBotoes()
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
                binding.containerAvaliacoes.removeAllViews()
                val avaliacoes = documents.mapNotNull { it.toObject(Avaliacao::class.java).copy(id = it.id) }
                
                if (avaliacoes.isEmpty()) {
                    binding.txtSemAvaliacoes.visibility = View.VISIBLE
                } else {
                    binding.txtSemAvaliacoes.visibility = View.GONE
                    avaliacoes.forEach { avaliacao ->
                        adicionarItemAvaliacao(avaliacao)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar avaliações", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarItemAvaliacao(avaliacao: Avaliacao) {
        val itemBinding = ItemAvaliacaoBinding.inflate(layoutInflater, binding.containerAvaliacoes, false)
        binding.containerAvaliacoes.addView(itemBinding.root)

        db.collection("usuarios").document(avaliacao.idUsuario).get()
            .addOnSuccessListener { userDoc ->
                val nome = userDoc.getString("nome") ?: "Usuário"
                configurarItemAvaliacao(
                    itemBinding,
                    nome,
                    avaliacao.titulo,
                    avaliacao.descricao,
                    avaliacao.data,
                    avaliacao.curtidas
                )
            }
    }

    private fun preencherResumo() {
        binding.layoutResumo.txtMedia.text = media.toString()
        binding.layoutResumo.txtTotalAvaliacoes.text = "($totalAvaliacoes avaliações)"
        binding.layoutResumo.txtEstrelasMedia.text = converterMediaParaEstrelas(media)
        binding.txtTituloHeader.text = "Avaliações ($totalAvaliacoes)"
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

    private fun configurarBotoes() {
        binding.btnVoltar.setOnClickListener {
            finish()
        }

        binding.btnAvaliar.setOnClickListener {
            val intent = android.content.Intent(this, AvaliarActivity::class.java)
            intent.putExtra("ID_LIVRO", idLivro)
            startActivity(intent)
        }

        binding.btnOrdenar.setOnClickListener {
            mostrarDialogOrdenacao()
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

    private fun configurarItemAvaliacao(
        itemBinding: ItemAvaliacaoBinding,
        nome: String,
        titulo: String,
        comentario: String,
        data: Timestamp?,
        curtidas: Int = 0
    ) {
        var curtido = false
        var numCurtidas = curtidas
        itemBinding.txtNomeUsuario.text = nome
        itemBinding.txtTituloAvaliacao.text = titulo
        itemBinding.txtComentario.text = comentario

        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm:ss a", Locale.ENGLISH)
        val dataFormatada = data?.toDate()?.let { sdf.format(it) } ?: "Data desconhecida"
        itemBinding.txtData.text = dataFormatada
        itemBinding.txtCurtidas.text = numCurtidas.toString()
        itemBinding.txtComentario.visibility = View.VISIBLE
        itemBinding.btnDenunciar.setOnClickListener { mostrarDialogDenuncia() }

        itemBinding.btnCurtir.setOnClickListener {
            curtido = !curtido
            if (curtido) {
                numCurtidas++
                itemBinding.btnCurtir.setImageResource(R.drawable.ic_heart_filled)
            } else {
                numCurtidas--
                itemBinding.btnCurtir.setImageResource(R.drawable.ic_heart)
            }
            itemBinding.txtCurtidas.text = numCurtidas.toString()
        }
    }

    private fun mostrarDialogDenuncia() {
        val dialogBinding = DialogDenunciaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelar.setOnClickListener {
            mostrarConfirmacaoCancelar(dialog)
        }

        dialogBinding.btnEnviar.setOnClickListener {
            val selectedId = dialogBinding.radioGroup.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(this, "Por favor, selecione um motivo para a denúncia", Toast.LENGTH_SHORT).show()
            } else if (selectedId == R.id.rbOutro && dialogBinding.edtOutro.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Por favor, descreva o motivo da denúncia", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                mostrarSucessoDenuncia()
            }
        }

        dialog.show()
    }

    private fun mostrarConfirmacaoCancelar(parentDialog: AlertDialog) {
        val dialogBinding = DialogConfirmacaoCancelarBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnNao.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSim.setOnClickListener {
            dialog.dismiss()
            parentDialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarSucessoDenuncia() {
        val dialogBinding = DialogSucessoDenunciaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_busca

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