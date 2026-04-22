package com.example.biblioteca_app.models

import androidx.annotation.DrawableRes

data class Noticia(
    val titulo: String,
    val descricao: String
)

data class Livro(
    val titulo: String,
    val autor: String,
    @DrawableRes val imagemRes: Int // O "ID" da capa do livro e o drawableres é pra saber que vai receber um R.drawable
)