package com.o7solutions.student_project_bakingo.Test

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.o7solutions.student_project_bakingo.R
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URI

class ImageUploadActivity : AppCompatActivity() {

    // 1. Initialize the Singleton instance
    private val appwriteManager by lazy { AppwriteManager.getInstance(applicationContext) }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {

                startImageUpload(uri)
        } else {
            Log.d("Appwrite", "No media selected")
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_upload)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private fun startImageUpload(uri: Uri) {
        lifecycleScope.launch {
            appwriteManager.uploadImageFromUri(uri)
        }
    }


}