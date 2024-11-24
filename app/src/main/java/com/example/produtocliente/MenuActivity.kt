package com.example.produtocliente


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity




class MenuActivity : AppCompatActivity() {


    private lateinit var btnClientes: Button
    private lateinit var btnProdutos: Button
    private lateinit var db: DatabaseHelper
    private var userId: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        db = DatabaseHelper(this)


        btnClientes = findViewById(R.id.btnClientes)
        btnProdutos = findViewById(R.id.btnProdutos)


        val login = intent.getStringExtra("login")
        val cursor = db.getUser(login ?: "")
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }


        btnClientes.setOnClickListener {
            val intent = Intent(this@MenuActivity, ClientesActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }


        btnProdutos.setOnClickListener {
            val intent = Intent(this@MenuActivity, ProdutosActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}

