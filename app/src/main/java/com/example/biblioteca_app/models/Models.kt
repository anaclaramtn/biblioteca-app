package com.example.biblioteca_app.models

import java.io.Serializable

data class Noticia(
    val titulo: String,
    val descricao: String,
    val imagemRes: Int? = null
)

data class Livro(
    val titulo: String,
    val autor: String,
    val descricao: String,
    val imagemRes: Int,
    val disponivel: Boolean,
    val media: Float,
    val totalAvaliacoes: Int
) : Serializable