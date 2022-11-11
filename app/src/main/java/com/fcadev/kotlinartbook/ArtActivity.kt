package com.fcadev.kotlinartbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fcadev.kotlinartbook.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {

    lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("new")){
            binding.artNameText.setText("")
            binding.artistNameText.setText("")
            binding.yearText.setText("")
            binding.imageView.setImageResource(R.drawable.selectimages)
            binding.saveButton.visibility = View.VISIBLE
        }else{
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)

            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artNameIndex = cursor.getColumnIndex("artname")
            val artistNameIndex = cursor.getColumnIndex("artistname")
            val yearNameIndex = cursor.getColumnIndex("year")
            val imageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.artNameText.setText(cursor.getString(artNameIndex))
                binding.artistNameText.setText(cursor.getString(artistNameIndex))
                binding.yearText.setText(cursor.getString(yearNameIndex))

                val byteArr = cursor.getBlob(imageIndex)
                val bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)

                binding.imageView.setImageBitmap(bitmap)
            }

            cursor.close()

        }

    }

    fun saveButtonClicked(view: View){
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArr = outputStream.toByteArray()

            try {
                //val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArr)
                statement.execute()
            }catch (e: Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@ArtActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //bundan önce açık ne kadar activity varsa hepsini kapat maine dön demek.
            startActivity(intent)

        }
    }

    //Bitmap küçültme fonksiyonu her proje de kullanılabilir.
    private fun makeSmallerBitmap (image: Bitmap, maximumSize: Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bipmapRatio : Double = width.toDouble() / height.toDouble()
        if (bipmapRatio > 1){
            //Görsel yataydır
            width = maximumSize
            val scaleHeight = width / bipmapRatio
            height = scaleHeight.toInt()
        }else{
            //Görsel dikeydir
            height = maximumSize
            val scaleWidth = height * bipmapRatio
            width = scaleWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(view: View){

        // Galariye erişim izin işlemleri
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                //neden izin istediğimizi göstermek
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                    //izin istemek
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                //izin istemek
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            //Galeriye git
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }

    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    val imageUri = intentFromResult.data
                    //binding.imageView.setImageURI(imageUri)
                    if (imageUri != null){
                        try {
                            val source = ImageDecoder.createSource(contentResolver, imageUri)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }

                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }

    }

}