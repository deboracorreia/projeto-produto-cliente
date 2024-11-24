package com.example.produtocliente

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream


class ProdutosActivity : AppCompatActivity() {


    private lateinit var etNome: EditText
    private lateinit var etPrice: EditText
    private lateinit var etMarca: EditText
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var imgFotoProdutos: ImageView
    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView
    private lateinit var produtoList: ArrayList<ProdutoModel>
    private var produto = ProdutoModel()
    private var produtoid: Int = -1
    private var fotoBase64: String? = null // Para armazenar a foto em Base64

    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 200
    private val CAMERA_PERMISSION_CODE = 101


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produtos)


        db = DatabaseHelper(this)
        etNome = findViewById(R.id.etNome)
        etPrice = findViewById(R.id.etPrice)
        etMarca = findViewById(R.id.etMarca)
        imgFotoProdutos = findViewById(R.id.imgFotoProduto)
        val btnFoto: Button = findViewById(R.id.btnFoto)
        listView = findViewById(R.id.listViewProdutos)
        btnSave = findViewById(R.id.btnSave)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnUpdate.isEnabled = false
        btnDelete.isEnabled = false


        updateListView()
        userId = intent.getIntExtra("userId", -1)


        btnFoto.setOnClickListener {
            val options = arrayOf("Câmera", "Galeria")
            val builder = AlertDialog.Builder(this)
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            builder.show()
        }

        btnSave.setOnClickListener {
            val nome = etNome.text.toString()
            val price = etPrice.text.toString().toDoubleOrNull()
            val marca = etMarca.text.toString()

            val userId = userId
            if (nome.isNotEmpty()) {
                produto.nome = nome
                produto.price //retirado text
                produto.marca = marca
                produto.userId = userId
                if (db.insertProduct(nome, price, marca, userId, fotoBase64)) {
                    Toast.makeText(this, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    updateListView()
                    limparCampos()
                } else {
                    Toast.makeText(this, "Erro ao cadastrar produto", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Preencha todos os campos e adicione uma foto!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        listView.setOnItemClickListener { _, _, position, _ ->
            val produto = produtoList[position]
            etNome.setText(produto.nome)
            etPrice.setText(produto.price.toString())
            etMarca.setText(produto.marca)
            produtoid = produto.id
            fotoBase64 = produto.foto


            // Converte Base64 para Bitmap e exibe no ImageView
            if (!fotoBase64.isNullOrEmpty()) {
                val bitmap = base64ToBitmap(fotoBase64!!)
                imgFotoProdutos.setImageBitmap(bitmap)
            }
            btnUpdate.isEnabled = true
            btnDelete.isEnabled = true
            btnSave.isEnabled = false
        }
        btnUpdate.setOnClickListener {
            val nome = etNome.text.toString()
            val price = etPrice.text.toString().toDouble()
            val marca = etMarca.text.toString()
            val userId = userId.toString().toIntOrNull() ?: -1 //retirado o text
//            val produto = ProdutoModel(produtoid, nome, price, marca, userId)
//            db.updateProduto(produto)
//            updateListView()
//            limparCampos()
//            Toast.makeText(this, "Produto alterado com sucesso!", Toast.LENGTH_SHORT)
//                .show()

            if (nome.isNotEmpty() && marca.isNotEmpty() && fotoBase64 != null) {
                val produtoAtualizado =
                    ProdutoModel(produtoid, nome, price, marca, userId, fotoBase64)


                if (db.updateProduto(produtoAtualizado) > 0) {
                    Toast.makeText(this, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    updateListView()
                    limparCampos()
                } else {
                    Toast.makeText(this, "Erro ao atualizar produto", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Preencha todos os campos e adicione uma foto!",
                    Toast.LENGTH_SHORT
                ).show()
            }
//chave a mais?        }

        }
        btnDelete.setOnClickListener {
            db.deleteProduto(produtoid)
            Toast.makeText(this, "Produto excluído com sucesso!", Toast.LENGTH_SHORT).show()
            updateListView()
            limparCampos()
        }

        updateListView()
    }


    private fun limparCampos() {
        etNome.text.clear()
        etPrice.text.clear()
        etMarca.text.clear()

        imgFotoProdutos.setImageResource(R.drawable.ic_launcher_foreground) // Substituir pelo seu placeholder
        fotoBase64 = null

    }

    private fun updateListView() {
        produtoList = db.listarProdutos()
        val nomeProduto = produtoList.map { it.id.toString() + " - " + it.nome }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nomeProduto)
        listView.adapter = adapter
    }

    private fun openCamera() {
        if (checkCameraPermission()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_CAMERA)
        } else {
            requestCameraPermission()
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    imgFotoProdutos.setImageBitmap(photo)
                    fotoBase64 = bitmapToBase64(photo) // Converte a foto para Base64
                }


                REQUEST_GALLERY -> {
                    val uri = data?.data
                    imgFotoProdutos.setImageURI(uri)
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    fotoBase64 = bitmapToBase64(bitmap) // Converte a foto para Base64
                }
            }
        }
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun base64ToBitmap(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}






