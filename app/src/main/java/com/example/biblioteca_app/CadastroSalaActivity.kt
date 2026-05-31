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

import com.example.biblioteca_app.models.Sala

class CadastroSalaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var salaParaEditar: Sala? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_cadastro_sala)

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Verifica se veio algo para editar
        salaParaEditar = intent.getSerializableExtra("SALA") as? Sala
        salaParaEditar?.let {
            findViewById<EditText>(R.id.edtNomeSala).setText(it.nome)
            findViewById<EditText>(R.id.edtCapacidadeSala).setText(it.capacidade.toString())
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
            val nome = findViewById<EditText>(R.id.edtNomeSala).text.toString()
            val cap = findViewById<EditText>(R.id.edtCapacidadeSala).text.toString()

            if (nome.isBlank() || cap.isBlank()) {
                exibirAviso()
            } else {
                val capacidadeInt = cap.toIntOrNull() ?: 0

                // Prepara os dados da sala para o Firestore
                val dadosSala = hashMapOf(
                    "nome" to nome,
                    "capacidade" to capacidadeInt,
                    "isDisponivel" to true
                )

                if (salaParaEditar == null) {
                    // MODO CADASTRO
                    db.collection("salas")
                        .add(dadosSala)
                        .addOnSuccessListener { docRef ->
                            AcervoadmActivity.listaSalas.add(0, Sala(docRef.id, nome, capacidadeInt))
                            Toast.makeText(this, "Acervo Atualizado!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // MODO EDIÇÃO
                    db.collection("salas")
                        .document(salaParaEditar!!.id)
                        .set(dadosSala)
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
