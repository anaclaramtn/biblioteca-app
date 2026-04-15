package com.example.biblioteca_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// como uma nova activity deve ser criada assim que adicionamos uma tela
//res > new > empty views activity

class tela_cadastro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_cadastro)
    }
}