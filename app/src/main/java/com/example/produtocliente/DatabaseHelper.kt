package com.example.produtocliente


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_nome, null, 1) {


    companion object {
        const val DATABASE_nome = "dbClientesProdutosNew"
        const val USERS_TABLE = "users"
        const val CLIENTES_TABLE = "clientes"
        const val PRODUTOS_TABLE = "produtos"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $USERS_TABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT, password TEXT, email TEXT)")
        db?.execSQL("CREATE TABLE $CLIENTES_TABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, email TEXT,endereco TEXT, userId INTEGER, foto TEXT)")
        db?.execSQL("CREATE TABLE $PRODUTOS_TABLE (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, price REAL ,marca TEXT, userId INTEGER, foto TEXT)")
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $USERS_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $CLIENTES_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $PRODUTOS_TABLE")
        onCreate(db)
    }


    // Operações de usuários
    fun checkUserExists(login: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $USERS_TABLE WHERE login=? AND password=?",
            arrayOf(login, password)
        )
        return cursor.count > 0
    }


    fun insertUser(login: String, password: String, email: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("login", login)
            put("password", password)
            put("email", email)
        }
        val result = db.insert(USERS_TABLE, null, contentValues)
        return result != -1L
    }


    fun getUser(login: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $USERS_TABLE WHERE login=?", arrayOf(login))
    }


    // Operações de clientes
    fun insertClient(Cliente: ClienteModel): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("nome", Cliente.nome)
            put("email", Cliente.email)
            put("userId", Cliente.userId)
            put("endereco", Cliente.endereco)
            put("foto", Cliente.foto) // Adicionando o campo de foto
        }
        val result = db.insert(CLIENTES_TABLE, null, contentValues)
        return result != -1L
    }


    fun updateCliente(Cliente: ClienteModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()  //nao colocamos o apply{
        contentValues.put("nome", Cliente.nome)
        contentValues.put("email", Cliente.email)
        contentValues.put("endereco", Cliente.endereco)
        contentValues.put("userId", Cliente.userId)
        contentValues.put("foto", Cliente.foto) // Adicionando o campo de foto

        val success =
            db.update(CLIENTES_TABLE, contentValues, "id=?", arrayOf(Cliente.id.toString()))
        db.close()
        return success
    }


    fun deleteCliente(id: Int): Int {
        val db = this.writableDatabase
        val success = db.delete(CLIENTES_TABLE, "id=?", arrayOf(id.toString()))
        db.close()
        return success
    }


    fun listarClientes(): ArrayList<ClienteModel> {
        val clienteList = ArrayList<ClienteModel>()
        val selectQuery = "SELECT id, nome, email, endereco, userId FROM $CLIENTES_TABLE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val cliente = ClienteModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    endereco = cursor.getString(cursor.getColumnIndexOrThrow("endereco")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"))
//                    foto = cursor.getString(cursor.getColumnIndexOrThrow("foto")) Removido para não pesar listagem de clientes, filtrar quando buscar cliente por id
                )
                clienteList.add(cliente)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return clienteList
    }


    // Operações de produtos
    fun insertProduct(nome: String, price: Double?, marca: String, userId: Int, foto: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("nome", nome)
            put("price", price) // retirado .tofloat
            put("marca", marca)
            put("userId", userId)
            put("foto", foto)
        }
        val result = db.insert(PRODUTOS_TABLE, null, contentValues)
        return result != -1L
    }



    fun updateProduto(Produto: ProdutoModel): Int {

//alterado

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put("id", Produto.id)
        contentValues.put("nome", Produto.nome)
        contentValues.put("marca", Produto.marca)
        contentValues.put("price", Produto.price.toFloat())
        contentValues.put("userId", Produto.userId)
        contentValues.put("foto", Produto.foto)



        val success =
//ALTERADO
            db.update(PRODUTOS_TABLE, contentValues, "id=?", arrayOf(Produto.id.toString()))

        db.close()

        return success

    }


//ALTERADO
    fun deleteProduto(id: Int): Int {

        val db = this.writableDatabase

        val success = db.delete(PRODUTOS_TABLE, "id=?", arrayOf(id.toString()))

        db.close()

        return success

    }

    fun listarProdutos(): ArrayList<ProdutoModel> {
        val produtoList = ArrayList<ProdutoModel>()
        val selectQuery = "SELECT * FROM $PRODUTOS_TABLE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val marcaIndex = cursor.getColumnIndexOrThrow("marca")
        if (cursor.moveToFirst()) {
            do {
                val produto = ProdutoModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    marca = cursor.getString(cursor.getColumnIndexOrThrow("marca"))?:"",
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                    foto = cursor.getString(cursor.getColumnIndexOrThrow("foto"))
                )
                produtoList.add(produto)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return produtoList
    }

    // Método adicionado para buscar a foto do cliente ao selecionar na ListView
    fun getFotoClienteById(clienteId: Int): String? {
        val db = this.readableDatabase
        var fotoBase64: String? = null
        val query = "SELECT foto FROM Clientes WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(clienteId.toString()))

        if (cursor.moveToFirst()) {
            fotoBase64 = cursor.getString(cursor.getColumnIndexOrThrow("foto"))
        }

        cursor.close()
        db.close()
        return fotoBase64
    }

}
