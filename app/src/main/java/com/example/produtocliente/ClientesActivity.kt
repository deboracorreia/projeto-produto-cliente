package com.example.produtocliente


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
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


class ClientesActivity : AppCompatActivity() {


    private lateinit var etNome: EditText
    private lateinit var etEmail: EditText
    private lateinit var etEndereco: EditText
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var imgFotoCliente: ImageView
    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView
    private lateinit var clienteList: ArrayList<ClienteModel>
    private var cliente = ClienteModel()
    private var clienteId: Int = -1
    private var fotoBase64: String? = null // Para armazenar a foto em Base64


    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 200
    private val CAMERA_PERMISSION_CODE = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)


        db = DatabaseHelper(this)
        etNome = findViewById(R.id.etNome)
        etEmail = findViewById(R.id.etEmail)
        etEndereco = findViewById(R.id.etEndereco)
        imgFotoCliente = findViewById(R.id.imgFotoCliente)
        val btnFoto: Button = findViewById(R.id.btnFoto)
        listView = findViewById(R.id.listViewClientes)
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
            val email = etEmail.text.toString()
            val endereco = etEndereco.text.toString()
            if (nome.isNotEmpty() && email.isNotEmpty()) {
                cliente.nome = nome
                cliente.email = email
                cliente.endereco = endereco
                cliente.userId = userId

                if (nome.isNotEmpty() && email.isNotEmpty() && fotoBase64 != null) {
                    cliente.nome = nome
                    cliente.email = email
                    cliente.endereco = endereco
                    cliente.foto = fotoBase64
                    cliente.userId = userId


                    if (db.insertClient(cliente)) {
                        Toast.makeText(this, "Cliente cadastrado com sucesso!", Toast.LENGTH_SHORT)
                            .show()
                        updateListView()
                        limparCampos()
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar cliente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Preencha todos os campos e adicione uma foto!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        listView.setOnItemClickListener { _, _, position, _ ->
            val cliente = clienteList[position]
            etNome.setText(cliente.nome)
            etEmail.setText(cliente.email)
            etEndereco.setText(cliente.endereco)
            clienteId = cliente.id
            fotoBase64 = db.getFotoClienteById(clienteId)// Alterado de cliente.foto para esse find para tentar corrigir o erro de ter várias fotos no Cursor


            // Converte Base64 para Bitmap e exibe no ImageView
            if (!fotoBase64.isNullOrEmpty()) {
                val bitmap = base64ToBitmap(fotoBase64!!)
                imgFotoCliente.setImageBitmap(bitmap)
            }

            btnUpdate.isEnabled = true
            btnDelete.isEnabled = true
            btnSave.isEnabled = false
        }

        btnUpdate.setOnClickListener {
            val nome = etNome.text.toString()
            val email = etEmail.text.toString()
            val endereco = etEndereco.text.toString()

            if (nome.isNotEmpty() && email.isNotEmpty() && fotoBase64 != null) {
                val clienteAtualizado =
                    ClienteModel(clienteId, nome, email, endereco, userId, fotoBase64)


                if (db.updateCliente(clienteAtualizado) > 0) {
                    Toast.makeText(this, "Cliente atualizado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    updateListView()
                    limparCampos()
                } else {
                    Toast.makeText(this, "Erro ao atualizar cliente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Preencha todos os campos e adicione uma foto!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnDelete.setOnClickListener {
            db.deleteCliente(clienteId)
            Toast.makeText(this, "Cliente excluído com sucesso!", Toast.LENGTH_SHORT).show()
            updateListView()
            limparCampos()
        }

        updateListView()

    }

    private fun limparCampos() {
        etNome.text.clear()
        etEmail.text.clear()
        etEndereco.text.clear()
        imgFotoCliente.setImageResource(R.drawable.ic_launcher_foreground) // Substituir pelo seu placeholder
        fotoBase64 = null
    }


    private fun updateListView() {
        clienteList = db.listarClientes()
        val nomeCliente = clienteList.map { it.id.toString() + " - " + it.nome }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nomeCliente)
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
                    imgFotoCliente.setImageBitmap(photo)
                    fotoBase64 = bitmapToBase64(photo) // Converte a foto para Base64
                }


                REQUEST_GALLERY -> {
                    val uri = data?.data
                    imgFotoCliente.setImageURI(uri)
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

