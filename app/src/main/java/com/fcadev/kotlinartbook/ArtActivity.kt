package com.fcadev.kotlinartbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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

class ArtActivity : AppCompatActivity() {

    lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()

    }

    fun saveButtonClicked(view: View){}

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