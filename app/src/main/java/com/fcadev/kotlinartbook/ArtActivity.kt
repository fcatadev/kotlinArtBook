package com.fcadev.kotlinartbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fcadev.kotlinartbook.databinding.ActivityArtBinding

class ArtActivity : AppCompatActivity() {

    lateinit var binding: ActivityArtBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun saveButtonClicked(view: View){}

    fun selectImage(view: View){}

}