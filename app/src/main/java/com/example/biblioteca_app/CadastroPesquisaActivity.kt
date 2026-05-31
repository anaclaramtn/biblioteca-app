package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

import com.example.biblioteca_app.models.PesquisaAdm

class CadastroPesquisaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var pesquisaParaEditar: PesquisaAdm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_cadastro_pesquisa)

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Verifica se veio algo para editar
        pesquisaParaEditar = intent.getSerializableExtra("PESQUISA") as? PesquisaAdm
        pesquisaParaEditar?.let {
            findViewById<EditText>(R.id.edtNomeProfessor).setText(it.nome)
            findViewById<EditText>(R.id.edtDescricaoAtividade).setText(it.descricao)
            findViewById<EditText>(R.id.edtInfoAdicionais).setText(it.disponibilidade)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            confirmarSaida()
        }

        findViewById<AppCompatButton>(R.id.btnEnviar).setOnClickListener {
            val prof = findViewById<EditText>(R.id.edtNomeProfessor).text.toString()
            val desc = findViewById<EditText>(R.id.edtDescricaoAtividade).text.toString()
            val info = findViewById<EditText>(R.id.edtInfoAdicionais).text.toString()

            if (prof.isBlank() || desc.isBlank() || info.isBlank()) {
                exibirAviso()
            } else {
                // Prepara os dados da pesquisa para o Firestore
                val dadosPesquisa = hashMapOf(
                    "nome" to prof,
                    "descricao" to desc,
                    "disponibilidade" to info
                )

                if (pesquisaParaEditar == null) {
                    // MODO CADASTRO
                    db.collection("pesquisaCientifica")
                        .add(dadosPesquisa)
                        .addOnSuccessListener { docRef ->
                            // Também adiciona na lista local para feedback imediato
                            AcervoadmActivity.listaPesquisas.add(0, PesquisaAdm(docRef.id, prof, desc, info))

                            Toast.makeText(this, "Acervo Atualizado!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // MODO EDIÇÃO
                    db.collection("pesquisaCientifica")
                        .document(pesquisaParaEditar!!.id)
                        .set(dadosPesquisa)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Acervo Atualizado!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        setupNavBar()
    }

    private fun confirmarSaida() {
        AlertDialog.Builder(this)
            .setTitle("Certeza que deseja voltar?")
            .setMessage("Suas informações não serão salvas.")
            .setPositiveButton("Sim") { _, _ -> finish() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun exibirAviso() {
        AlertDialog.Builder(this)
            .setTitle("Aviso")
            .setMessage("Todos os campos precisam ser preenchidos")
            .setPositiveButton("OK", null)
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
