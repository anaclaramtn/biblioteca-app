package com.example.biblioteca_app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.ChatMessage

class ChatBotActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_chatbot)

        setupHeader()

    }

    private fun setupHeader() {
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.txtNomeChatBot).text = "ChatBot"
    }
}
