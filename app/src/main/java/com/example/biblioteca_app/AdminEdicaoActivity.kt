package com.example.biblioteca_app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AdminEdicaoActivity : AppCompatActivity() {

    private var livroAtual: Livro? = null
    private var imageUri: Uri? = null
    private var imagemBase64: String? = null
    private val db = FirebaseFirestore.getInstance()

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) { e.printStackTrace() }

            val imgCapa = findViewById<ImageView>(R.id.imgCapa)
            imgCapa.setImageURI(uri)
            
            // Converter para Base64
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.let { btmp: Bitmap ->
                    val outputStream = ByteArrayOutputStream()
                    btmp.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val byteArray = outputStream.toByteArray()
                    imagemBase64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_edicao)

        livroAtual = intent.getSerializableExtra("LIVRO") as? Livro

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preencherDados()
        setupBotoes()
        setupNavBar()
    }

    private fun preencherDados() {
        livroAtual?.let { livro ->
            findViewById<EditText>(R.id.edtTitulo).setText(livro.titulo)
            findViewById<EditText>(R.id.edtAutor).setText(livro.autor)
            findViewById<EditText>(R.id.edtDescricao).setText(livro.descricao)
            
            imagemBase64 = livro.imagemBase64
            
            val imgCapa = findViewById<ImageView>(R.id.imgCapa)
            when {
                !livro.imagemBase64.isNullOrEmpty() -> {
                    try {
                        val decodedBytes = android.util.Base64.decode(livro.imagemBase64, android.util.Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        imgCapa.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imgCapa.setImageResource(R.drawable.capadomquixote)
                    }
                }
                livro.imagemRes != null && livro.imagemRes != 0 -> {
                    imgCapa.setImageResource(livro.imagemRes)
                }
                else -> {
                    imgCapa.setImageResource(R.drawable.capadomquixote)
                }
            }
        }
    }

    private fun setupBotoes() {
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            confirmarSaida()
        }

        findViewById<Button>(R.id.btnEnviar).setOnClickListener {
            salvarAlteracoes()
        }
        
        findViewById<ImageView>(R.id.imgCapa).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun salvarAlteracoes() {
        val novoTitulo = findViewById<EditText>(R.id.edtTitulo).text.toString()
        val novoAutor = findViewById<EditText>(R.id.edtAutor).text.toString()
        val novaDesc = findViewById<EditText>(R.id.edtDescricao).text.toString()

        if (novoTitulo.isBlank() || novoAutor.isBlank() || novaDesc.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        livroAtual?.let { livro: Livro ->
            val updates = hashMapOf<String, Any?>(
                "titulo" to novoTitulo,
                "autor" to novoAutor,
                "sinopse" to novaDesc,
                "imagemBase64" to imagemBase64
            )
            
            db.collection("livros").document(livro.id)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Livro atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao atualizar!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun confirmarSaida() {
        AlertDialog.Builder(this)
            .setTitle("Certeza que deseja voltar?")
            .setMessage("Suas informações não serão salvas.")
            .setPositiveButton("Sim") { _, _ -> finish() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.navbar)
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
