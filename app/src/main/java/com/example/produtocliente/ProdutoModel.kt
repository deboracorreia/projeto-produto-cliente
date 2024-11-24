package com.example.produtocliente


data class ProdutoModel(
    var id: Int = 0,
    var nome: String = "",
    var price: Double = 0.0,
    var marca: String = "",
    var userId: Int = 0,
    var foto: String? = null // Campo para armazenar a foto em Base64
)


