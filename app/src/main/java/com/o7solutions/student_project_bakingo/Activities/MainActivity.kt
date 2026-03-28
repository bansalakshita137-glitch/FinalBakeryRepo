package com.o7solutions.student_project_bakingo.Activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.o7solutions.student_project_bakingo.R
import com.o7solutions.student_project_bakingo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Edge-to-Edge first
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Set the status bar color (For API < 35 compatibility)
        window.statusBarColor = Color.parseColor("#DE4964")

        // 3. For Android 15 (API 35+), we must disable default contrast
        // to allow our custom color/background to show properly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.isStatusBarContrastEnforced = false
        }

        // 4. Force the Icons to be WHITE (AppearanceLight = false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        // 5. Apply Insets to the ROOT so content doesn't overlap the status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // This ensures the top status bar area gets the color of your background
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,0)
            insets
        }

        // 6. Navigation Setup
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }
}