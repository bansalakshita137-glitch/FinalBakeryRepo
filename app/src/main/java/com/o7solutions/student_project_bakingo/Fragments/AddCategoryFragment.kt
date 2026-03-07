package com.o7solutions.student_project_bakingo.Fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.o7solutions.student_project_bakingo.Category
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.Test.AppwriteManager
import kotlinx.coroutines.launch

class AddCategoryFragment : Fragment() {

    private lateinit var etCategoryName: TextInputEditText
    private lateinit var imgCategory: ImageView
    private lateinit var btnSaveCategory: MaterialButton

    private var selectedImageUri: Uri? = null

    private val databaseRef =
        FirebaseDatabase.getInstance().getReference("Categories")

    // Image Picker
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                imgCategory.setImageURI(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_add_category, container, false)


        view.findViewById<MaterialToolbar>(R.id.appBar).setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        etCategoryName = view.findViewById(R.id.etCategoryName)
        imgCategory = view.findViewById(R.id.imgCategory)
        btnSaveCategory = view.findViewById(R.id.btnSaveCategory)

        imgCategory.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnSaveCategory.setOnClickListener {
            saveCategory()
        }

        return view
    }

    private fun saveCategory() {

        val name = etCategoryName.text.toString().trim()

        if (name.isEmpty()) {
            etCategoryName.error = "Category name required"
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Select category image", Toast.LENGTH_SHORT).show()
            return
        }

        btnSaveCategory.isEnabled = false
        btnSaveCategory.text = "Uploading..."

        val appwriteManager = AppwriteManager.getInstance(requireContext())

        lifecycleScope.launch {
            try {
                // Upload image to Appwrite
                val imageUrl =
                    appwriteManager.uploadImageFromUri(selectedImageUri!!)

                // Save to Firebase
                val categoryId = databaseRef.push().key!!

                val category = Category(
                    name = name,
                    categoryUrl = imageUrl,
                    categoryId = categoryId,
                    time = System.currentTimeMillis().toString()
                )

                databaseRef.child(categoryId).setValue(category)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Category added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        resetUI()
                    }
                    .addOnFailureListener {
                        showError(it.message)
                    }

            } catch (e: Exception) {
                showError(e.message)
            }
        }
    }

    private fun resetUI() {
        etCategoryName.setText("")
        imgCategory.setImageResource(android.R.drawable.ic_menu_camera)
        selectedImageUri = null
        btnSaveCategory.isEnabled = true
        btnSaveCategory.text = "Save Category"
    }

    private fun showError(message: String?) {
        btnSaveCategory.isEnabled = true
        btnSaveCategory.text = "Save Category"
        Toast.makeText(requireContext(), message ?: "Something went wrong", Toast.LENGTH_LONG).show()
    }
}