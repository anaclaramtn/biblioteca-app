package com.example.biblioteca_app.models

import com.google.firebase.Timestamp
import java.io.Serializable

data class Noticia(
    var id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val descricaoLonga: String = "",
    val imagemRes: Int? = null,
    val imagemBase64: String? = null
) : Serializable

data class Livro(
    var id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val descricao: String = "",
    val imagemRes: Int? = null,
    val imagemBase64: String? = null,
    val disponivel: Boolean = true,
    val media: Float = 0f,
    val totalAvaliacoes: Int = 0
) : Serializable
data class Notificacao(
    var id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val data: String = "",
    var lida: Boolean = false,
    val idDocOriginal: String? = null,
    val timestamp: Long = 0
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
    val imagemBase64: String? = null,
    val isAtivo: Boolean
)

data class Jogo(
    var id: String = "",
    val nome: String = "",
    val imagemRes: Int = 0,
    val imagemBase64: String? = null,
    val isDisponivel: Boolean = true
) : Serializable

data class Sala(
    var id: String = "",
    val nome: String = "",
    val capacidade: Int = 0
) : Serializable

data class PesquisaAdm(
    var id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val disponibilidade: String = ""
) : Serializable

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
    var curtidas: Int = 0
)