package com.example.produtocliente


data class ClienteModel(
    var id: Int = 0,
    var nome: String = "",
    var email: String = "",
    var endereco: String = "",
    var userId: Int = 0,
    var foto: String? = null // Campo para armazenar a foto em Base64
)


