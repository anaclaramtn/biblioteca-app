package com.example.biblioteca_app.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Noticia(
    val titulo: String,
    val descricao: String,
    val imagemRes: Int? = null
)

data class Livro(
    val id: String = "",
    val titulo: String,
    val autor: String,
    val descricao: String,
    val imagemRes: Int,
    val disponivel: Boolean,
    val media: Float,
    val totalAvaliacoes: Int
) : Serializable

data class Notificacao(
    val titulo: String,
    val mensagem: String,
    val data: String,
    var lida: Boolean = false
)

data class Usuario(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    var notaMedia: Double = 0.0,
    var isAdmin: Boolean = false
)

data class Emprestimo(
    val titulo: String,
    val dataEmprestimo: String,
    val dataDevolucao: String? = null,
    val valorMulta: Double,
    val imagemRes: Int? = null,
    val isAtivo: Boolean
)

data class Jogo(
    val nome: String,
    val imagemRes: Int
)

data class Sala(
    val nome: String,
    val capacidade: Int
)

data class PesquisaAdm(
    val nome: String,
    val descricao: String,
    val disponibilidade: String
)

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

data class Avaliacao(
    val id: String = "",
    val idLivro: String = "",
    val idUsuario: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val nota: Float = 0f,
    val data: Timestamp? = null,
    val curtidas: Int = 0
)