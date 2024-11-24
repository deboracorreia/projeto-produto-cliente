package com.example.produtocliente


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.produtocliente.DatabaseHelper
import com.example.produtocliente.MenuActivity
import com.example.produtocliente.R


class LoginActivity : AppCompatActivity() {


    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var db: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        db = DatabaseHelper(this)


        //Caso queira refazer o Banco de Dados descomente a linha abaixo

        //db.onUpgrade(db.writableDatabase,1,2)


        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        etEmail = findViewById(R.id.etEmail)
        etEmail.isVisible = false
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)


        btnLogin.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()


            if (db.checkUserExists(login, password)) {
                val intent = Intent(this@LoginActivity, MenuActivity::class.java)
                intent.putExtra("login", login)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show()
            }
        }


        btnRegister.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()
            val email = etEmail.text.toString()
            etEmail.isVisible = true
            if (login.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {




                if (etEmail.isVisible && etEmail.text.toString().isNotEmpty()) {
                    if (login.isNotEmpty() || password.isNotEmpty() || email.isNotEmpty()) {
                        if (db.insertUser(login, password, email)) {
                            Toast.makeText(
                                this,
                                "Usuário registrado com sucesso!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            limparCampos()
                            etEmail.isVisible = false
                        } else {
                            Toast.makeText(this, "Erro ao registrar usuário", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }
    }
    private fun limparCampos() {
        etLogin.text.clear()
        etPassword.text.clear()
        etEmail.text.clear()
    }
}



